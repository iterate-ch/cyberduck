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
import ch.cyberduck.core.cloud.CloudPath;
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
import org.jets3t.service.model.cloudfront.Distribution;
import org.jets3t.service.multithread.*;
import org.jets3t.service.utils.ObjectUtils;

import java.io.*;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.*;

/**
 * @version $Id:$
 */
public class S3Path extends CloudPath {
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

    public boolean exists() {
        if(this.isRoot()) {
            return true;
        }
        try {
            this.getDetails();
        }
        catch(S3ServiceException e) {
            return false;
        }
        return true;
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

    /**
     * @return
     * @throws S3ServiceException
     */
    protected S3Bucket getBucket() throws S3ServiceException {
        if(null == _bucket) {
            if(this.isRoot()) {
                return null;
            }
            final String bucketname = this.getContainerName();
            if(!session.S3.isBucketAccessible(bucketname)) {
                throw new S3ServiceException("Bucket not available: " + bucketname);
            }
            final S3Bucket[] buckets = session.getBuckets(false);
            for(int i = 0; i < buckets.length; i++) {
                if(buckets[i].getName().equals(bucketname)) {
                    _bucket = buckets[i];
                    break;
                }
            }
            if(null == _bucket) {
                log.warn("Bucket not found with name:" + bucketname);
                return new S3Bucket(bucketname);
            }
        }
        return _bucket;
    }

    public void readSize() {
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Getting size of {0}", "Status", ""),
                    this.getName()));

