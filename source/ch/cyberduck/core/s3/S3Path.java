package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.cloud.CloudPath;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.Local;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.StorageObjectsChunk;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.S3Version;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.model.container.ObjectKeyAndVersion;
import org.jets3t.service.utils.ServiceUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

/**
 * @version $Id$
 */
public class S3Path extends CloudPath {
    private static final Logger log = Logger.getLogger(S3Path.class);

    private final S3Session session;

    public S3Path(S3Session s, Path parent, String name, int type) {
        super(parent, name, type);
        this.session = s;
    }

    public S3Path(S3Session s, String path, int type) {
        super(s, path, type);
        this.session = s;
    }

    public S3Path(S3Session s, Path parent, Local file) {
        super(parent, file);
        this.session = s;
    }

    public <T> S3Path(S3Session s, T dict) {
        super(s, dict);
        this.session = s;
    }

    @Override
    public S3Session getSession() {
        return session;
    }

    /**
     * Object details not contained in standard listing.
     *
     * @see #getDetails()
     */
    protected StorageObject details;

    /**
     * Retrieve and cache object details.
     *
     * @return Object details
     */
    protected StorageObject getDetails() throws BackgroundException {
        final String container = this.getContainer().getName();
        if(session.getHost().getCredentials().isAnonymousLogin()) {
            log.info("Anonymous cannot access object details");
            final StorageObject object = new StorageObject(this.getKey());
            object.setBucketName(container);
            return object;
        }
        if(null == details || !details.isMetadataComplete()) {
            try {
                if(this.attributes().isDuplicate()) {
                    details = session.getClient().getVersionedObjectDetails(this.attributes().getVersionId(),
                            container, this.getKey());
                }
                else {
                    details = session.getClient().getObjectDetails(container, this.getKey());
                }
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot read file attributes", e, this);
            }
        }
        return details;
    }

    @Override
    public InputStream read(final TransferStatus status) throws BackgroundException {
        try {
            if(this.attributes().isDuplicate()) {
                return session.getClient().getVersionedObject(attributes().getVersionId(),
                        this.getContainer().getName(), this.getKey(),
                        null, // ifModifiedSince
                        null, // ifUnmodifiedSince
                        null, // ifMatch
                        null, // ifNoneMatch
                        status.isResume() ? status.getCurrent() : null, null).getDataInputStream();
            }
            return session.getClient().getObject(this.getContainer().getName(), this.getKey(),
                    null, // ifModifiedSince
                    null, // ifUnmodifiedSince
                    null, // ifMatch
                    null, // ifNoneMatch
                    status.isResume() ? status.getCurrent() : null, null).getDataInputStream();
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Download failed", e, this);
        }
    }

