package ch.cyberduck.core.g;

/*
 *  Copyright (c) 2010 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.serializer.Deserializer;
import ch.cyberduck.core.serializer.Serializer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gdata.client.DocumentQuery;
import com.google.gdata.data.DateTime;
import com.google.gdata.data.Link;
import com.google.gdata.data.MediaContent;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.docs.DocumentEntry;
import com.google.gdata.data.docs.DocumentListEntry;
import com.google.gdata.data.docs.DocumentListFeed;
import com.google.gdata.data.docs.FolderEntry;
import com.google.gdata.data.extensions.LastModifiedBy;
import com.google.gdata.data.media.MediaSource;
import com.google.gdata.data.media.MediaStreamSource;
import com.google.gdata.util.ContentType;
import com.google.gdata.util.ServiceException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

public class GPath extends Path {
    private static Logger log = Logger.getLogger(GPath.class);

    static {
        PathFactory.addFactory(Protocol.GDOCS, new Factory());
    }

    private static class Factory extends PathFactory<GSession> {
        @Override
        protected Path create(GSession session, String path, int type) {
            return new GPath(session, path, type);
        }

        @Override
        protected Path create(GSession session, String parent, String name, int type) {
            return new GPath(session, parent, name, type);
        }

        @Override
        protected Path create(GSession session, String path, Local file) {
            return new GPath(session, path, file);
        }

        @Override
        protected <T> Path create(GSession session, T dict) {
            return new GPath(session, dict);
        }
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
        String documentUriObj = dict.stringForKey("DocumentUri");
        if(documentUriObj != null) {
            this.setDocumentUri(documentUriObj);
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
        if(documentUri != null) {
            dict.setStringForKey(documentUri, "DocumentUri");
        }
        return super.<S>getAsDictionary(dict);
    }

    private static final String DOCUMENT_FOLDER_TYPE = "folder";
    private static final String DOCUMENT_TEXT_TYPE = "document";
    private static final String DOCUMENT_PRESENTATION_TYPE = "presentation";
    private static final String DOCUMENT_SPREADSHEET_TYPE = "spreadsheet";

    private final GSession session;

    protected GPath(GSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected GPath(GSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected GPath(GSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected <T> GPath(GSession s, T dict) {
        super(dict);
        this.session = s;
    }

    /**
     * Unique identifier
     */
    private String exportUri;

    public String getExportUri() {
        if(null == exportUri) {
            return this.getAbsolute();
        }
        return exportUri;
    }

    public void setExportUri(String exportUri) {
        this.exportUri = exportUri;
    }

    private String documentType;

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    private String resourceId;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    private String documentUri;

    public String getDocumentUri() {
        return documentUri;
    }

    public void setDocumentUri(String documentUri) {
        this.documentUri = documentUri;
    }

    @Override
    public void readSize() {

    }

    @Override
    public void readTimestamp() {

    }

    @Override
    public void readPermission() {

    }

    @Override
    public GSession getSession() throws ConnectionCanceledException {
        return session;
    }

    @Override
    public void download(BandwidthThrottle throttle, StreamListener listener, boolean check) {
        if(attributes.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                MediaContent mc = new MediaContent();
                StringBuilder uri = new StringBuilder(this.getExportUri());
                if(StringUtils.isNotEmpty(getExportFormat(this.getDocumentType()))) {
                    uri.append("&exportFormat=").append(getExportFormat(this.getDocumentType()));
                }
                mc.setUri(uri.toString());
                MediaSource ms = session.getClient().getMedia(mc);
                in = ms.getInputStream();
                if(null == in) {
                    throw new IOException("Unable opening data stream");
                }
                out = new Local.OutputStream(this.getLocal(), this.getStatus().isResume());

                this.download(in, out, throttle, listener);
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
        if(attributes.isDirectory()) {
            this.getLocal().mkdir(true);
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
     * @param p        The permission to set after uploading or null
     * @param check    Check for open connection and open if needed before transfer
     */
    @Override
    protected void upload(BandwidthThrottle throttle, StreamListener listener, Permission p, boolean check) {
        try {
            if(check) {
                this.getSession().check();
            }
            if(attributes.isFile()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                DocumentListEntry document = new DocumentEntry();
                File upload = new File(this.getLocal().getAbsolute());
                InputStream in = null;
                try {
                    in = new Local.InputStream(this.getLocal());
                    MediaContent content = new MediaContent();
                    final String mime = this.getLocal().getMimeType();
                    content.setMediaSource(new MediaStreamSource(in, mime,
                            new DateTime(this.getLocal().attributes.getModificationDate()),
                            this.getLocal().attributes.getSize()));
                    content.setMimeType(new ContentType(mime));
                    document.setContent(content);
                    document.setTitle(new PlainTextConstruct(this.getName()));

                    this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                            this.getName()));
                    getStatus().setCurrent(0);
                    session.getClient().insert(new URL("https://docs.google.com/feeds/default/private/full/"), document);

                    getStatus().setCurrent(this.getLocal().attributes.getSize());
                    getStatus().setComplete(true);
                }
                finally {
                    IOUtils.closeQuietly(in);
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

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            this.getSession().setWorkdir(this);
            childs.addAll(this.list(
                    new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full"))
            ));
            if(this.isRoot()) {
                childs.addAll(this.list(
                        new DocumentQuery(new URL("https://docs.google.com/feeds/default/private/full/-/folder"))
                ));
            }
        }
        catch(ServiceException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
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
        DocumentListFeed pager = session.getClient().getFeed(query, DocumentListFeed.class);
        do {
            feed.getEntries().addAll(pager.getEntries());
            if(null == pager.getNextLink()) {
                break;
            }
            pager = session.getClient().getFeed(new URL(pager.getNextLink().getHref()), DocumentListFeed.class);
        }
        while(pager.getEntries().size() > 0);
        this.filter(feed.getEntries());
        for(final DocumentListEntry entry : feed.getEntries()) {
            log.debug("Resource:" + entry.getResourceId());
            final StringBuilder title = new StringBuilder(entry.getTitle().getPlainText());
            final String type = entry.getType();
            GPath p = new GPath(this.getSession(),
                    title.toString(),
                    DOCUMENT_FOLDER_TYPE.equals(type) ? Path.DIRECTORY_TYPE : Path.FILE_TYPE);
            p.setParent(this);
            p.setDocumentType(type);
            if(!entry.getParentLinks().isEmpty()) {
                p.setPath(entry.getParentLinks().iterator().next().getTitle(), title.toString());
            }

            p.setExportUri(((MediaContent) entry.getContent()).getUri());
            p.setDocumentUri(entry.getDocumentLink().getHref());
            p.setResourceId(entry.getResourceId());

            p.attributes.setChecksum(entry.getDocId());
            if(null != entry.getMediaSource()) {
                p.attributes.setSize(entry.getMediaSource().getContentLength());
            }

            final DateTime lastViewed = entry.getLastViewed();
            if(lastViewed != null) {
                p.attributes.setAccessedDate(lastViewed.getValue());
            }
            LastModifiedBy lastModifiedBy = entry.getLastModifiedBy();
            if(lastModifiedBy != null) {
                p.attributes.setOwner(lastModifiedBy.getName());
            }
            final DateTime updated = entry.getUpdated();
            if(updated != null) {
                p.attributes.setModificationDate(updated.getValue());
            }

            childs.add(p);
        }
        return childs;
    }

    @Override
    public String getMimeType() {
        if(attributes.isFile()) {
            return getMimeType(getExportFormat(this.getDocumentType()));
        }
        return super.getMimeType();
    }

    @Override
    public String getExtension() {
        if(attributes.isFile()) {
            final String exportFormat = getExportFormat(this.getDocumentType());
            if(StringUtils.isNotEmpty(exportFormat)) {
                return exportFormat;
            }
        }
        return super.getExtension();
    }

    @Override
    public String getName() {
        if(attributes.isFile()) {
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
        log.debug("No output format conversion for document type:" + type);
        return null;
    }

    @Override
    public void mkdir(boolean recursive) {
        try {
            DocumentListEntry folder = new FolderEntry();
            folder.setTitle(new PlainTextConstruct(this.getName()));
            URL feedUrl = new URL("https://docs.google.com/feeds/default/private/full/");
            try {
                session.getClient().insert(feedUrl, folder);
            }
            catch(ServiceException e) {
                throw new IOException(e.getMessage());
            }
        }
        catch(IOException e) {
            this.error("Cannot create folder", e);
        }
    }

    @Override
    public boolean isWritePermissionsSupported() {
        return false;
    }

    @Override
    public void writePermissions(Permission perm, boolean recursive) {
        ;
    }

    @Override
    public boolean isWriteModificationDateSupported() {
        return false;
    }

    @Override
    public void writeModificationDate(long millis) {
        ;
    }

    @Override
    public void delete() {
        try {
            try {
                session.getClient().delete(
                        new URL("https://docs.google.com/feeds/default/private/full/" + this.getResourceId()), "*");
            }
            catch(ServiceException e) {
                throw new IOException(e.getMessage());
            }
            catch(MalformedURLException e) {
                throw new IOException(e.getMessage());
            }
        }
        catch(IOException e) {
            if(this.attributes.isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes.isDirectory()) {
                this.error("Cannot delete folder", e);
            }
        }
    }

    @Override
    public boolean isRenameSupported() {
        return false;
    }

    @Override
    public void rename(AbstractPath renamed) {

    }

    @Override
    public String toHttpURL() {
        return this.getDocumentUri();
    }
}