            attributes.setSize(this.getDetails().getContentLength());
        }
        catch(S3ServiceException e) {
            log.warn("Cannot read size:" + e.getMessage());
        }
        catch(IOException e) {
            ;
        }
    }

    public void readTimestamp() {
        if(attributes.isFile()) {
            try {
                session.check();
                session.message(MessageFormat.format(NSBundle.localizedString("Getting timestamp of {0}", "Status", ""),
                        this.getName()));

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

    public void readPermission() {
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Getting permission of {0}", "Status", ""),
                    this.getName()));

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
        if(attributes.isFile()) {
            OutputStream out = null;
            InputStream in = null;
            try {
                if(check) {
                    session.check();
                }
                this.getSession().message(MessageFormat.format(NSBundle.localizedString("Downloading {0}", "Status", ""),
                        this.getName()));

                DownloadPackage download;
                try {
                    download = ObjectUtils.createPackageForDownload(
                            new S3Object(this.getBucket(), this.getKey()),
                            new File(this.getLocal().getAbsolute()), true, false, null);
                    download.setAppendToFile(true);
                    out = download.getOutputStream();
                    if(null == out) {
                        throw new IOException("Unable to buffer data");
                    }
                }
                catch(Exception e) {
                    throw new S3ServiceException(e.getMessage(), e);
                }

                in = session.S3.getObject(this.getBucket(), this.getKey(), null, null, null, null,
                        this.getStatus().isResume() ? this.getStatus().getCurrent() : null, null).getDataInputStream();
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

    public void upload(BandwidthThrottle throttle, final StreamListener listener, final Permission p, final boolean check) {
        try {
            if(check) {
                session.check();
            }
            if(attributes.isFile()) {
                this.getSession().message(MessageFormat.format(NSBundle.localizedString("Uploading {0}", "Status", ""),
                        this.getName()));

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

                // No Content-Range support
                getStatus().setCurrent(0);

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
            return this.getContainerName();
        }
        if(this.getAbsolute().startsWith(Path.DELIMITER + this.getContainerName())) {
            return this.getAbsolute().substring(this.getContainerName().length() + 2);
        }
        return this.getAbsolute();
    }

    private static final int BUCKET_LIST_CHUNKING_SIZE = 1000;

    public AttributedList<Path> list() {
        final AttributedList<Path> childs = new AttributedList<Path>();
        try {
            session.check();
            session.message(NSBundle.localizedString("Listing directory", "Status", "") + " "
                    + this.getAbsolute());

            if(this.isRoot()) {
                // List all buckets
                final S3Bucket[] buckets = session.getBuckets(true);
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
                        paths[i] = new S3Path(session, bucket.getName(), objects[i].getKey(),
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
                        if(prefixes[i].equals(Path.DELIMITER)) {
                            log.warn("Skipping prefix " + prefixes[i]);
                            continue;
                        }
                        S3Path p = new S3Path(session, bucket.getName(), prefixes[i],
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

    public void mkdir() {
        log.debug("mkdir:" + this.getName());
        try {
            if(!this.isBucket()) {
                throw new S3ServiceException("Bucket can only be created at top level");
            }
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Making directory {0}", "Status", ""),
                    this.getName()));

            session.S3.createBucket(this.getName(), Preferences.instance().getProperty("s3.location"));
        }
        catch(S3ServiceException e) {
            this.error("Cannot create folder", e);
        }
        catch(IOException e) {
            ;
        }
    }

    public void mkdir(boolean recursive) {
        this.mkdir();
    }

    public void writePermissions(Permission perm, boolean recursive) {
        log.debug("writePermissions:" + perm);
        try {
            session.check();
            session.message(MessageFormat.format(NSBundle.localizedString("Changing permission of {0} to {1}", "Status", ""),
                    this.getName(), perm.getOctalString()));


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
                    session.S3.putBucketAcl(this.getContainerName(), acl);
                }
                else if(attributes.isFile()) {
                    session.S3.putObjectAcl(this.getContainerName(), this.getKey(), acl);
                }
            }
            if(attributes.isDirectory()) {
                if(recursive) {
                    for(AbstractPath child : this.childs()) {
                        if(!session.isConnected()) {
                            break;
                        }
                        child.writePermissions(perm, recursive);
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

    public void delete() {
        log.debug("delete:" + this.toString());
        try {
            session.check();
            if(attributes.isFile()) {
                session.message(MessageFormat.format(NSBundle.localizedString("Deleting {0}", "Status", ""),
                        this.getName()));

                session.S3.deleteObject(this.getContainerName(), this.getKey());
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    i.delete();
                }
                if(this.isBucket()) {
                    session.S3.deleteBucket(this.getContainerName());
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

    public void rename(Path renamed) {
        try {
            if(attributes.isFile()) {
                session.check();
                session.message(MessageFormat.format(NSBundle.localizedString("Renaming {0} to {1}", "Status", ""),
                        this.getName(), renamed));

                session.S3.moveObject(this.getContainerName(), this.getKey(), this.getContainerName(),
                        new S3Object(((S3Path) renamed).getKey()), false);
                this.setPath(renamed.getAbsolute());
                if(!this.getContainerName().equals(((S3Path) renamed).getContainerName())) {
                    _bucket = null;
                }
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    i.rename(PathFactory.createPath(this.getSession(), renamed.getAbsolute(),
                            i.getName(), i.attributes.getType()));
                }
            }
        }
        catch(S3ServiceException e) {
            if(this.attributes.isFile()) {
                this.error("Cannot rename file", e);
            }
            if(this.attributes.isDirectory()) {
                this.error("Cannot rename folder", e);
            }
        }
        catch(IOException e) {
            ;
        }
    }

    public void copy(Path copy) {
        try {
            if(attributes.isFile()) {
                session.check();
                session.message(MessageFormat.format(NSBundle.localizedString("Copying {0} to {1}", "Status", ""),
                        this.getName(), copy));

                session.S3.copyObject(this.getContainerName(), this.getKey(), ((S3Path) copy).getContainerName(),
                        new S3Object(((S3Path) copy).getKey()), false);
            }
            else if(attributes.isDirectory()) {
                for(AbstractPath i : this.childs()) {
                    if(!session.isConnected()) {
                        break;
                    }
                    i.copy(PathFactory.createPath(this.getSession(), copy.getAbsolute(),
                            i.getName(), i.attributes.getType()));
                }
            }
        }
        catch(S3ServiceException e) {
            if(this.attributes.isFile()) {
                this.error("Cannot copy file", e);
            }
            if(this.attributes.isDirectory()) {
                this.error("Cannot copy folder", e);
            }
        }
        catch(IOException e) {
            ;
        }
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
                        this.getContainerName(), this.getName(), null,
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
            if(RestS3Service.isBucketNameValidDNSName(this.getContainerName())) {
                return Protocol.S3.getScheme() + "://" + RestS3Service.generateS3HostnameForBucket(this.getContainerName()) + b.toString();
            }
            else {
                return this.getSession().getHost().toURL() + Path.DELIMITER + this.getContainerName() + b.toString();
            }
        }
        catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
    }

    public boolean isMkdirSupported() {
        return this.isRoot();
    }

    /**
     * @return
     */
    public Distribution readDistribution() {
        try {
            for(Distribution distribution : session.listDistributions()) {
                if(distribution.getOrigin().equals(RestS3Service.generateS3HostnameForBucket(this.getContainerName()))) {
                    return distribution;
                }
            }
        }
        catch(S3Exception e) {
            this.error(e.getMessage(), e);
        }
        catch(IOException e) {
            this.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * Amazon CloudFront Extension
     *
     * @param enabled
     * @param cnames
     */
    public void writeDistribution(final boolean enabled, final String[] cnames) {
        try {
            final String bucket = this.getContainerName();
            for(Distribution distribution : session.listDistributions()) {
                if(distribution.getOrigin().equals(RestS3Service.generateS3HostnameForBucket(this.getContainerName()))) {
                    session.updateDistribution(enabled, distribution, cnames);
                    return;
                }
            }
            // Create new configuration
            session.createDistribution(enabled, bucket, cnames);
        }
        catch(S3Exception e) {
            this.error(e.getMessage(), e);
        }
        catch(IOException e) {
            this.error(e.getMessage(), e);
        }
    }
}