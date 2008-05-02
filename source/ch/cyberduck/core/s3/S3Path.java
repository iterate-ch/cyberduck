package ch.cyberduck.core.s3;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;
import com.apple.cocoa.foundation.NSDictionary;

import ch.cyberduck.core.*;
import ch.cyberduck.core.io.BandwidthThrottle;

import org.apache.log4j.Logger;
import org.jets3t.service.S3ObjectsChunk;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.acl.CanonicalGrantee;
import org.jets3t.service.acl.GrantAndPermission;
import org.jets3t.service.acl.GroupGrantee;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.multithread.*;
import org.jets3t.service.utils.ObjectUtils;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * @version $Id:$
 */
public class S3Path extends Path {
    private static Logger log = Logger.getLogger(S3Path.class);

    static {
        PathFactory.addFactory(Protocol.S3, new Factory());
    }

    private static class Factory extends PathFactory {
        protected Path create(Session session, String path, int type) {
            return new S3Path((S3Session) session, path, type);
        }

        protected Path create(Session session, String parent, String name, int type) {
            return new S3Path((S3Session) session, parent, name, type);
        }

        protected Path create(Session session, String path, Local file) {
            return new S3Path((S3Session) session, path, file);
        }

        protected Path create(Session session, NSDictionary dict) {
            return new S3Path((S3Session) session, dict);
        }
    }

    private final S3Session session;

    protected S3Path(S3Session s, String parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    protected S3Path(S3Session s, String path, int type) {
        super(path, type);
        this.session = s;
    }

    protected S3Path(S3Session s, String parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    protected S3Path(S3Session s, NSDictionary dict) {
        super(dict);
        this.session = s;
    }

    public Session getSession() {
        return this.session;
    }

    private Status status;

    public Status getStatus() {
        if(null == status) {
            status = new Status() {
                public void setCanceled() {
                    super.setCanceled();
                    if(null == cancelTrigger) {
                        return;
                    }
                    log.debug("Cancel trigger:" + cancelTrigger.toString());
                    cancelTrigger.cancelTask(this);
                }
            };
        }
        return status;
    }

    private S3Object _details;

    /**
     * @return
     * @throws S3ServiceException
     */
    private S3Object getDetails() throws S3ServiceException {
        if(null == this._details || !_details.isMetadataComplete()) {
            log.debug("getDetails");
            this._details = session.S3.getObjectDetails(this.getBucket(), this.getKey());
        }
        return this._details;
    }

    private S3Bucket _bucket;

    private String getBucketName() {
        AbstractPath bucketname = this;
        while(!bucketname.getParent().isRoot()) {
            bucketname = bucketname.getParent();
        }
        return bucketname.getName();
    }

    /**
     * @return
     * @throws S3ServiceException
     */
    private S3Bucket getBucket() throws S3ServiceException {
        if(null == _bucket) {
            if(this.isRoot()) {
                return null;
            }
            final String bucketname = this.getBucketName();
            if(!session.S3.isBucketAccessible(bucketname)) {
                throw new S3ServiceException("Bucket not available: " + bucketname);
            }
            try {
                final S3Bucket[] buckets = session.S3.listAllBuckets();
                for(int i = 0; i < buckets.length; i++) {
                    if(buckets[i].getName().equals(bucketname)) {
                        _bucket = buckets[i];
                        break;
                    }
                }
            }
            catch(S3ServiceException e) {
                log.error("Listing buckets failed:" + e.getMessage());
            }
            if(null == _bucket) {
                log.warn("Bucket not found with name:" + bucketname);
                return new S3Bucket(bucketname);
            }
        }
        return _bucket;
    }

    public void readSize() {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Getting size of", "Status", "") + " " + this.getName());

                attributes.setSize(this.getDetails().getContentLength());
            }
            catch(S3ServiceException e) {
                log.warn("Cannot read size:" + e.getMessage());
            }
            catch(IOException e) {
                ;
            }
        }
    }

    public void readTimestamp() {
        synchronized(session) {
            if(attributes.isFile()) {
                try {
                    session.check();
                    session.message(NSBundle.localizedString("Getting timestamp of", "Status", "") + " " + this.getName());

                    attributes.setModificationDate(this.getDetails().getLastModifiedDate().getTime());
                }
                catch(S3ServiceException e) {
                    log.warn("Cannot read timestamp:" + e.getMessage());
                }
                catch(IOException e) {
                    ;
                }
            }
        }
    }

