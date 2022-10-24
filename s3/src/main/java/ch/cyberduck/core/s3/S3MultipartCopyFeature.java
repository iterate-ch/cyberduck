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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class S3MultipartCopyFeature extends S3CopyFeature {
    private static final Logger log = LogManager.getLogger(S3MultipartCopyFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;

    private final ThreadPool pool;

    /**
     * A split smaller than 5M is not allowed
     */
    private final Long partsize;

    public S3MultipartCopyFeature(final S3Session session, final S3AccessControlListFeature acl) {
        super(session, acl);
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.pool = ThreadPoolFactory.get("multipart", new HostPreferences(session.getHost()).getInteger("s3.upload.multipart.concurrency"));
        this.partsize = new HostPreferences(session.getHost()).getLong("s3.copy.multipart.size");
    }

    @Override
    protected String copy(final Path source, final S3Object destination, final TransferStatus status, final StreamListener listener) throws BackgroundException {
        try {
            final List<MultipartPart> completed = new ArrayList<>();
            // ID for the initiated multipart upload.
            final MultipartUpload multipart = session.getClient().multipartStartUpload(
                    destination.getBucketName(), destination);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Multipart upload started for %s with ID %s",
                        multipart.getObjectKey(), multipart.getUploadId()));
            }
            final long size = status.getLength();
            long remaining = size;
            long offset = 0;
            final List<Future<MultipartPart>> parts = new ArrayList<>();
            for(int partNumber = 1; remaining > 0; partNumber++) {
                // Last part can be less than 5 MB. Adjust part size.
                final long length = Math.min(Math.max((size / S3DefaultMultipartService.MAXIMUM_UPLOAD_PARTS), partsize), remaining);
                // Submit to queue
                parts.add(this.submit(source, multipart, partNumber, offset, length));
                remaining -= length;
                offset += length;
            }
            for(Future<MultipartPart> f : parts) {
                final MultipartPart part = Interruptibles.await(f);
                completed.add(part);
                listener.sent(part.getSize());
            }
            // Combining all the given parts into the final object. Processing of a Complete Multipart Upload request
            // could take several minutes to complete. Because a request could fail after the initial 200 OK response
            // has been sent, it is important that you check the response body to determine whether the request succeeded.
            final MultipartCompleted complete = session.getClient().multipartCompleteUpload(multipart, completed);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Completed multipart upload for %s with checksum %s",
                        complete.getObjectKey(), complete.getEtag()));
            }
            return complete.getVersionId();
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        finally {
            pool.shutdown(false);
        }
    }

    private Future<MultipartPart> submit(final Path source,
                                         final MultipartUpload multipart,
                                         final int partNumber, final long offset, final long length) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, source, offset, length));
        }
        return pool.execute(new Callable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                try {
                    final HttpRange range = HttpRange.byLength(offset, length);
                    final Path bucket = containerService.getContainer(source);
                    final MultipartPart part = session.getClient().multipartUploadPartCopy(multipart, partNumber,
                            bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(source),
                            null, null, null, null, range.getStart(), range.getEnd(), source.attributes().getVersionId());
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Received response %s for part number %d", part, partNumber));
                    }
                    // Populate part with response data that is accessible via the object's metadata
                    return new MultipartPart(partNumber,
                            null == part.getLastModified() ? new Date(System.currentTimeMillis()) : part.getLastModified(),
                            null == part.getEtag() ? StringUtils.EMPTY : part.getEtag(),
                            part.getSize());
                }
                catch(S3ServiceException e) {
                    throw new S3ExceptionMappingService().map("Cannot copy {0}", e, source);
                }
            }
        });
    }
}
