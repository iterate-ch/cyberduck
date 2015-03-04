package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ThreadPool;

import org.apache.log4j.Logger;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @version $Id$
 */
public class S3MultipartCopyFeature implements Copy {
    private static final Logger log = Logger.getLogger(S3MultipartCopyFeature.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private ThreadPool pool
            = new ThreadPool(PreferencesFactory.get().getInteger("s3.upload.multipart.concurrency"), "multipart");

    /**
     * A split smaller than 5M is not allowed
     */
    private Long partsize
            = PreferencesFactory.get().getLong("s3.upload.multipart.size");

    public S3MultipartCopyFeature(final S3Session session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        if(source.isFile()) {
            // Keep same storage class
            final String storageClass = source.attributes().getStorageClass();
            // Keep encryption setting
            final String encryptionAlgorithm = source.attributes().getEncryption();
            // Apply non standard ACL
            final S3AccessControlListFeature accessControlListFeature = new S3AccessControlListFeature(session);
            final Acl acl = accessControlListFeature.getPermission(source);
            this.copy(source, copy, storageClass, encryptionAlgorithm, acl);
        }
    }

    protected void copy(final Path source, final Path copy, final String storageClass,
                        final String encryptionAlgorithm,
                        final Acl acl) throws BackgroundException {
        if(source.isFile()) {
            final S3Object destination = new S3Object(containerService.getKey(copy));
            // Copying object applying the metadata of the original
            destination.setStorageClass(storageClass);
            destination.setServerSideEncryptionAlgorithm(encryptionAlgorithm);
            destination.setAcl(new S3AccessControlListFeature(session).convert(acl));
            try {
                final List<MultipartPart> completed = new ArrayList<MultipartPart>();
                // ID for the initiated multipart upload.
                final MultipartUpload multipart = session.getClient().multipartStartUpload(
                        containerService.getContainer(copy).getName(), destination);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Multipart upload started for %s with ID %s",
                            multipart.getObjectKey(), multipart.getUploadId()));
                }
                final long size = source.attributes().getSize();
                long remaining = size;
                long offset = 0;
                final List<Future<MultipartPart>> parts = new ArrayList<Future<MultipartPart>>();
                if(0 == remaining) {
                    parts.add(this.submit(source, copy, multipart, 1, offset, 0L));
                }
                for(int partNumber = 1; remaining > 0; partNumber++) {
                    boolean skip = false;
                    // Last part can be less than 5 MB. Adjust part size.
                    final Long length = Math.min(Math.max((size / S3MultipartService.MAXIMUM_UPLOAD_PARTS), partsize), remaining);
                    // Submit to queue
                    parts.add(this.submit(source, copy, multipart, partNumber, offset, length));
                    remaining -= length;
                    offset += length;
                }
                for(Future<MultipartPart> future : parts) {
                    try {
                        completed.add(future.get());
                    }
                    catch(InterruptedException e) {
                        log.error("Part upload failed with interrupt failure");
                        throw new ConnectionCanceledException(e);
                    }
                    catch(ExecutionException e) {
                        log.warn(String.format("Part upload failed with execution failure %s", e.getMessage()));
                        if(e.getCause() instanceof BackgroundException) {
                            throw (BackgroundException) e.getCause();
                        }
                        throw new BackgroundException(e);
                    }
                }
                // Combining all the given parts into the final object. Processing of a Complete Multipart Upload request
                // could take several minutes to complete. Because a request could fail after the initial 200 OK response
                // has been sent, it is important that you check the response body to determine whether the request succeeded.
                final MultipartCompleted complete = session.getClient().multipartCompleteUpload(multipart, completed);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Completed multipart upload for %s with checksum %s",
                            complete.getObjectKey(), complete.getEtag()));
                }
            }
            catch(ServiceException e) {
                throw new ServiceExceptionMappingService().map("Cannot copy {0}", e, source);
            }
            finally {
                pool.shutdown();
            }
        }
    }

    private Future<MultipartPart> submit(final Path source, final Path copy,
                                         final MultipartUpload multipart,
                                         final int partNumber, final long offset, final long length) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, source, offset, length));
        }
        return pool.execute(new Callable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                try {
                    final MultipartPart part = session.getClient().multipartUploadPartCopy(multipart, partNumber,
                            containerService.getContainer(source).getName(), containerService.getKey(source));
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Received response %s for part number %d", part, partNumber));
                    }
                    // Populate part with response data that is accessible via the object's metadata
                    return new MultipartPart(partNumber, part.getLastModified(),
                            part.getEtag(), part.getSize());
                }
                catch(S3ServiceException e) {
                    throw new ServiceExceptionMappingService().map("Cannot copy {0}", e, source);
                }
            }
        });
    }
}