    @Override
    public void download(BandwidthThrottle throttle, final StreamListener listener,
                         final TransferStatus status) throws BackgroundException {
        OutputStream out = null;
        InputStream in = null;
        try {
            in = this.read(status);
            out = this.getLocal().getOutputStream(status.isResume());
            this.download(in, out, throttle, listener, status);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download failed", e, this);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
    }

    /**
     * Default size threshold for when to use multipart uploads.
     */
    private static final long DEFAULT_MULTIPART_UPLOAD_THRESHOLD =
            Preferences.instance().getLong("s3.upload.multipart.threshold");

    /**
     * Default minimum part size for upload parts.
     */
    private static final int DEFAULT_MINIMUM_UPLOAD_PART_SIZE =
            Preferences.instance().getInteger("s3.upload.multipart.size");

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    @Override
    public void upload(final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status) throws BackgroundException {
        try {
            if(attributes().isFile()) {
                final StorageObject object = this.createObjectDetails();

                session.message(MessageFormat.format(Locale.localizedString("Uploading {0}", "Status"),
                        this.getName()));

                if(session.isMultipartUploadSupported()
                        && status.getLength() > DEFAULT_MULTIPART_UPLOAD_THRESHOLD) {
                    this.uploadMultipart(throttle, listener, status, object);
                }
                else {
                    this.uploadSingle(throttle, listener, status, object);
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Upload failed", e, this);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, this);
        }
    }

    private StorageObject createObjectDetails() throws BackgroundException {
        final StorageObject object = new StorageObject(this.getKey());
        final String type = new MappingMimeTypeService().getMime(getName());
        object.setContentType(type);
        if(Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
            session.message(MessageFormat.format(
                    Locale.localizedString("Compute MD5 hash of {0}", "Status"), this.getName()));
            object.setMd5Hash(ServiceUtils.fromHex(this.getLocal().attributes().getChecksum()));
        }
        Acl acl = this.attributes().getAcl();
        if(Acl.EMPTY.equals(acl)) {
            if(Preferences.instance().getProperty("s3.bucket.acl.default").equals("public-read")) {
                object.setAcl(session.getPublicCannedReadAcl());
            }
            else {
                // Owner gets FULL_CONTROL. No one else has access rights (default).
                object.setAcl(session.getPrivateCannedAcl());
            }
        }
        else {
            object.setAcl(new S3AccessControlListFeature(session).convert(acl));
        }
        // Storage class
        if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.storage.class"))) {
            object.setStorageClass(Preferences.instance().getProperty("s3.storage.class"));
        }
        if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.encryption.algorithm"))) {
            object.setServerSideEncryptionAlgorithm(Preferences.instance().getProperty("s3.encryption.algorithm"));
        }
        // Default metadata for new files
        for(String m : Preferences.instance().getList("s3.metadata.default")) {
            if(StringUtils.isBlank(m)) {
                continue;
            }
            if(!m.contains("=")) {
                log.warn(String.format("Invalid header %s", m));
                continue;
            }
            int split = m.indexOf('=');
            String name = m.substring(0, split);
            if(StringUtils.isBlank(name)) {
                log.warn(String.format("Missing key in header %s", m));
                continue;
            }
            String value = m.substring(split + 1);
            if(StringUtils.isEmpty(value)) {
                log.warn(String.format("Missing value in header %s", m));
                continue;
            }
            object.addMetadata(name, value);
        }
        return object;
    }

    /**
     * @param throttle Bandwidth throttle
     * @param listener Callback for bytes sent
     * @param status   Transfer status
     * @param object   File location
     */
    private void uploadSingle(final BandwidthThrottle throttle, final StreamListener listener,
                              final TransferStatus status, final StorageObject object) throws BackgroundException {

        InputStream in = null;
        ResponseOutputStream<StorageObject> out = null;
        MessageDigest digest = null;
        if(!Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
            // Content-MD5 not set. Need to verify ourselves instad of S3
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException e) {
                log.error(e.getMessage());
            }
        }
        try {
            if(null == digest) {
                log.warn("MD5 calculation disabled");
                in = this.getLocal().getInputStream();
            }
            else {
                in = new DigestInputStream(this.getLocal().getInputStream(), digest);
            }
            out = this.write(object, status.getLength() - status.getCurrent(),
                    Collections.<String, String>emptyMap());
            try {
                this.upload(out, in, throttle, listener, status);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Upload failed", e, this);
            }
        }
        catch(FileNotFoundException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        finally {
            IOUtils.closeQuietly(in);
            IOUtils.closeQuietly(out);
        }
        if(null != digest) {
            final StorageObject part = out.getResponse();
            session.message(MessageFormat.format(
                    Locale.localizedString("Compute MD5 hash of {0}", "Status"), this.getName()));
            // Obtain locally-calculated MD5 hash.
            String hexMD5 = ServiceUtils.toHex(digest.digest());
            try {
                session.getClient().verifyExpectedAndActualETagValues(hexMD5, part);
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Upload failed", e, this);
            }
        }
    }

    /**
     * @param throttle Bandwidth throttle
     * @param listener Callback for bytes sent
     * @param status   Transfer status
     * @param object   File location
     * @throws IOException      I/O error
     * @throws ServiceException Service error
     */
    private void uploadMultipart(final BandwidthThrottle throttle, final StreamListener listener,
                                 final TransferStatus status, final StorageObject object)
            throws IOException, ServiceException, BackgroundException {

        final ThreadFactory threadFactory = new NamedThreadFactory("multipart");

        MultipartUpload multipart = null;
        if(status.isResume()) {
            // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
            // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
            // not yet been completed or aborted.
            final List<MultipartUpload> uploads = session.getClient().multipartListUploads(this.getContainer().getName());
            for(MultipartUpload upload : uploads) {
                if(!upload.getBucketName().equals(this.getContainer().getName())) {
                    continue;
                }
                if(!upload.getObjectKey().equals(this.getKey())) {
                    continue;
                }
                if(log.isInfoEnabled()) {
                    log.info(String.format("Resume multipart upload %s", upload.getUploadId()));
                }
                multipart = upload;
                break;
            }
        }
        if(null == multipart) {
            log.info("No pending multipart upload found");

            // Initiate multipart upload with metadata
            Map<String, Object> metadata = object.getModifiableMetadata();
            if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.storage.class"))) {
                metadata.put(session.getClient().getRestHeaderPrefix() + "storage-class",
                        Preferences.instance().getProperty("s3.storage.class"));
            }
            if(StringUtils.isNotBlank(Preferences.instance().getProperty("s3.encryption.algorithm"))) {
                metadata.put(session.getClient().getRestHeaderPrefix() + "server-side-encryption",
                        Preferences.instance().getProperty("s3.encryption.algorithm"));
            }

            multipart = session.getClient().multipartStartUpload(
                    this.getContainer().getName(), this.getKey(), metadata);
        }

        final List<MultipartPart> completed;
        if(status.isResume()) {
            log.info(String.format("List completed parts of %s", multipart.getUploadId()));
            // This operation lists the parts that have been uploaded for a specific multipart upload.
            completed = session.getClient().multipartListParts(multipart);
        }
        else {
            completed = new ArrayList<MultipartPart>();
        }

        /**
         * At any point, at most
         * <tt>nThreads</tt> threads will be active processing tasks.
         */
        final ExecutorService pool = Executors.newFixedThreadPool(
                Preferences.instance().getInteger("s3.upload.multipart.concurency"), threadFactory);

        try {
            final List<Future<MultipartPart>> parts = new ArrayList<Future<MultipartPart>>();

            final long defaultPartSize = Math.max((status.getLength() / MAXIMUM_UPLOAD_PARTS),
                    DEFAULT_MINIMUM_UPLOAD_PART_SIZE);

            long remaining = status.getLength();
            long marker = 0;

            for(int partNumber = 1; remaining > 0; partNumber++) {
                boolean skip = false;
                if(status.isResume()) {
                    log.info(String.format("Determine if part %d can be skipped", partNumber));
                    for(MultipartPart c : completed) {
                        if(c.getPartNumber().equals(partNumber)) {
                            log.info("Skip completed part number " + partNumber);
                            listener.bytesSent(c.getSize());
                            skip = true;
                            break;
                        }
                    }
                }

                // Last part can be less than 5 MB. Adjust part size.
                final long length = Math.min(defaultPartSize, remaining);

                if(!skip) {
                    // Submit to queue
                    parts.add(this.submitPart(throttle, listener, status, multipart, pool, partNumber, marker, length));
                }

                remaining -= length;
                marker += length;
            }
            for(Future<MultipartPart> future : parts) {
                try {
                    completed.add(future.get());
                }
                catch(InterruptedException e) {
                    log.error("Part upload failed:" + e.getMessage());
                    throw new ConnectionCanceledException(e);
                }
                catch(ExecutionException e) {
                    log.warn("Part upload failed:" + e.getMessage());
                    if(e.getCause() instanceof ServiceException) {
                        throw (ServiceException) e.getCause();
                    }
                    if(e.getCause() instanceof IOException) {
                        throw (IOException) e.getCause();
                    }
                    throw new ConnectionCanceledException(e);
                }
            }
            if(status.isComplete()) {
                session.getClient().multipartCompleteUpload(multipart, completed);
            }
        }
        finally {
            if(!status.isComplete()) {
                // Cancel all previous parts
                log.info(String.format("Cancel multipart upload %s", multipart.getUploadId()));
                session.getClient().multipartAbortUpload(multipart);
            }
            // Cancel future tasks
            pool.shutdown();
        }
    }

