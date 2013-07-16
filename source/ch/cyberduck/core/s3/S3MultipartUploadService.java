package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.StreamListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.DefaultIOExceptionMappingService;
import ch.cyberduck.core.exception.ServiceExceptionMappingService;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.threading.NamedThreadFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.StorageObject;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @version $Id$
 */
public class S3MultipartUploadService extends S3SingleUploadService {
    private static final Logger log = Logger.getLogger(S3MultipartUploadService.class);

    /**
     * Default minimum part size for upload parts.
     */
    private static final int DEFAULT_MINIMUM_UPLOAD_PART_SIZE =
            Preferences.instance().getInteger("s3.upload.multipart.size");

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;


    private S3Session session;

    private PathContainerService containerService = new PathContainerService();

    /**
     * At any point, at most
     * <tt>nThreads</tt> threads will be active processing tasks.
     */
    private ExecutorService pool = Executors.newFixedThreadPool(
            Preferences.instance().getInteger("s3.upload.multipart.concurency"), new NamedThreadFactory("multipart"));


    public S3MultipartUploadService(final S3Session session) {
        super(session);
        this.session = session;
    }

    public void upload(final Path file, final BandwidthThrottle throttle, final StreamListener listener, final TransferStatus status,
                       final StorageObject object) throws BackgroundException {
        try {
            MultipartUpload multipart = null;
            if(status.isResume()) {
                // This operation lists in-progress multipart uploads. An in-progress multipart upload is a
                // multipart upload that has been initiated, using the Initiate Multipart Upload request, but has
                // not yet been completed or aborted.
                final List<MultipartUpload> uploads = session.getClient().multipartListUploads(
                        containerService.getContainer(file).getName());
                for(MultipartUpload upload : uploads) {
                    if(!upload.getBucketName().equals(containerService.getContainer(file).getName())) {
                        continue;
                    }
                    if(!upload.getObjectKey().equals(containerService.getKey(file))) {
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
                        containerService.getContainer(file).getName(), containerService.getKey(file), metadata);
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
                        parts.add(this.submitPart(file, throttle, listener, status, multipart, partNumber, marker, length));
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
        catch(ServiceException e) {
            throw new ServiceExceptionMappingService().map("Upload failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload failed", e, file);
        }
    }

    private Future<MultipartPart> submitPart(final Path file,
                                             final BandwidthThrottle throttle, final StreamListener listener,
                                             final TransferStatus status, final MultipartUpload multipart,
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
                        in = file.getLocal().getInputStream();
                    }
                    else {
                        in = new DigestInputStream(file.getLocal().getInputStream(), digest);
                    }
                    out = write(file, new StorageObject(containerService.getKey(file)), length, requestParameters);
                    session.upload(file, out, in, throttle, listener, offset, length, status);
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
                        throw new ServiceExceptionMappingService().map("Upload failed", e, file);
                    }
                }
                // Populate part with response data that is accessible via the object's metadata
                return new MultipartPart(partNumber, part.getLastModifiedDate(),
                        part.getETag(), part.getContentLength());
            }
        });
    }
}