    public void readPermission() {
        synchronized(session) {
            try {
                session.check();
                session.message(NSBundle.localizedString("Getting permission of", "Status", "") + " " + this.getName());

                AccessControlList acl = null;
                if(this.isBucket()) {
                    acl = session.S3.getBucketAcl(this.getBucket());
                }
                else if(attributes.isFile()) {
                    acl = session.S3.getObjectAcl(this.getBucket(), this.getKey());
                }
                if(null == acl) {
                    attributes.setPermission(Permission.EMPTY);
                }
                else {
                    attributes.setPermission(this.readPermissions(acl.getGrants()));
                }
            }
            catch(S3ServiceException e) {
                log.warn("Cannot read file attributes", e);
            }
            catch(IOException e) {
                ;
            }
        }
    }

    private Permission readPermissions(Set grants) {
        boolean[][] p = new boolean[3][3];
        for(Iterator iter = grants.iterator(); iter.hasNext();) {
            GrantAndPermission grant = (GrantAndPermission) iter.next();
            final org.jets3t.service.acl.Permission access = grant.getPermission();
            if(grant.getGrantee().equals(GroupGrantee.ALL_USERS)) {
                if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_READ)
                        || access.equals(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL)) {
                    p[Permission.OTHER][Permission.READ] = true;
                }
                if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_WRITE)
                        || access.equals(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL)) {
                    p[Permission.OTHER][Permission.WRITE] = true;
                }
            }
            if(grant.getGrantee().equals(new CanonicalGrantee(_bucket.getOwner().getId()))) {
                if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_READ)
                        || access.equals(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL)) {
                    p[Permission.OWNER][Permission.READ] = true;
                }
                if(access.equals(org.jets3t.service.acl.Permission.PERMISSION_WRITE)
                        || access.equals(org.jets3t.service.acl.Permission.PERMISSION_FULL_CONTROL)) {
                    p[Permission.OWNER][Permission.WRITE] = true;
                }
            }
        }
        return new Permission(p);
    }

    private CancelEventTrigger cancelTrigger;

    private class S3ServiceTransferEventAdaptor extends S3ServiceEventAdaptor {

        long bytesTransferred = getStatus().getCurrent();

        private StreamListener listener;

        public S3ServiceTransferEventAdaptor(StreamListener listener) {
            this.listener = listener;
        }

//        public void s3ServiceEventPerformed(DownloadObjectsEvent event) {
//            super.s3ServiceEventPerformed(event);
//
//            if(ServiceEvent.EVENT_STARTED == event.getEventCode()) {
//                final ThreadWatcher watcher = event.getThreadWatcher();
//
//                cancelTrigger = watcher.getCancelEventListener();
//            }
//            else if(ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
//                final ThreadWatcher watcher = event.getThreadWatcher();
//
//                final long diff = watcher.getBytesTransferred() - bytesTransferred;
//
//                listener.bytesReceived(diff);
//                S3Path.this.getStatus().setCurrent(bytesTransferred += diff);
//            }
//            else if(ServiceEvent.EVENT_ERROR == event.getEventCode()) {
//                S3Path.this.error("Download failed", event.getErrorCause());
//            }
//        }

        public void s3ServiceEventPerformed(CreateObjectsEvent event) {
            super.s3ServiceEventPerformed(event);

            if(ServiceEvent.EVENT_IN_PROGRESS == event.getEventCode()) {
                final ThreadWatcher watcher = event.getThreadWatcher();

                final long diff = watcher.getBytesTransferred() - bytesTransferred;

                listener.bytesSent(diff);
                S3Path.this.getStatus().setCurrent(bytesTransferred += diff);
            }
            else if(ServiceEvent.EVENT_STARTED == event.getEventCode()) {
                final ThreadWatcher watcher = event.getThreadWatcher();

                cancelTrigger = watcher.getCancelEventListener();
            }
            else if(ServiceEvent.EVENT_COMPLETED == event.getEventCode()) {
                // Manually mark as complete
                S3Path.this.getStatus().setComplete(true);
            }
            else if(ServiceEvent.EVENT_ERROR == event.getEventCode()) {
                S3Path.this.error("Upload failed", event.getErrorCause());
            }
        }
    }

    public void download(BandwidthThrottle throttle, final StreamListener listener, final boolean check) {
        synchronized(session) {
            if(attributes.isFile()) {
                OutputStream out = null;
                InputStream in = null;
                try {
                    if(check) {
                        session.check();
                    }
                    session.message(NSBundle.localizedString("Downloading", "Status", "") + " " + this.getName());

                    DownloadPackage download;
                    try {
                        download = ObjectUtils.createPackageForDownload(
                                new S3Object(this.getBucket(), this.getKey()),
                                new File(this.getLocal().getAbsolute()), true, false, null);
                        out = download.getOutputStream();
                        if(null == out) {
                            throw new IOException("Unable to buffer data");
                        }
                    }
                    catch(Exception e) {
                        throw new S3ServiceException(e.getMessage(), e);
                    }

                    in = session.S3.getObject(this.getBucket(), this.getKey(), null, null, null, null,
                            status.isResume() ? new Long(status.getCurrent()) : null, null).getDataInputStream();
                    if(null == in) {
                        throw new IOException("Unable opening data stream");
                    }

                    this.download(in, out, throttle, listener);
                }
                catch(S3ServiceException e) {
                    this.error("Download failed", e);
                }
                catch(IOException e) {
                    ;
                }
                finally {
                    try {
                        if(in != null) {
                            in.close();
                        }
                        if(out != null) {
                            out.close();
                        }
                    }
                    catch(IOException e) {
                        log.error(e.getMessage());
                    }
                }
                if(attributes.isDirectory()) {
                    this.getLocal().mkdir(true);
                }
            }
        }
    }

    public void upload(BandwidthThrottle throttle, final StreamListener listener, final Permission p, final boolean check) {
        synchronized(session) {
            try {
                if(check) {
                    session.check();
                }
                if(attributes.isFile()) {
                    session.message(NSBundle.localizedString("Uploading", "Status", "") + " " + this.getName());

                    final S3ServiceMulti multi = new S3ServiceMulti(session.S3,
                            new S3ServiceTransferEventAdaptor(listener)
                    );
                    final S3Object object;
                    try {
                        object = ObjectUtils.createObjectForUpload(this.getKey(),
                                new File(this.getLocal().getAbsolute()),
                                null, //no encryption
                                false); //no gzip
                        if(null != p) {
                            AccessControlList acl = AccessControlList.REST_CANNED_PRIVATE;
                            if(p.getOtherPermissions()[Permission.READ]) {
                                acl = AccessControlList.REST_CANNED_PUBLIC_READ;
                            }
                            if(p.getOtherPermissions()[Permission.WRITE]) {
                                acl = AccessControlList.REST_CANNED_PUBLIC_READ_WRITE;
                            }
                            object.setAcl(acl);
                        }
                    }
                    catch(Exception e) {
                        throw new S3ServiceException(e.getMessage(), e);
                    }

                    // Transfer
                    multi.putObjects(this.getBucket(), new S3Object[]{object});
                }
                if(attributes.isDirectory()) {
                    if(this.isBucket()) {
                        // Create bucket
                        this.mkdir();
                    }
                }
            }
            catch(S3ServiceException e) {
                this.error("Upload failed", e);
            }
            catch(IOException e) {
                ;
            }
        }
    }

    protected boolean isBucket() {
        return this.getParent().isRoot();
    }

    /**
     * The absolute path minus the bucket part
     *
     * @return
     */
    private String getKey() {
        if(this.isBucket()) {
            return this.getBucketName();
        }
        return this.getAbsolute().substring(this.getBucketName().length() + 2);
    }

    private static final int BUCKET_LIST_CHUNKING_SIZE = 1000;

    public AttributedList list(final ListParseListener listener) {
        synchronized(session) {
            AttributedList childs = new AttributedList() {
                public boolean add(Object object) {
                    boolean result = super.add(object);
                    listener.parsed(this);
                    return result;
                }
            };
            try {
                session.check();
                session.message(NSBundle.localizedString("Listing directory", "Status", "") + " "
                        + this.getAbsolute());

                if(this.isRoot()) {
                    // List all buckets
                    final S3Bucket[] buckets = session.S3.listAllBuckets();
                    for(int i = 0; i < buckets.length; i++) {
                        S3Path p = new S3Path(session, this.getAbsolute(), buckets[i].getName(),
                                Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
                        p._bucket = buckets[i];

                        p.attributes.setOwner(buckets[i].getOwner().getDisplayName());
                        p.attributes.setCreationDate(buckets[i].getCreationDate().getTime());

                        childs.add(p);
                    }
                }
                else {
                    final S3Bucket bucket = this.getBucket();
                    // Keys can be listed by prefix. By choosing a common prefix
                    // for the names of related keys and marking these keys with
                    // a special character that delimits hierarchy, you can use the list
                    // operation to select and browse keys hierarchically
                    String prefix = "";
                    if(!this.isBucket()) {
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
                        S3ObjectsChunk chunk = session.S3.listObjectsChunked(
                                bucket.getName(), prefix, delimiter,
                                BUCKET_LIST_CHUNKING_SIZE, priorLastKey);

                        final S3Object[] objects = chunk.getObjects();
                        final S3Path[] paths = new S3Path[objects.length];
                        for(int i = 0; i < objects.length; i++) {
                            paths[i] = (S3Path) PathFactory.createPath(session, bucket.getName(), objects[i].getKey(),
                                    Path.FILE_TYPE);
                            paths[i].setParent(this);
                            paths[i]._bucket = bucket;

                            paths[i].attributes.setSize(objects[i].getContentLength());
                            paths[i].attributes.setModificationDate(objects[i].getLastModifiedDate().getTime());
                            if(null != bucket.getOwner()) {
                                paths[i].attributes.setOwner(bucket.getOwner().getDisplayName());
                            }
                        }
                        childs.addAll(Arrays.asList(paths));

                        final String[] prefixes = chunk.getCommonPrefixes();
                        for(int i = 0; i < prefixes.length; i++) {
                            S3Path p = (S3Path) PathFactory.createPath(session, bucket.getName(), prefixes[i],
                                    Path.DIRECTORY_TYPE);
                            p.setParent(this);
                            p._bucket = bucket;

                            if(null != bucket.getOwner()) {
                                p.attributes.setOwner(bucket.getOwner().getDisplayName());
                            }
                            boolean[][] access = new boolean[3][3];
                            access[Permission.OWNER][Permission.READ] = true;
                            access[Permission.OWNER][Permission.EXECUTE] = true;
                            access[Permission.OTHER][Permission.READ] = true;
                            access[Permission.OTHER][Permission.EXECUTE] = true;
                            p.attributes.setPermission(new Permission(access));
                            childs.add(p);
                        }

                        priorLastKey = chunk.getPriorLastKey();
                    }
                    while(priorLastKey != null && !getStatus().isCanceled());
                }
            }
            catch(S3ServiceException e) {
                childs.attributes().setReadable(false);
                this.error("Listing directory failed", e);
            }
            catch(IOException e) {
                ;
            }
            return childs;
        }
    }

    public void mkdir() {
        synchronized(session) {
            log.debug("mkdir:" + this.getName());
            try {
                if(!this.isBucket()) {
                    throw new S3ServiceException("Bucket can only be created at top level");
                }
                session.check();
                session.message(NSBundle.localizedString("Make directory", "Status", "") + " " + this.getName());

                session.S3.createBucket(this.getName(), Preferences.instance().getProperty("s3.location"));
            }
            catch(S3ServiceException e) {
                this.error("Cannot create folder", e);
            }
            catch(IOException e) {
                ;
            }
        }
    }

    public void mkdir(boolean recursive) {
        this.mkdir();
    }

    public void writePermissions(Permission perm, boolean recursive) {
        synchronized(session) {
            log.debug("changePermissions:" + perm);
            try {
                session.check();
                session.message(NSBundle.localizedString("Changing permission to", "Status", "") + " " + perm.getOctalString() + " (" + this.getName() + ")");

                AccessControlList acl = null;
                if(this.isBucket()) {
                    acl = session.S3.getBucketAcl(this.getBucket());
                }
                else if(attributes.isFile()) {
                    acl = session.S3.getObjectAcl(this.getBucket(), this.getKey());
                }
                if(acl != null) {
                    final CanonicalGrantee ownerGrantee = new CanonicalGrantee(acl.getOwner().getId());
                    acl.revokeAllPermissions(ownerGrantee);
                    if(perm.getOwnerPermissions()[Permission.READ]) {
                        acl.grantPermission(ownerGrantee,
                                org.jets3t.service.acl.Permission.PERMISSION_READ);
                    }
                    if(perm.getOwnerPermissions()[Permission.WRITE]) {
                        acl.grantPermission(ownerGrantee,
                                org.jets3t.service.acl.Permission.PERMISSION_WRITE);
                    }
                    acl.revokeAllPermissions(GroupGrantee.ALL_USERS);
                    if(perm.getOtherPermissions()[Permission.READ]) {
                        acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_READ);
                    }
                    if(perm.getOtherPermissions()[Permission.WRITE]) {
                        acl.grantPermission(GroupGrantee.ALL_USERS, org.jets3t.service.acl.Permission.PERMISSION_WRITE);
                    }
                    if(this.isBucket()) {
                        session.S3.putBucketAcl(this.getBucketName(), acl);
                    }
                    else if(attributes.isFile()) {
                        session.S3.putObjectAcl(this.getBucketName(), this.getKey(), acl);
                    }
                }
                if(attributes.isDirectory()) {
                    if(recursive) {
                        for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                            if(!session.isConnected()) {
                                break;
                            }
                            ((AbstractPath) iter.next()).writePermissions(perm, recursive);
                        }
                    }
                }
            }
            catch(S3ServiceException e) {
                this.error("Cannot change permissions", e);
            }
            catch(IOException e) {
                ;
            }
        }
    }

    public void delete() {
        synchronized(session) {
            log.debug("delete:" + this.toString());
            try {
                session.check();
                if(attributes.isFile()) {
                    session.message(NSBundle.localizedString("Deleting", "Status", "") + " " + this.getName());
                    session.S3.deleteObject(this.getBucketName(), this.getKey());
                }
                else if(attributes.isDirectory()) {
                    for(Iterator iter = this.childs().iterator(); iter.hasNext();) {
                        if(!session.isConnected()) {
                            break;
                        }
                        Path file = (Path) iter.next();
                        file.delete();
                    }
                    if(this.isBucket()) {
                        session.S3.deleteBucket(this.getBucketName());
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
                ;
            }
        }
    }

    public boolean isRenameSupported() {
        return false;
    }

    public void rename(String absolute) {
        throw new UnsupportedOperationException();
    }

    /**
     * Overwritten to provide publicy accessible URL of given object
     *
     * @return
     */
    public String toURL() {
        if(Preferences.instance().getBoolean("s3.url.public")) {
            // Determine expiry time for URL
            int secondsFromNow = Preferences.instance().getInteger("s3.url.expire.seconds");
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.SECOND, secondsFromNow);
            long secondsSinceEpoch = cal.getTimeInMillis() / 1000;

            // Generate URL
            try {
                session.check();
                return S3Service.createSignedUrl("GET",
                        this.getBucketName(), this.getName(), null,
                        null, session.S3.getAWSCredentials(), secondsSinceEpoch, false);
            }
            catch(S3ServiceException e) {
                log.error(e.getMessage());
            }
            catch(IOException e) {
                session.interrupt();
                log.error(e.getMessage());
            }
        }
        try {
            StringBuffer b = new StringBuffer();
            StringTokenizer t = new StringTokenizer(this.getKey(), "/");
            while(t.hasMoreTokens()) {
                b.append(DELIMITER).append(URLEncoder.encode(t.nextToken(), "UTF-8"));
            }
            // Do not use java.net.URL because it doesn't know about SFTP!
            return Protocol.S3.getScheme() + "://" + RestS3Service.generateS3HostnameForBucket(this.getBucketName()) + b.toString();
        }
        catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public boolean isMkdirSupported() {
        return this.isRoot();
    }
}