    private Future<MultipartPart> submitPart(final BandwidthThrottle throttle, final StreamListener listener,
                                             final TransferStatus status, final MultipartUpload multipart,
                                             final ExecutorService pool,
                                             final int partNumber,
                                             final long offset, final long length) throws BackgroundException {
        if(pool.isShutdown()) {
            throw new ConnectionCanceledException();
        }
        log.info(String.format("Submit part %d to queue", partNumber));
        return pool.submit(new Callable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                final Map<String, String> requestParameters = new HashMap<String, String>();
                requestParameters.put("uploadId", multipart.getUploadId());
                requestParameters.put("partNumber", String.valueOf(partNumber));

                InputStream in = null;
                ResponseOutputStream<StorageObject> out = null;
                MessageDigest digest = null;
                try {
                    if(!Preferences.instance().getBoolean("s3.upload.metadata.md5")) {
                        // Content-MD5 not set. Need to verify ourselves instad of S3
                        try {
                            digest = MessageDigest.getInstance("MD5");
                        }
                        catch(NoSuchAlgorithmException e) {
                            log.error(e.getMessage());
                        }
                    }
                    if(null == digest) {
                        log.warn("MD5 calculation disabled");
                        in = getLocal().getInputStream();
                    }
                    else {
                        in = new DigestInputStream(getLocal().getInputStream(), digest);
                    }
                    out = write(new StorageObject(getKey()), length, requestParameters);
                    upload(out, in, throttle, listener, offset, length, status);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                finally {
                    IOUtils.closeQuietly(in);
                    IOUtils.closeQuietly(out);
                }
                final StorageObject part = out.getResponse();
                if(null != digest) {
                    // Obtain locally-calculated MD5 hash
                    String hexMD5 = ServiceUtils.toHex(digest.digest());
                    try {
                        session.getClient().verifyExpectedAndActualETagValues(hexMD5, part);
                    }
                    catch(ServiceException e) {
                        throw new ServiceExceptionMappingService().map("Upload failed", e, S3Path.this);
                    }
                }
                // Populate part with response data that is accessible via the object's metadata
                return new MultipartPart(partNumber, part.getLastModifiedDate(),
                        part.getETag(), part.getContentLength());
            }
        });
    }

    @Override
    public OutputStream write(final TransferStatus status) throws BackgroundException {
        return this.write(this.createObjectDetails(), status.getLength() - status.getCurrent(),
                Collections.<String, String>emptyMap());
    }

    private ResponseOutputStream<StorageObject> write(final StorageObject part, final Long contentLength,
                                                      final Map<String, String> requestParams) throws BackgroundException {
        DelayedHttpEntityCallable<StorageObject> command = new DelayedHttpEntityCallable<StorageObject>() {
            @Override
            public StorageObject call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    session.getClient().putObjectWithRequestEntityImpl(getContainer().getName(), part, entity, requestParams);
                }
                catch(ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Upload failed", e, S3Path.this);
                }
                return part;
            }

            @Override
            public long getContentLength() {
                return contentLength;
            }
        };
        return this.write(command);
    }

    @Override
    public AttributedList<Path> list() throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Listing directory {0}", "Status"),
                    this.getName()));

            if(this.isRoot()) {
                // List all buckets
                return new AttributedList<Path>(new S3BucketListService().list(session));
            }
            else {
                // Keys can be listed by prefix. By choosing a common prefix
                // for the names of related keys and marking these keys with
                // a special character that delimits hierarchy, you can use the list
                // operation to select and browse keys hierarchically
                String prefix = StringUtils.EMPTY;
                if(!this.isContainer()) {
                    // estricts the response to only contain results that begin with the
                    // specified prefix. If you omit this optional argument, the value
                    // of Prefix for your query will be the empty string.
                    // In other words, the results will be not be restricted by prefix.
                    prefix = this.getKey();
                    if(!prefix.endsWith(String.valueOf(Path.DELIMITER))) {
                        prefix += Path.DELIMITER;
                    }
                }
                // If this optional, Unicode string parameter is included with your request,
                // then keys that contain the same string between the prefix and the first
                // occurrence of the delimiter will be rolled up into a single result
                // element in the CommonPrefixes collection. These rolled-up keys are
                // not returned elsewhere in the response.
                final AttributedList<Path> children = new AttributedList<Path>();
                children.addAll(this.listObjects(this.getContainer(), prefix, String.valueOf(Path.DELIMITER)));
                if(Preferences.instance().getBoolean("s3.revisions.enable")) {
                    if(new S3VersioningFeature(session).getConfiguration(this.getContainer()).isEnabled()) {
                        String priorLastKey = null;
                        String priorLastVersionId = null;
                        do {
                            final VersionOrDeleteMarkersChunk chunk = session.getClient().listVersionedObjectsChunked(
                                    this.getContainer().getName(), prefix, String.valueOf(Path.DELIMITER),
                                    Preferences.instance().getInteger("s3.listing.chunksize"),
                                    priorLastKey, priorLastVersionId, true);
                            children.addAll(this.listVersions(this.getContainer(), Arrays.asList(chunk.getItems())));
                            priorLastKey = chunk.getNextKeyMarker();
                            priorLastVersionId = chunk.getNextVersionIdMarker();
                        }
                        while(priorLastKey != null);
                    }
                }
                return children;
            }
        }
        catch(ServiceException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new ServiceExceptionMappingService().map("Listing directory failed", e, this);
        }
        catch(IOException e) {
            log.warn(String.format("Directory listing failure for %s with failure %s", this, e.getMessage()));
            throw new DefaultIOExceptionMappingService().map(e, this);
        }
    }

    private AttributedList<Path> listObjects(final Path bucket, final String prefix, final String delimiter)
            throws IOException, ServiceException, BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        // Null if listing is complete
        String priorLastKey = null;
        do {
            // Read directory listing in chunks. List results are always returned
            // in lexicographic (alphabetical) order.
            final StorageObjectsChunk chunk = session.getClient().listObjectsChunked(
                    bucket.getName(), prefix, delimiter,
                    Preferences.instance().getInteger("s3.listing.chunksize"), priorLastKey);

            final StorageObject[] objects = chunk.getObjects();
            for(StorageObject object : objects) {
                final S3Path p = new S3Path(session, bucket.getName() + Path.DELIMITER + object.getKey(), FILE_TYPE);
                p.attributes().setSize(object.getContentLength());
                p.attributes().setModificationDate(object.getLastModifiedDate().getTime());
                p.attributes().setRegion(bucket.attributes().getRegion());
                p.attributes().setStorageClass(object.getStorageClass());
                p.attributes().setEncryption(object.getServerSideEncryptionAlgorithm());
                // Directory placeholders
                if(object.isDirectoryPlaceholder()) {
                    p.attributes().setType(DIRECTORY_TYPE);
                    p.attributes().setPlaceholder(true);
                }
                else if(0 == object.getContentLength()) {
                    if("application/x-directory".equals(p.getDetails().getContentType())) {
                        p.attributes().setType(DIRECTORY_TYPE);
                        p.attributes().setPlaceholder(true);
                    }
                }
                final Object etag = object.getMetadataMap().get(StorageObject.METADATA_HEADER_ETAG);
                if(null != etag) {
                    final String checksum = etag.toString().replaceAll("\"", StringUtils.EMPTY);
                    p.attributes().setChecksum(checksum);
                    if(checksum.equals("d66759af42f282e1ba19144df2d405d0")) {
                        // Fix #5374 s3sync.rb interoperability
                        p.attributes().setType(DIRECTORY_TYPE);
                        p.attributes().setPlaceholder(true);
                    }
                }
                if(object instanceof S3Object) {
                    p.attributes().setVersionId(((S3Object) object).getVersionId());
                }
                children.add(p);
            }
            final String[] prefixes = chunk.getCommonPrefixes();
            for(String common : prefixes) {
                if(common.equals(String.valueOf(Path.DELIMITER))) {
                    log.warn("Skipping prefix " + common);
                    continue;
                }
                final Path p = new S3Path(session, bucket.getName() + Path.DELIMITER + common, DIRECTORY_TYPE);
                if(children.contains(p.getReference())) {
                    // There is already a placeholder object
                    continue;
                }
                p.attributes().setRegion(bucket.attributes().getRegion());
                p.attributes().setPlaceholder(false);
                children.add(p);
            }
            priorLastKey = chunk.getPriorLastKey();
        }
        while(priorLastKey != null);
        return children;
    }

    private List<Path> listVersions(final Path bucket, final List<BaseVersionOrDeleteMarker> versionOrDeleteMarkers)
            throws IOException, ServiceException {
        // Amazon S3 returns object versions in the order in which they were
        // stored, with the most recently stored returned first.
        Collections.sort(versionOrDeleteMarkers, new Comparator<BaseVersionOrDeleteMarker>() {
            @Override
            public int compare(BaseVersionOrDeleteMarker o1, BaseVersionOrDeleteMarker o2) {
                return o1.getLastModified().compareTo(o2.getLastModified());
            }
        });
        final List<Path> versions = new ArrayList<Path>();
        int i = 0;
        for(BaseVersionOrDeleteMarker marker : versionOrDeleteMarkers) {
            if((marker.isDeleteMarker() && marker.isLatest())
                    || !marker.isLatest()) {
                // Latest version already in default listing
                final S3Path p = new S3Path(session, bucket.getName() + Path.DELIMITER + marker.getKey(), FILE_TYPE);
                // Versioning is enabled if non null.
                p.attributes().setVersionId(marker.getVersionId());
                p.attributes().setRevision(++i);
                p.attributes().setDuplicate(true);
                p.attributes().setModificationDate(marker.getLastModified().getTime());
                p.attributes().setRegion(bucket.attributes().getRegion());
                if(marker instanceof S3Version) {
                    p.attributes().setSize(((S3Version) marker).getSize());
                    p.attributes().setETag(((S3Version) marker).getEtag());
                    p.attributes().setStorageClass(((S3Version) marker).getStorageClass());
                }
                versions.add(p);
            }
        }
        return versions;
    }

    @Override
    public void mkdir() throws BackgroundException {
        try {
            if(this.isContainer()) {
                // Create bucket
                if(!ServiceUtils.isBucketNameValidDNSName(this.getName())) {
                    throw new ServiceException(Locale.localizedString("Bucket name is not DNS compatible", "S3"));
                }
                String location = Preferences.instance().getProperty("s3.location");
                if(!session.getHost().getProtocol().getLocations().contains(location)) {
                    log.warn("Default bucket location not supported by provider:" + location);
                    location = "US";
                    log.warn("Fallback to US");
                }
                AccessControlList acl;
                if(Preferences.instance().getProperty("s3.bucket.acl.default").equals("public-read")) {
                    acl = session.getPublicCannedReadAcl();
                }
                else {
                    acl = session.getPrivateCannedAcl();
                }
                session.getClient().createBucket(this.getContainer().getName(), location, acl);
            }
            else {
                StorageObject object = new StorageObject(this.getKey() + Path.DELIMITER);
                object.setBucketName(this.getContainer().getName());
                // Set object explicitly to private access by default.
                object.setAcl(session.getPrivateCannedAcl());
                object.setContentLength(0);
                object.setContentType("application/x-directory");
                session.getClient().putObject(this.getContainer().getName(), object);
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot create folder {0}", e, this);
        }
    }

    @Override
    public void delete(final LoginController prompt) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Deleting {0}", "Status"),
                    this.getName()));

            if(attributes().isFile()) {
                this.delete(prompt, this.getContainer(), Collections.singletonList(
                        new ObjectKeyAndVersion(this.getKey(), this.attributes().getVersionId())));
            }
            else if(attributes().isDirectory()) {
                final List<ObjectKeyAndVersion> files = new ArrayList<ObjectKeyAndVersion>();
                for(Path child : this.list()) {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    if(child.attributes().isDirectory()) {
                        child.delete(prompt);
                    }
                    else {
                        files.add(new ObjectKeyAndVersion(child.getKey(), child.attributes().getVersionId()));
                    }
                }
                if(!this.isContainer()) {
                    // Because we normalize paths and remove a trailing delimiter we add it here again as the
                    // default directory placeholder formats has the format `/placeholder/' as a key.
                    files.add(new ObjectKeyAndVersion(this.getKey() + Path.DELIMITER,
                            this.attributes().getVersionId()));
                    // Always returning 204 even if the key does not exist.
                    // Fallback to legacy directory placeholders with metadata instead of key with trailing delimiter
                    files.add(new ObjectKeyAndVersion(this.getKey(),
                            this.attributes().getVersionId()));
                    // AWS does not return 404 for non-existing keys
                }
                if(!files.isEmpty()) {
                    this.delete(prompt, this.getContainer(), files);
                }
                if(this.isContainer()) {
                    // Finally delete bucket itself
                    session.getClient().deleteBucket(this.getContainer().getName());
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot delete {0}", e, this);
        }
    }

    /**
     * @param container Bucket
     * @param keys      Key and version ID for versioned object or null
     * @throws ConnectionCanceledException Authentication canceled for MFA delete
     * @throws ServiceException            Service error
     */
    protected void delete(final LoginController prompt, final Path container, final List<ObjectKeyAndVersion> keys) throws ServiceException, BackgroundException {
        if(new S3VersioningFeature(session).getConfiguration(container).isMultifactor()) {
            final Credentials factor = session.mfa(prompt);
            session.getClient().deleteMultipleObjectsWithMFA(container.getName(),
                    keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                    factor.getUsername(),
                    factor.getPassword(),
                    true);
        }
        else {
            if(session.getHost().getHostname().equals(Protocol.S3_SSL.getDefaultHostname())) {
                session.getClient().deleteMultipleObjects(container.getName(),
                        keys.toArray(new ObjectKeyAndVersion[keys.size()]),
                        true);
            }
            else {
                for(ObjectKeyAndVersion k : keys) {
                    session.getClient().deleteObject(container.getName(), k.getKey());
                }
            }
        }
    }

    @Override
    public void rename(final Path renamed) throws BackgroundException {
        try {
            session.message(MessageFormat.format(Locale.localizedString("Renaming {0} to {1}", "Status"),
                    this.getName(), renamed));

            if(attributes().isFile() || attributes().isPlaceholder()) {
                final StorageObject destination = new StorageObject(renamed.getKey());
                // Keep same storage class
                destination.setStorageClass(this.attributes().getStorageClass());
                // Keep encryption setting
                destination.setServerSideEncryptionAlgorithm(this.attributes().getEncryption());
                // Apply non standard ACL
                final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
                destination.setAcl(acl.convert(acl.read(this)));
                // Moving the object retaining the metadata of the original.
                session.getClient().moveObject(this.getContainer().getName(), this.getKey(), renamed.getContainer().getName(),
                        destination, false);
            }
            else if(attributes().isDirectory()) {
                for(Path i : this.list()) {
                    if(!session.isConnected()) {
                        throw new ConnectionCanceledException();
                    }
                    i.rename(new S3Path(session, renamed, i.getName(), i.attributes().getType()));
                }
            }
        }
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Cannot rename {0}", e, this);
        }
    }
}