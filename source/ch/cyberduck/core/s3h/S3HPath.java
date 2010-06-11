package ch.cyberduck.core.s3h;

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
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;

import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.S3ObjectsChunk;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.model.*;
import org.jets3t.service.utils.ObjectUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @version $Id$
 */
public class S3HPath extends CloudPath {
    private static Logger log = Logger.getLogger(S3HPath.class);

    static {
        PathFactory.addFactory(Protocol.S3, new Factory());
    }

    private static class Factory extends PathFactory<S3HSession> {
        @Override
        protected Path create(S3HSession session, String path, int type) {
            return new S3HPath(session, path, type);
        }

        @Override
        protected Path create(S3HSession session, String parent, String name, int type) {
            return new S3HPath(session, parent, name, type);
        }

        @Override
        protected Path create(S3HSession session, String path, Local file) {
            return new S3HPath(session, path, file);
        }

        @Override
        protected <T> Path create(S3HSession session, T dict) {
            return new S3HPath(session, dict);
        }
    }

    private final S3HSession session;

    protected S3HPath(S3HSession s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected S3HPath(S3HSession s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected S3HPath(S3HSession s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected <T> S3HPath(S3HSession s, T dict) {
        super(dict);
        this.session = s;
    }

    @Override
    public S3HSession getSession() throws ConnectionCanceledException {
        if(null == session) {
            throw new ConnectionCanceledException();
        }
        return session;
    }

    @Override
    public boolean exists() {
        if(attributes.isDirectory()) {
            if(this.isContainer()) {
                try {
                    return this.getSession().getClient().isBucketAccessible(this.getContainerName());
                }
                catch(S3ServiceException e) {
                    log.error(e.getMessage());
                    return false;
                }
                catch(ConnectionCanceledException e) {
                    log.error(e.getMessage());
                    return false;
                }
            }
            return !this.childs().isEmpty();
        }
        return super.exists();
    }

    protected S3Object _details;

    /**
     * @return
     * @throws S3ServiceException
     */
    protected S3Object getDetails() throws IOException, S3ServiceException {
        if(null == _details || !_details.isMetadataComplete()) {
            try {
                _details = this.getSession().getClient().getObjectDetails(
                        this.getSession().getBucket(this.getContainerName()), this.getKey());
                if(null == _details.getAcl()) {
                    try {
                        final AccessControlList acl = this.getSession().getClient().getObjectAcl(
                                this.getSession().getBucket(this.getContainerName()), this.getKey());
                        _details.setAcl(acl);
                    }
                    catch(S3ServiceException e) {
                        // This method can be performed by anonymous services, but can only succeed if the
                        // object's existing ACL already allows read access by the anonymous user.
                        log.error(e.getXmlMessage());
                    }
                }
            }
            catch(S3ServiceException e) {
                // Anonymous services can only get a publicly-readable object's details
                log.error(e.getXmlMessage());
            }
        }
        if(null == _details) {
            log.warn("Cannot read object details.");
            return new S3Object(this.getSession().getBucket(this.getContainerName()), this.getKey());
        }
        return _details;
    }

    /**
     * Versioning support. List all available version markers for this object.
     *
     * @return
     */
    public List<S3Version> getVersions() {
        if(this.attributes.isFile()) {
            try {
                List<S3Version> versions = new ArrayList<S3Version>();
                for(BaseVersionOrDeleteMarker marker : this.getSession().getClient().getObjectVersions(this.getContainerName(), this.getKey())) {
                    if(marker instanceof S3Version) {
                        versions.add((S3Version) marker);
                    }
                }
                return versions;
            }
            catch(S3ServiceException e) {
                this.error("Listing directory failed", e);
            }
            catch(IOException e) {
                this.error("Listing directory failed", e);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Versioning support. Download a specific version of this object.
     *
     * @param versionId
     */
    public void download(String versionId) {
        if(this.attributes.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(StringUtils.isBlank(versionId)) {
                    log.error("Cannot revert to version ID that is null");
                    return;
                }
                final S3Object object = this.getSession().getClient().getVersionedObject(versionId,
                        this.getContainerName(), this.getKey());

                in = object.getDataInputStream();
                out = this.getLocal().getOutputStream(this.getStatus().isResume());
                this.download(in, out, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                        new AbstractStreamListener());
            }
            catch(S3ServiceException e) {
                this.error("Download failed", e);
            }
            catch(IOException e) {
                this.error("Download failed", e);
            }
            finally {
                IOUtils.closeQuietly(in);
                IOUtils.closeQuietly(out);
            }
        }
    }

    /**
     * Versioning support. Copy a previous version of the object into the same bucket.
     * The copied object becomes the latest version of that object and all object versions are preserved.
     *
     * @param versionId
     */
    public void revert(String versionId) {
        if(this.attributes.isFile()) {
            try {
                if(StringUtils.isBlank(versionId)) {
                    log.error("Cannot revert to version ID that is null");
                    return;
                }
                final S3Object destination = new S3Object(this.getKey());
                // Keep same storage class
                destination.setStorageClass(this.attributes.getStorageClass());
                // Apply non standard ACL
                destination.setAcl(this.getDetails().getAcl());
                this.getSession().getClient().copyVersionedObject(versionId,
                        this.getContainerName(), this.getKey(), this.getContainerName(), destination, false);
            }
            catch(S3ServiceException e) {
                this.error("Cannot revert file", e);
            }
            catch(IOException e) {
                this.error("Cannot revert file", e);
            }
        }
    }

    /**
     * Versioning support. Delete a version using MFA if enabled.
     *
     * @param versionId
     */
    public void delete(String versionId) {
        if(this.attributes.isFile()) {
            try {
                if(StringUtils.isBlank(versionId)) {
                    log.error("Cannot revert to version ID that is null");
                    return;
                }
                if(this.getSession().isMFA(this.getContainerName())) {
                    this.getSession().getHost().getCredentials().setUsername(
                            Preferences.instance().getProperty("s3.mfa.serialnumber")
                    );
                    this.getSession().getHost().getCredentials().setUseKeychain(false);
                    // Prompt for MFA credentials.
                    this.getSession().getLoginController().prompt(
                            this.getSession().getHost(),
                            Locale.localizedString("Provide additional login credentials", "Credentials"),
                            Locale.localizedString("Multi-Factor Authentication", "S3"));
                    this.getSession().getClient().deleteVersionedObjectWithMFA(versionId,
                            this.getSession().getHost().getCredentials().getUsername(),
                            this.getSession().getHost().getCredentials().getPassword(),
                            this.getContainerName(), this.getKey());
                }
                else {
                    this.getSession().getClient().deleteVersionedObject(versionId, this.getContainerName(),
                            this.getKey());
                }
            }
            catch(S3ServiceException e) {
                this.error("Cannot delete file", e);
            }
            catch(IOException e) {
                this.error("Cannot delete file", e);
            }
        }
    }

    public AccessControlList readAcl() {
        try {
            return this.getDetails().getAcl();
        }
        catch(S3ServiceException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
        return null;
    }

    /**
     * Format to RFC 1123 timestamp
     * Expires: Thu, 01 Dec 1994 16:00:00 GMT
     */
    private SimpleDateFormat rfc1123 =
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.ENGLISH);

    {
        rfc1123.setTimeZone(TimeZone.getDefault());
    }

    private static final String METADATA_HEADER_EXPIRES = "Expires";

    /**
     * Implements http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.21
     *
     * @param expiration
     */
    public void setExpiration(final Date expiration) {
        try {
            this.getSession().check();
            // You can also copy an object and update its metadata at the same time. Perform a
            // copy-in-place  (with the same bucket and object names for source and destination)
            // to update an object's metadata while leaving the object's data unchanged.
            final S3Object target = this.getDetails();
            target.addMetadata(METADATA_HEADER_EXPIRES, rfc1123.format(expiration));
            this.getSession().getClient().updateObjectMetadata(this.getContainerName(), target);
        }
        catch(S3ServiceException e) {
            this.error("Cannot write file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot write file attributes", e);
        }
    }

    public static final String METADATA_HEADER_CACHE_CONTROL = "Cache-Control";

    /**
     * Implements http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.9
     *
     * @param maxage Timespan in seconds from when the file is requested
     */
    public void setCacheControl(final String maxage) {
        try {
            this.getSession().check();
            // You can also copy an object and update its metadata at the same time. Perform a
            // copy-in-place  (with the same bucket and object nexames for source and destination)
            // to update an object's metadata while leaving the object's data unchanged.
            final S3Object target = this.getDetails();
            if(StringUtils.isEmpty(maxage)) {
                target.removeMetadata(METADATA_HEADER_CACHE_CONTROL);
            }
            else {
                target.addMetadata(METADATA_HEADER_CACHE_CONTROL, maxage);
            }
            this.getSession().getClient().updateObjectMetadata(this.getContainerName(), target);
        }
        catch(S3ServiceException e) {
            this.error("Cannot write file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot write file attributes", e);
        }
    }

    @Override
    public Map<String, String> readMetadata() {
        if(attributes.isFile()) {
            try {
                this.getSession().check();
                final S3Object target = this.getDetails();
                return target.getModifiableMetadata();
            }
            catch(S3ServiceException e) {
                this.error("Cannot read file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public void writeMetadata(Map<String, String> meta) {
        if(attributes.isFile()) {
            try {
                this.getSession().check();
                final S3Object target = this.getDetails();
                target.replaceAllMetadata(meta);
                this.getSession().getClient().updateObjectMetadata(this.getContainerName(), target);
                target.setMetadataComplete(false);
            }
            catch(S3ServiceException e) {
                this.error("Cannot write file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot write file attributes", e);
            }
        }
    }

    @Override
    public void readChecksum() {
        if(attributes.isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        this.getName()));

                final S3Object details = this.getDetails();
                if(StringUtils.isNotEmpty(details.getMd5HashAsHex())) {
                    attributes.setChecksum(details.getMd5HashAsHex());
                }
                else {
                    log.debug("Setting ETag Header as checksum for:" + this.toString());
                    attributes.setChecksum(details.getETag());
                }
            }
            catch(S3ServiceException e) {
                this.error("Cannot read file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    @Override
    public void readSize() {
        if(attributes.isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Getting size of {0}", "Status"),
                        this.getName()));

                final S3Object details = this.getDetails();
                attributes.setSize(details.getContentLength());
            }
            catch(S3ServiceException e) {
                this.error("Cannot read file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    @Override
    public void readTimestamp() {
        if(attributes.isFile()) {
            try {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Getting timestamp of {0}", "Status"),
                        this.getName()));

                final S3Object details = this.getDetails();
                attributes.setModificationDate(details.getLastModifiedDate().getTime());
            }
            catch(S3ServiceException e) {
                this.error("Cannot read file attributes", e);
            }
            catch(IOException e) {
                this.error("Cannot read file attributes", e);
            }
        }
    }

    private static final Permission DEFAULT_FOLDER_PERMISSION;

    static {
        boolean[][] access = new boolean[3][3];
        access[Permission.OWNER][Permission.READ] = true;
        access[Permission.OWNER][Permission.WRITE] = true;
        access[Permission.OWNER][Permission.EXECUTE] = true;
        DEFAULT_FOLDER_PERMISSION = new Permission(access);
    }

    @Override
    public void readPermission() {
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Getting permission of {0}", "Status"),
                    this.getName()));
            AccessControlList acl = null;
            if(this.isContainer()) {
                final Credentials credentials = this.getSession().getHost().getCredentials();
                if(null == this.getSession().getBucket(this.getContainerName()).getAcl() && !credentials.isAnonymousLogin()) {
                    this.getSession().getBucket(this.getContainerName()).setAcl(
                            this.getSession().getClient().getBucketAcl(this.getSession().getBucket(this.getContainerName())));
                }
                acl = this.getSession().getBucket(this.getContainerName()).getAcl();
            }
            else if(attributes.isFile()) {
                acl = this.getDetails().getAcl();
            }
            if(null == acl) {
                if(attributes.isDirectory()) {
                    attributes.setPermission(DEFAULT_FOLDER_PERMISSION);
                }
            }
            else {
                attributes.setPermission(this.readPermissions(acl.getGrants()));
            }
        }
        catch(S3ServiceException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
    }

    /**
     * @param grants
     * @return
     */
    private Permission readPermissions(Set<GrantAndPermission> grants) throws IOException, S3ServiceException {
        boolean[][] p = new boolean[3][3];
        final S3Owner owner = this.getSession().getBucket(this.getContainerName()).getOwner();
        for(GrantAndPermission grant : grants) {
            final org.jets3t.service.acl.Permission access = grant.getPermission();
            if(grant.getGrantee().equals(GroupGrantee.ALL_USERS)) {
                if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_READ)) {
                    p[Permission.OTHER][Permission.READ] = true;
                }
                else if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_WRITE)) {
                    p[Permission.OTHER][Permission.WRITE] = true;
                }
                else if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL)) {
                    p[Permission.OTHER][Permission.EXECUTE] = true;
                }
            }
            if(null != owner) {
                if(grant.getGrantee().equals(new CanonicalGrantee(owner.getId()))) {
                    if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_READ)) {
                        p[Permission.OWNER][Permission.READ] = true;
                    }
                    else if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_WRITE)) {
                        p[Permission.OWNER][Permission.WRITE] = true;
                    }
                    else if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL)) {
                        p[Permission.OWNER][Permission.EXECUTE] = true;
                    }
                }
            }
        }
        return new Permission(p);
    }

    @Override
    public void download(BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        if(attributes.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    this.getSession().check();
                }
                this.getSession().message(MessageFormat.format(Locale.localizedString("Downloading {0}", "Status"),
                        this.getName()));

                final S3Bucket bucket = this.getSession().getBucket(this.getContainerName());

                in = this.getSession().getClient().getObject(bucket, this.getKey(), null, null, null, null,
                        this.getStatus().isResume() ? this.getStatus().getCurrent() : null, null).getDataInputStream();
                out = this.getLocal().getOutputStream(this.getStatus().isResume());
                this.download(in, out, throttle, listener);
            }
            catch(S3ServiceException e) {
                this.error("Download failed", e);
            }
            catch(IOException e) {
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

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener,
                       final Permission p, final boolean check) {
        try {
            if(check) {
                this.getSession().check();
            }
            if(attributes.isFile()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Compute MD5 hash of {0}", "Status"),
                        this.getName()));
                S3Object object;
                try {
                    object = ObjectUtils.createObjectForUpload(this.getKey(),
                            new File(this.getLocal().getAbsolute()),
                            null, //no encryption
                            false); //no gzip)
                }
                catch(Exception e) {
                    throw new IOException(e.getMessage());
                }
                AccessControlList acl = AccessControlList.REST_CANNED_PRIVATE;
                if(null != p) {
                    if(p.getOtherPermissions()[Permission.READ]) {
                        acl = AccessControlList.REST_CANNED_PUBLIC_READ;
                    }
                    if(p.getOtherPermissions()[Permission.WRITE]) {
                        acl = AccessControlList.REST_CANNED_PUBLIC_READ_WRITE;
                    }
                }
                object.setAcl(acl);
                object.setStorageClass(Preferences.instance().getProperty("s3.storage.class"));

                this.getSession().message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                final Status status = this.getStatus();
                // No Content-Range support
                status.setResume(false);

                final InputStream in = object.getDataInputStream();
                try {
                    this.getSession().getClient().pubObjectWithRequestEntityImpl(
                            this.getContainerName(), object, new InputStreamRequestEntity(in,
                                    this.getLocal().attributes.getSize() - status.getCurrent(),
                                    this.getLocal().getMimeType()) {

                                @Override
                                public void writeRequest(OutputStream out) throws IOException {
                                    S3HPath.this.upload(out, in, throttle, listener);
                                }
                            });
                }
                finally {
                    IOUtils.closeQuietly(in);
                }
            }
            if(attributes.isDirectory()) {
                this.mkdir();
            }
        }
        catch(S3ServiceException e) {
            this.error("Upload failed", e);
        }
        catch(IOException e) {
            this.error("Upload failed", e);
        }
    }

    private static final int BUCKET_LIST_CHUNKING_SIZE = 1000;

    @Override
    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            if(this.isRoot()) {
                // List all buckets
                for(S3Bucket bucket : this.getSession().getBuckets(true)) {
                    Path p = PathFactory.createPath(this.getSession(), this.getAbsolute(), bucket.getName(),
                            Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                    if(null != bucket.getOwner()) {
                        p.attributes.setOwner(bucket.getOwner().getDisplayName());
                    }
                    if(null != bucket.getCreationDate()) {
                        p.attributes.setCreationDate(bucket.getCreationDate().getTime());
                    }
                    childs.add(p);
                }
            }
            else {
                final S3Bucket bucket = this.getSession().getBucket(this.getContainerName());
                // Keys can be listed by prefix. By choosing a common prefix
                // for the names of related keys and marking these keys with
                // a special character that delimits hierarchy, you can use the list
                // operation to select and browse keys hierarchically
                String prefix = "";
                if(!this.isContainer()) {
                    // estricts the response to only contain results that begin with the
                    // specified prefix. If you omit this optional argument, the value
                    // of Prefix for your query will be the empty string.
                    // In other words, the results will be not be restricted by prefix.
                    prefix = this.getKey() + Path.DELIMITER;
                }
                // If this optional, Unicode string parameter is included with your request,
                // then keys that contain the same string between the prefix and the first
                // occurrence of the delimiter will be rolled up into a single result
                // element in the CommonPrefixes collection. These rolled-up keys are
                // not returned elsewhere in the response.
                final String delimiter = Path.DELIMITER;
                // Null if listing is complete
                String priorLastKey = null;
                do {
                    // Read directory listing in chunks. List results are always returned
                    // in lexicographic (alphabetical) order.
                    S3ObjectsChunk chunk = this.getSession().getClient().listObjectsChunked(
                            bucket.getName(), prefix, delimiter,
                            BUCKET_LIST_CHUNKING_SIZE, priorLastKey);

                    final S3Object[] objects = chunk.getObjects();
                    for(S3Object object : objects) {
                        final S3HPath path = (S3HPath) PathFactory.createPath(this.getSession(), bucket.getName(), object.getKey(), Path.FILE_TYPE);
                        path.setParent(this);
                        if(path.getAbsolute().equals(this.getAbsolute())) {
                            // #Workaround for key that end with /. Refer to #3347.
                            continue;
                        }
                        path.attributes.setSize(object.getContentLength());
                        path.attributes.setModificationDate(object.getLastModifiedDate().getTime());
                        if(null != bucket.getOwner()) {
                            path.attributes.setOwner(bucket.getOwner().getDisplayName());
                            path.attributes.setGroup(bucket.getOwner().getId());
                        }
                        if(0 == object.getContentLength()) {
                            final S3Object details = path.getDetails();
                            if(MIMETYPE_DIRECTORY.equals(details.getContentType())) {
                                path.attributes.setType(Path.DIRECTORY_TYPE);
                            }
                        }
                        path.attributes.setStorageClass(object.getStorageClass());
                        // Versioning is enabled if non null.
                        path.attributes.setVersionId(object.getVersionId());
                        childs.add(path);
                    }

                    final String[] prefixes = chunk.getCommonPrefixes();
                    for(String common : prefixes) {
                        if(common.equals(Path.DELIMITER)) {
                            log.warn("Skipping prefix " + common);
                            continue;
                        }
                        final Path p = PathFactory.createPath(this.getSession(), bucket.getName(), common, Path.DIRECTORY_TYPE);
                        p.setParent(this);
                        if(childs.contains(p)) {
                            continue;
                        }
                        if(null != bucket.getOwner()) {
                            p.attributes.setOwner(bucket.getOwner().getDisplayName());
                            p.attributes.setGroup(bucket.getOwner().getId());
                        }
                        p.attributes.setPermission(DEFAULT_FOLDER_PERMISSION);
                        childs.add(p);
                    }

                    priorLastKey = chunk.getPriorLastKey();
                }
                while(priorLastKey != null && !getStatus().isCanceled());
            }
            this.getSession().setWorkdir(this);
        }
        catch(S3ServiceException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
        }
        catch(IOException e) {
            childs.attributes().setReadable(false);
            this.error("Listing directory failed", e);
        }
        return childs;
    }

    /**
     * Mimetype used to indicate that an S3 object actually represents a directory on the local file system.
     */
    private final static String MIMETYPE_DIRECTORY = "application/x-directory";

    @Override
    public void mkdir() {
        log.debug("mkdir:" + this.getName());
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Making directory {0}", "Status"),
                    this.getName()));

            if(this.isContainer()) {
                // Create bucket
                if(!ServiceUtils.isBucketNameValidDNSName(this.getName())) {
                    this.error("Bucket name is not DNS compatible");
                    return;
                }
                this.getSession().getClient().createBucket(this.getName(),
                        Preferences.instance().getProperty("s3.location"));
                this.getSession().getBuckets(true);
            }
            else {
                final S3Bucket bucket = this.getSession().getBucket(this.getContainerName());
                S3Object object = new S3Object(bucket, this.getKey());
                // Set object explicitly to private access by default.
                object.setAcl(AccessControlList.REST_CANNED_PRIVATE);
                object.setContentLength(0);
                object.setContentType(MIMETYPE_DIRECTORY);
                this.getSession().getClient().putObject(bucket, object);
            }
        }
        catch(S3ServiceException e) {
            this.error("Cannot create folder", e);
        }
        catch(IOException e) {
            this.error("Cannot create folder", e);
        }
    }

    @Override
    public void mkdir(boolean recursive) {
        this.mkdir();
    }

    @Override
    public void writePermissions(Permission perm, boolean recursive) {
        log.debug("writePermissions:" + perm);
        try {
            this.getSession().check();
            this.getSession().message(MessageFormat.format(Locale.localizedString("Changing permission of {0} to {1}", "Status"),
                    this.getName(), perm.getOctalString()));

            AccessControlList acl = null;
            final S3Bucket bucket = this.getSession().getBucket(this.getContainerName());
            if(this.isContainer()) {
                acl = this.getSession().getClient().getBucketAcl(bucket);
            }
            else if(attributes.isFile()) {
                acl = this.getSession().getClient().getObjectAcl(bucket, this.getKey());
            }
            if(acl != null) {
                this.updateAccessControlList(perm, acl);
                this.writePermissions(acl);
            }
            if(attributes.isDirectory()) {
                if(recursive) {
                    for(AbstractPath child : this.childs()) {
                        if(!this.getSession().isConnected()) {
                            break;
                        }
                        child.writePermissions(perm, recursive);
                    }
                }
            }
            attributes.setPermission(perm);
        }
        catch(S3ServiceException e) {
            this.error("Cannot change permissions", e);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    public void writePermissions(AccessControlList acl) {
        try {
            if(this.isContainer()) {
                this.getSession().getClient().putBucketAcl(this.getContainerName(), acl);
            }
            else if(attributes.isFile()) {
                this.getSession().getClient().putObjectAcl(this.getContainerName(), this.getKey(), acl);
            }
        }
        catch(S3ServiceException e) {
            this.error("Cannot change permissions", e);
        }
        catch(IOException e) {
            this.error("Cannot change permissions", e);
        }
    }

    /**
     * @param perm The permissions to apply
     * @param acl  The ACL to update
     */
    protected void updateAccessControlList(Permission perm, AccessControlList acl) {
        final CanonicalGrantee owner = new CanonicalGrantee(acl.getOwner().getId());
        // Even the owner is subject to the ACL. For example, if an owner does not have READ access
        // to an object, the owner cannot read that object. However, the owner of
        // an object always has write access to the access control policy (WRITE_ACP)
        // and can change the ACL to read the object.
        acl.revokeAllPermissions(owner);
        if(perm.getOwnerPermissions()[Permission.READ]) {
            acl.grantPermission(owner, org.jets3t.service.acl.Permission.PERMISSION_READ);
        }
        if(perm.getOwnerPermissions()[Permission.WRITE]) {
            // when applied to a bucket, grants permission to create, overwrite, and delete any object in the bucket.
            // This permission is not supported for objects.
            if(this.isContainer()) {
                acl.grantPermission(owner, org.jets3t.service.acl.Permission.PERMISSION_WRITE);
            }
        }
        acl.revokeAllPermissions(GroupGrantee.ALL_USERS);
        if(perm.getOtherPermissions()[Permission.READ]) {
            acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_READ);
        }
        if(perm.getOtherPermissions()[Permission.WRITE]) {
            // when applied to a bucket, grants permission to create, overwrite, and delete any object in the bucket.
            // This permission is not supported for objects.
            if(this.isContainer()) {
                acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_WRITE);
            }
        }
    }

    /**
     * Use ACL support.
     *
     * @return Always returning false because permissions should be set using ACLs
     * @see #writePermissions(org.jets3t.service.acl.AccessControlList)
     */
    @Override
    public boolean isWritePermissionsSupported() {
        return true;
    }

    @Override
    public boolean isWriteModificationDateSupported() {
        return false;
    }

    @Override
    public void writeModificationDate(long millis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            this.getSession().check();
            if(attributes.isFile()) {
                this.getSession().message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                        this.getName()));

                this.getSession().getClient().deleteObject(this.getContainerName(), this.getKey());
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isContainer()) {
                    this.getSession().getClient().deleteBucket(this.getContainerName());
                }
                else {
                    this.getSession().getClient().deleteObject(this.getContainerName(), this.getKey());
                }
            }
        }
        catch(S3ServiceException e) {
            if(this.attributes.isFile()) {
                this.error("Cannot delete file", e);
            }
            if(this.attributes.isDirectory()) {
                this.error("Cannot delete folder", e);
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

    /**
     * Renaming buckets is not currently supported by S3
     *
     * @return True if directory placeholder or object
     */
    @Override
    public boolean isRenameSupported() {
        return !attributes.isVolume();
    }

    @Override
    public void rename(AbstractPath renamed) {
        try {
            if(attributes.isFile()) {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                        this.getName(), renamed));

                final S3Object destination = new S3Object(((S3HPath) renamed).getKey());
                // Keep same storage class
                destination.setStorageClass(this.attributes.getStorageClass());
                // Apply non standard ACL
                destination.setAcl(this.getDetails().getAcl());
                // Moving the object retaining the metadata of the original.
                this.getSession().getClient().moveObject(this.getContainerName(), this.getKey(), this.getContainerName(),
                        destination, false);
                this.setPath(renamed.getAbsolute());
            }
            else if(attributes.isVolume()) {
                // Renaming buckets is not currently supported by S3
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    i.rename(PathFactory.createPath(this.getSession(), renamed.getAbsolute(),
                            i.getName(), i.attributes.getType()));
                }
            }
        }
        catch(S3ServiceException e) {
            this.error(this.attributes.isFile() ? "Cannot rename file" : "Cannot rename folder", e);
        }
        catch(IOException e) {
            this.error(this.attributes.isFile() ? "Cannot rename file" : "Cannot rename folder", e);
        }
    }

    @Override
    public void copy(AbstractPath copy) {
        try {
            if(attributes.isFile()) {
                this.getSession().check();
                this.getSession().message(MessageFormat.format(Locale.localizedString("Copying {0} to {1}", "Status"),
                        this.getName(), copy));

                S3Object destination = new S3Object(((S3HPath) copy).getKey());
                // Keep same storage class
                destination.setStorageClass(copy.attributes.getStorageClass());
                // Apply non standard ACL
                destination.setAcl(this.getDetails().getAcl());
                // Copying object applying the metadata of the original
                this.getSession().getClient().copyObject(this.getContainerName(), this.getKey(),
                        ((S3HPath) copy).getContainerName(), destination, false);
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!this.getSession().isConnected()) {
                        break;
                    }
                    i.copy(PathFactory.createPath(this.getSession(), copy.getAbsolute(),
                            i.getName(), i.attributes.getType()));
                }
            }
        }
        catch(S3ServiceException e) {
            this.error(this.attributes.isFile() ? "Cannot copy file" : "Cannot copy folder", e);
        }
        catch(IOException e) {
            this.error(this.attributes.isFile() ? "Cannot copy file" : "Cannot copy folder", e);
        }
    }

    @Override
    public String toHttpURL() {
        return this.toURL();
    }

    /**
     * Overwritten to provide publicy accessible URL of given object
     *
     * @return
     */
    @Override
    public String toURL() {
        if(Preferences.instance().getBoolean("s3.url.public")) {
            return this.createSignedUrl();
        }
        final String key = this.isContainer() ? "" : this.encode(this.getKey());
        try {
            if(ServiceUtils.isBucketNameValidDNSName(this.getContainerName())) {
                return Protocol.S3.getScheme() + "://"
                        + this.getSession().getHostnameForBucket(this.getContainerName()) + key;
            }
            else {
                return this.getSession().getHost().toURL() + Path.DELIMITER + this.getContainerName() + key;
            }
        }
        catch(ConnectionCanceledException e) {
            log.error(e.getMessage());
            return super.toURL();
        }
    }

    /**
     * Query String Authentication generates a signed URL string that will grant
     * access to an S3 resource (bucket or object)
     * to whoever uses the URL up until the time specified.
     *
     * @return
     */
    public String createSignedUrl() {
        try {
            if(this.getSession().getHost().getCredentials().isAnonymousLogin()) {
                log.info("Anonymous cannot create signed URL");
                return null;
            }
            this.getSession().check();
            // Determine expiry time for URL
            int secondsFromNow = Preferences.instance().getInteger("s3.url.expire.seconds");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, secondsFromNow);
            long secondsSinceEpoch = cal.getTimeInMillis() / 1000;

            // Generate URL
            return this.getSession().getClient().createSignedUrl("GET",
                    this.getContainerName(), this.getKey(), null,
                    null, secondsSinceEpoch, false, this.getHost().getProtocol().isSecure(),
                    this.getSession().configuration.getBoolProperty("s3service.disable-dns-buckets", false));
        }
        catch(S3ServiceException e) {
            this.error("Cannot read file attributes", e);
        }
        catch(IOException e) {
            this.error("Cannot read file attributes", e);
        }
        return null;
    }

    /**
     * Generates a URL string that will return a Torrent file for an object in S3,
     * which file can be downloaded and run in a BitTorrent client.
     *
     * @return
     */
    public String createTorrentUrl() {
        try {
            return S3Service.createTorrentUrl(
                    this.getContainerName(), this.getKey(), this.getSession().configuration);
        }
        catch(ConnectionCanceledException e) {
            this.error("Cannot read file attributes", e);
        }
        return null;
    }
}