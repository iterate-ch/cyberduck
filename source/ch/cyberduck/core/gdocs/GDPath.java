package ch.cyberduck.core.gdocs;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gdata.client.CoreErrorDomain;
import com.google.gdata.client.DocumentQuery;
import com.google.gdata.client.GoogleAuthTokenFactory;
import com.google.gdata.client.Service;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.*;
import com.google.gdata.data.acl.AclEntry;
import com.google.gdata.data.acl.AclFeed;
import com.google.gdata.data.acl.AclRole;
import com.google.gdata.data.acl.AclScope;
import com.google.gdata.data.docs.*;
import com.google.gdata.data.extensions.LastModifiedBy;
import com.google.gdata.data.media.MediaMultipart;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.NotImplementedException;
import com.google.gdata.util.ServiceException;

import javax.mail.MessagingException;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class GDPath extends Path {
    private static Logger log = Logger.getLogger(GDPath.class);

    private static class Factory extends PathFactory<GDSession> {
        @Override
        protected Path create(GDSession session, String path, int type) {
            return new GDPath(session, path, type);
        }

        @Override
        protected Path create(GDSession session, String parent, String name, int type) {
            return new GDPath(session, parent, name, type);
        }

        @Override
        protected Path create(GDSession session, String parent, Local file) {
            return new GDPath(session, parent, file);
        }

        @Override
        protected <T> Path create(GDSession session, T dict) {
            return new GDPath(session, dict);
        }
    }

    public static PathFactory factory() {
        return new Factory();
    }

    @Override
    protected void init(Deserializer dict) {
        String resourceIdObj = dict.stringForKey("ResourceId");
        if(resourceIdObj != null) {
            this.setResourceId(resourceIdObj);
        }
        String exportUriObj = dict.stringForKey("ExportUri");
        if(exportUriObj != null) {
            this.setExportUri(exportUriObj);
        }
        String documentTypeObj = dict.stringForKey("DocumentType");
        if(documentTypeObj != null) {
            this.setDocumentType(documentTypeObj);
        }
        super.init(dict);
    }

    @Override
    protected <S> S getAsDictionary(Serializer dict) {
        if(resourceId != null) {
            dict.setStringForKey(resourceId, "ResourceId");
        }
        if(exportUri != null) {
            dict.setStringForKey(exportUri, "ExportUri");
        }
        if(documentType != null) {
            dict.setStringForKey(documentType, "DocumentType");
        }
        return super.<S>getAsDictionary(dict);
    }

    private static final String DOCUMENT_FOLDER_TYPE = "folder";
    private static final String DOCUMENT_FILE_TYPE = "file";
    private static final String DOCUMENT_TEXT_TYPE = "document";
    private static final String DOCUMENT_PRESENTATION_TYPE = "presentation";
    private static final String DOCUMENT_SPREADSHEET_TYPE = "spreadsheet";

    private final GDSession session;

    protected GDPath(GDSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected GDPath(GDSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected GDPath(GDSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected <T> GDPath(GDSession s, T dict) {
        super(dict);
        this.session = s;
    }

    private String documentType;

    public String getDocumentType() {
        if(null == documentType) {
            if(attributes().isDirectory()) {
                return DOCUMENT_FOLDER_TYPE;
            }
            // Arbitrary file type not converted to Google Docs.
            return DOCUMENT_FILE_TYPE;
        }
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    /**
     * Unique identifier
     */
    private String exportUri;

    /**
     * @return Download URL without export format.
     */
    public String getExportUri() {
        if(StringUtils.isBlank(exportUri)) {
            log.warn("Refetching Export URI for " + this.toString());
            final GDPath cached = (GDPath) this.getParent().children().get(this.getReference());
            if(null == cached) {
                log.error("Missing Export URI for " + this.toString());
                return null;
            }
            exportUri = cached.getExportUri();
        }
        return exportUri;
    }

    public void setExportUri(String exportUri) {
        this.exportUri = exportUri;
    }

    private String resourceId;

    public String getResourceId() {
        if(StringUtils.isBlank(resourceId)) {
            log.warn("Refetching Resource ID for " + this.toString());
            final GDPath cached = (GDPath) this.getParent().children().get(this.getReference());
            if(null == cached) {
                log.error("Missing Resource ID for " + this.toString());
                return null;
            }
            resourceId = cached.getResourceId();
        }
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    private String documentUri;

    /**
     * @return The URL to the document editable in the web browser
     */
    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    private String getDocumentId() {
        // Removing document type from resourceId gives us the documentId
        return StringUtils.removeStart(this.getResourceId(), this.getDocumentType() + ":");
    }

    /**
     * @return Includes the protocol and hostname only
     */
    protected StringBuilder getFeed() {
        return new StringBuilder(this.getSession().getHost().getProtocol().getScheme()).append("://").append(
                this.getSession().getHost().getHostname());
    }

    protected URL getFolderFeed() throws MalformedURLException {
        final StringBuilder feed = this.getFeed();
        if(this.isRoot()) {
            return new URL(feed.append("/feeds/default/private/full/folder%3Aroot/contents").toString());
        }
        return new URL(feed.append("/feeds/default/private/full/folder%3A").append(this.getDocumentId()).append("/contents").toString());
    }

    protected URL getAclFeed() throws MalformedURLException {
        final StringBuilder feed = this.getFeed();
        return new URL(feed.append("/feeds/default/private/full/").append(this.getResourceId()).append("/acl").toString());
    }

    public URL getRevisionsFeed() throws MalformedURLException {
        final StringBuilder feed = this.getFeed();
        return new URL(feed.append("/feeds/default/private/full/").append(this.getResourceId()).append("/revisions").toString());
    }

    @Override
    public void readSize() {
        ;
    }

    @Override
    public void readTimestamp() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void readAcl() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                    this.getName()));
            try {
                Acl acl = new Acl();
                AclFeed feed = this.getSession().getClient().getFeed(this.getAclFeed(), AclFeed.class);
                for(AclEntry entry : feed.getEntries()) {
                    AclScope scope = entry.getScope();
                    AclScope.Type type = scope.getType();
                    AclRole role = entry.getRole();
                    if(type.equals(AclScope.Type.DEFAULT)) {
                        acl.addAll(new Acl.CanonicalUser(scope.getValue()), new Acl.Role(role.getValue()));
                    }
                    if(type.equals(AclScope.Type.USER)) {
                        acl.addAll(new Acl.CanonicalUser(scope.getValue()), new Acl.Role(role.getValue()));
                    }
                    if(type.equals(AclScope.Type.DOMAIN)) {
                        acl.addAll(new Acl.DomainUser(scope.getValue()), new Acl.Role(role.getValue()));
                    }
                    if(type.equals(AclScope.Type.GROUP)) {
                        acl.addAll(new Acl.GroupUser(scope.getValue()), new Acl.Role(role.getValue()));
                    }
                }
                this.attributes().setAcl(acl);
            }
            catch(ServiceException e) {
                throw new IOException(e.getMessage());
            }
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    @Override
    public void writeAcl(Acl acl, boolean recursive) {
        for(Acl.User user : acl.keySet()) {
            if(!user.isValid()) {
                continue;
            }
            AclScope scope;
            if(user.isEmailIdentifier()) {
                scope = new AclScope(AclScope.Type.USER, user.getIdentifier());
            }
            else if(user.isGroupIdentifier()) {
                scope = new AclScope(AclScope.Type.GROUP, user.getIdentifier());
            }
            else if(user.isDomainIdentifier()) {
                scope = new AclScope(AclScope.Type.DOMAIN, user.getIdentifier());
            }
            else {
                scope = new AclScope(AclScope.Type.DEFAULT, user.getIdentifier());
            }
            for(Acl.Role role : acl.get(user)) {
                if(!role.isValid()) {
                    continue;
                }
                AclEntry entry = new AclEntry();
                entry.setScope(scope);
                entry.setRole(new AclRole(role.getName()));
                try {
                    try {
                        this.getSession().getClient().insert(this.getAclFeed(), entry);
                    }
                    catch(ServiceException e) {
                        throw new IOException(e.getMessage());
                    }
                }
                catch(IOException e) {
                    this.error("Cannot change permissions", e);
                }
            }
            this.attributes().clear(false, false, true, false);
        }
    }

    @Override
    public GDSession getSession() {
        return session;
    }

    @Override
    protected void download(BandwidthThrottle throttle, StreamListener listener, boolean check) {
        if(attributes().isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        this.getName()));

                MediaContent mc = new MediaContent();
                StringBuilder uri = new StringBuilder(this.getExportUri());
                final String type = this.getDocumentType();
                final GoogleAuthTokenFactory.UserToken token
                        = (GoogleAuthTokenFactory.UserToken) this.getSession().getClient().getAuthTokenFactory().getAuthToken();
                try {
                    if(type.equals(DOCUMENT_SPREADSHEET_TYPE)) {
                        // Authenticate against the Spreadsheets API to obtain an auth token
                        SpreadsheetService spreadsheet = new SpreadsheetService(this.getSession().getUserAgent());
                        final Credentials credentials = this.getSession().getHost().getCredentials();
                        spreadsheet.setUserCredentials(credentials.getUsername(), credentials.getPassword());
                        // Substitute the spreadsheets token for the docs token
                        this.getSession().getClient().setUserToken(
                                ((GoogleAuthTokenFactory.UserToken) spreadsheet.getAuthTokenFactory().getAuthToken()).getValue());
                    }
                    if(StringUtils.isNotEmpty(getExportFormat(type))) {
                        uri.append("&exportFormat=").append(getExportFormat(type));
                    }
                    mc.setUri(uri.toString());
                    MediaSource ms = this.getSession().getClient().getMedia(mc);
                    in = ms.getInputStream();
                    if(null == in) {
                        throw new IOException("Unable opening data stream");
                    }
                    out = this.getLocal().getOutputStream(this.status().isResume());
                    this.download(in, out, throttle, listener);
                }
                finally {
                    // Restore docs token for our DocList client
                    this.getSession().getClient().setUserToken(token.getValue());
                }
            }
            catch(IOException e) {
                this.error("Download failed", e);
            }
            catch(ServiceException e) {
                this.error("Download failed", e);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    /**
     * Google Apps Premier domains can upload files of arbitrary type. Uploading an arbitrary file is
     * the same as uploading documents (with and without metadata), except there is no
     * restriction on the file's Content-Type. Unlike normal document uploads, arbitrary
     * file uploads preserve their original format/extension, meaning there is no loss in
     * fidelity when the file is stored in Google Docs.
     * <p/>
     * By default, uploaded document files will be converted to a native Google Docs format.
     * For example, an .xls upload will create a Google Spreadsheet. To keep the file as an Excel
     * spreadsheet (and therefore upload the file as an arbitrary file), specify the convert=false
     * parameter to preserve the original format. The convert parameter is true by default for
     * document files. The parameter will be ignored for types that cannot be
     * converted (e.g. .exe, .mp3, .mov, etc.).
     *
     * @param throttle The bandwidth limit
     * @param listener The stream listener to notify about bytes received and sent
     * @param check    Check for open connection and open if needed before transfer
     */
    @Override
    protected void upload(BandwidthThrottle throttle, StreamListener listener, boolean check) {
        try {
            if(attributes().isFile()) {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                InputStream in = null;
                OutputStream out = null;
                try {
                    final String mime = this.getLocal().getMimeType();
                    final MediaStreamSource source = new MediaStreamSource(this.getLocal().getInputStream(), mime,
                            new DateTime(this.attributes().getModificationDate()),
                            this.attributes().getSize());
                    if(this.exists()) {
                        // First, fetch entry using the resourceId
                        URL url = new URL("https://docs.google.com/feeds/default/private/full/" + this.getResourceId());
                        final DocumentEntry updated = this.getSession().getClient().getEntry(url, DocumentEntry.class);
                        updated.setMediaSource(source);
                        updated.updateMedia(true);
                    }
                    else {
                        final MediaContent content = new MediaContent();
                        content.setMediaSource(source);
                        content.setMimeType(new ContentType(mime));
                        final DocumentEntry document = new DocumentEntry();
                        document.setContent(content);
                        document.setTitle(new PlainTextConstruct(this.getName()));

                        this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                                this.getName()));
                        status().setResume(false);

                        String feed = ((GDPath) this.getParent()).getFolderFeed().toExternalForm();
                        StringBuilder url = new StringBuilder(feed);
                        if(this.isOcrSupported()) {
                            // Image file type
                            url.append("?ocr=").append(Preferences.instance().getProperty("google.docs.upload.ocr"));
                        }
                        else if(this.isConversionSupported()) {
                            // Convertible to Google Docs file type
                            url.append("?convert=").append(Preferences.instance().getProperty("google.docs.upload.convert"));
                        }
                        Service.GDataRequest request = null;
                        try {
                            // Write as MIME multipart containing the entry and media.  Use the
                            // content type from the multipart since this contains auto-generated
                            // boundary attributes.
                            final MediaMultipart multipart = new MediaMultipart(document, document.getMediaSource());
                            request = this.getSession().getClient().createRequest(
                                    Service.GDataRequest.RequestType.INSERT, new URL(url.toString()),
                                    new ContentType(multipart.getContentType()));
                            out = request.getRequestStream();

                            final PipedOutputStream pipe = new PipedOutputStream();
                            in = new PipedInputStream(pipe);
                            ThreadPool.instance().execute(new Runnable() {
                                public void run() {
                                    try {
                                        multipart.writeTo(pipe);
                                        pipe.close();
                                    }
                                    catch(IOException e) {
                                        log.error(e.getMessage());
                                    }
                                    catch(MessagingException e) {
                                        log.error(e.getMessage());
                                    }
                                }
                            });
                            this.upload(out, in, throttle, listener);
                            // Parse response for HTTP error message.
                            try {
                                request.execute();
                            }
                            catch(ServiceException e) {
                                this.status().setComplete(false);
                                throw e;
                            }
                        }
                        catch(MessagingException e) {
                            throw new ServiceException(
                                    CoreErrorDomain.ERR.cantWriteMimeMultipart, e);
                        }
                        finally {
                            if(request != null) {
                                request.end();
                            }
                        }
                    }
                }
                finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
            }
        }
        catch(ServiceException e) {
            this.error("Upload failed", e);
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
    }

    /**
     * @return True for image formats supported by OCR
     */
    protected boolean isOcrSupported() {
        return this.getMimeType().endsWith("png") || this.getMimeType().endsWith("jpeg")
                || this.getMimeType().endsWith("gif");
    }

    /**
     * @return True if the document, spreadsheet or presentation format is recognized by Google Docs.
     */
    protected boolean isConversionSupported() {
        // The convert parameter will be ignored for types that cannot be converted. Therefore we
        // can always return true.
        return true;
    }

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            this.getSession().setWorkdir(this);

            childs.addAll(this.list(new DocumentQuery(this.getFolderFeed())));
        }
        catch(ServiceException e) {
            childs.attributes().setReadable(false);
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
        }
        return childs;
    }

    private void filter(List<DocumentListEntry> entries) {
        for(Iterator<DocumentListEntry> iter = entries.iterator(); iter.hasNext();) {
            DocumentListEntry entry = iter.next();
            if(!entry.getParentLinks().isEmpty()) {
                for(Link link : entry.getParentLinks()) {
                    if(!this.getName().equals(link.getTitle())) {
                        iter.remove();
                    }
                    break;
                }
            }
            else if(!this.isRoot()) {
                iter.remove();
            }
        }
    }

    /**
     * @param query
     * @return
     * @throws ServiceException
     * @throws IOException
     */
    private AttributedList<Path> list(DocumentQuery query) throws ServiceException, IOException {
        final AttributedList<Path> childs = new AttributedList<Path>();

        DocumentListFeed feed = new DocumentListFeed();
        DocumentListFeed pager = this.getSession().getClient().getFeed(query, DocumentListFeed.class);
        do {
            feed.getEntries().addAll(pager.getEntries());
            if(null == pager.getNextLink()) {
                break;
            }
            pager = this.getSession().getClient().getFeed(new URL(pager.getNextLink().getHref()), DocumentListFeed.class);
        }
        while(pager.getEntries().size() > 0);
        this.filter(feed.getEntries());
        for(final DocumentListEntry documentEntry : feed.getEntries()) {
            log.debug("Resource:" + documentEntry.getResourceId());
            final String type = documentEntry.getType();
            {
                GDPath path = new GDPath(this.getSession(), documentEntry.getTitle().getPlainText(),
                        DOCUMENT_FOLDER_TYPE.equals(type) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                path.setParent(this);
                path.setDocumentType(type);
                if(!documentEntry.getParentLinks().isEmpty()) {
                    path.setPath(documentEntry.getParentLinks().iterator().next().getTitle(), documentEntry.getTitle().getPlainText());
                }
                // Download URL
                path.setExportUri(((OutOfLineContent) documentEntry.getContent()).getUri());
                // Link to Google Docs Editor
                path.setDocumentUri(documentEntry.getDocumentLink().getHref());
                path.setResourceId(documentEntry.getResourceId());
                // Add unique document ID as checksum
                path.attributes().setChecksum(documentEntry.getDocId());
                if(null != documentEntry.getMediaSource()) {
                    path.attributes().setSize(documentEntry.getMediaSource().getContentLength());
                }
                if(documentEntry.getQuotaBytesUsed() > 0) {
                    path.attributes().setSize(documentEntry.getQuotaBytesUsed());
                }
                final DateTime lastViewed = documentEntry.getLastViewed();
                if(lastViewed != null) {
                    path.attributes().setAccessedDate(lastViewed.getValue());
                }
                LastModifiedBy lastModifiedBy = documentEntry.getLastModifiedBy();
                if(lastModifiedBy != null) {
                    path.attributes().setOwner(lastModifiedBy.getName());
                }
                final DateTime updated = documentEntry.getUpdated();
                if(updated != null) {
                    path.attributes().setModificationDate(updated.getValue());
                }
                // Add to listing
                childs.add(path);
                if(path.attributes().isFile()) {
                    // Fetch revisions
                    if(Preferences.instance().getBoolean("google.docs.revisions.enable")) {
                        try {
                            final List<RevisionEntry> revisions = this.getSession().getClient().getFeed(
                                    path.getRevisionsFeed(), RevisionFeed.class).getEntries();
                            Collections.sort(revisions, new Comparator<RevisionEntry>() {
                                public int compare(RevisionEntry o1, RevisionEntry o2) {
                                    return o1.getUpdated().compareTo(o2.getUpdated());
                                }
                            });
                            int i = 0;
                            for(RevisionEntry revisionEntry : revisions) {
                                GDPath revision = new GDPath(this.getSession(), documentEntry.getTitle().getPlainText(),
                                        DOCUMENT_FOLDER_TYPE.equals(type) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
                                revision.setParent(this);
                                revision.setDocumentType(type);
                                revision.setExportUri(((OutOfLineContent) revisionEntry.getContent()).getUri());

                                final long size = ((OutOfLineContent) revisionEntry.getContent()).getLength();
                                if(size > 0) {
                                    revision.attributes().setSize(size);
                                }
                                revision.attributes().setOwner(revisionEntry.getModifyingUser().getName());
                                revision.attributes().setModificationDate(revisionEntry.getUpdated().getValue());
                                // Versioning is enabled if non null.
                                revision.attributes().setVersionId(revisionEntry.getVersionId());
                                revision.attributes().setRevision(++i);
                                revision.attributes().setDuplicate(true);
                                // Add to listing
                                childs.add(revision);
                            }
                        }
                        catch(NotImplementedException e) {
                            log.error("No revisions available:" + e.getMessage());
                        }
                    }
                }
            }
        }
        return childs;
    }

    @Override
    public String getMimeType() {
        if(attributes().isFile()) {
            final String exportFormat = getExportFormat(this.getDocumentType());
            if(StringUtils.isNotEmpty(exportFormat)) {
                return getMimeType(exportFormat);
            }
        }
        return super.getMimeType();
    }

    @Override
    public String getExtension() {
        if(attributes().isFile()) {
            final String exportFormat = getExportFormat(this.getDocumentType());
            if(StringUtils.isNotEmpty(exportFormat)) {
                return exportFormat;
            }
        }
        return super.getExtension();
    }

    @Override
    public String getName() {
        if(attributes().isFile()) {
            final String exportFormat = getExportFormat(this.getDocumentType());
            if(StringUtils.isNotEmpty(exportFormat)) {
                if(!super.getName().endsWith(exportFormat)) {
                    return super.getName() + "." + exportFormat;
                }
            }
        }
        return super.getName();
    }

    /**
     * @param type The document type
     * @return
     */
    protected static String getExportFormat(String type) {
        if(type.equals(DOCUMENT_TEXT_TYPE)) {
            return Preferences.instance().getProperty("google.docs.export.document");
        }
        if(type.equals(DOCUMENT_PRESENTATION_TYPE)) {
            return Preferences.instance().getProperty("google.docs.export.presentation");
        }
        if(type.equals(DOCUMENT_SPREADSHEET_TYPE)) {
            return Preferences.instance().getProperty("google.docs.export.spreadsheet");
        }
        if(type.equals(DOCUMENT_FILE_TYPE)) {
            // For files not converted to Google Docs.
            // DOCUMENT_FILE_TYPE
            log.debug("No output format conversion for document type:" + type);
            return null;
        }
        log.warn("Unknown document type:" + type);
        return null;
    }

    @Override
    public void mkdir() {
        if(this.attributes().isDirectory()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                        this.getName()));

                DocumentListEntry folder = new FolderEntry();
                folder.setTitle(new PlainTextConstruct(this.getName()));
                try {
                    this.getSession().getClient().insert(((GDPath) this.getParent()).getFolderFeed(), folder);
                }
                catch(ServiceException e) {
                    throw new IOException(e.getMessage());
                }
                this.cache().put(this.getReference(), AttributedList.<Path>emptyList());
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create folder", e);
            }
        }
    }

    @Override
    public void readUnixPermission() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeUnixPermission(Permission perm, boolean recursive) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTimestamp(long millis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        try {
            try {
                if(this.attributes().isDuplicate()) {
                    log.warn("Cannot delete revision " + this.attributes().getRevision());
                    return;
                }
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                session.getClient().delete(
                        new URL("https://docs.google.com/feeds/default/private/full/" + this.getResourceId()), "*");
            }
            catch(ServiceException e) {
                throw new IOException(e.getMessage());
            }
            catch(MalformedURLException e) {
                throw new IOException(e.getMessage());
            }
            // The directory listing is no more current
            this.getParent().invalidate();
        }
        catch(IOException e) {
            if(this.attributes().isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes().isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    @Override
    public void rename(AbstractPath renamed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void touch() {
        if(this.attributes().isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                DocumentListEntry file = new DocumentEntry();
                file.setTitle(new PlainTextConstruct(this.getName()));
                try {
                    this.getSession().getClient().insert(((GDPath) this.getParent()).getFolderFeed(), file);
                }
                catch(ServiceException e) {
                    throw new IOException(e.getMessage());
                }
                // The directory listing is no more current
                this.getParent().invalidate();
            }
            catch(IOException e) {
                this.error("Cannot create file", e);
            }
        }
    }

    @Override
    public String toHttpURL() {
        return this.getDocumentUri();
    }
}
