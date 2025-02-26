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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

public class S3MultipartUploadService extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = LogManager.getLogger(S3MultipartUploadService.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3MultipartService multipartService;
    private final S3AccessControlListFeature acl;

    private Write<StorageObject> writer;
    /**
     * A split smaller than 5M is not allowed
     */
    private final Long partsize;
    private final Integer concurrency;

    public S3MultipartUploadService(final S3Session session, final Write<StorageObject> writer, final S3AccessControlListFeature acl) {
        this(session, writer, acl, HostPreferencesFactory.get(session.getHost()).getLong("s3.upload.multipart.size"),
                HostPreferencesFactory.get(session.getHost()).getInteger("s3.upload.multipart.concurrency"));
    }

    public S3MultipartUploadService(final S3Session session, final Write<StorageObject> writer, final S3AccessControlListFeature acl, final Long partsize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.multipartService = new S3DefaultMultipartService(session);
        this.containerService = session.getFeature(PathContainerService.class);
        this.writer = writer;
        this.acl = acl;
        this.partsize = Math.max(HostPreferencesFactory.get(session.getHost()).getLong("s3.upload.multipart.partsize.minimum"), partsize);
        this.concurrency = concurrency;
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener streamListener,
                                final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            MultipartUpload multipart = null;
            try {
                if(status.isAppend()) {
                    final List<MultipartUpload> list = multipartService.find(file);
                    if(!list.isEmpty()) {
                        multipart = list.iterator().next();
                    }
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn("Ignore failure listing incomplete multipart uploads. {}", e.getMessage());
            }
            final Path bucket = containerService.getContainer(file);
            final List<MultipartPart> completed = new ArrayList<>();
            // Not found or new upload
            if(null == multipart) {
                log.info("No pending multipart upload found");
                final S3Object object = new S3WriteFeature(session, acl).getDetails(file, status);
                // ID for the initiated multipart upload.
                multipart = session.getClient().multipartStartUpload(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), object);
                log.debug("Multipart upload started for {} with ID {}", multipart.getObjectKey(), multipart.getUploadId());
                multipart.setBucketName(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
            }
            else {
                if(status.isAppend()) {
                    // Add already completed parts
                    completed.addAll(multipartService.list(multipart));
                }
            }
            // Full size of file
            final long size = status.getOffset() + status.getLength();
            final List<Future<MultipartPart>> parts = new ArrayList<>();
            long remaining = status.getLength();
            long offset = 0;
            for(int partNumber = 1; remaining > 0; partNumber++) {
                boolean skip = false;
                if(status.isAppend()) {
                    log.info("Determine if part number {} can be skipped", partNumber);
                    for(MultipartPart c : completed) {
                        if(c.getPartNumber().equals(partNumber)) {
                            log.info("Skip completed part number {}", partNumber);
                            skip = true;
                            offset += c.getSize();
                            break;
                        }
                    }
                }
                if(!skip) {
                    // Last part can be less than 5 MB. Adjust part size.
                    final long length = Math.min(Math.max((size / (S3DefaultMultipartService.MAXIMUM_UPLOAD_PARTS - 1)), partsize), remaining);
                    // Submit to queue
                    parts.add(this.submit(pool, file, local, throttle, streamListener, status, multipart, partNumber, offset, length, callback));
                    remaining -= length;
                    offset += length;
                }
            }
            completed.addAll(Interruptibles.awaitAll(parts));
            // Combining all the given parts into the final object. Processing of a Complete Multipart Upload request
            // could take several minutes to complete. Because a request could fail after the initial 200 OK response
            // has been sent, it is important that you check the response body to determine whether the request succeeded.
            multipart.setBucketName(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
            progress.message(MessageFormat.format(LocaleFactory.localizedString("Finalize {0}", "Status"),
                    file.getName()));
            final MultipartCompleted complete = session.getClient().multipartCompleteUpload(multipart, completed);
            log.info("Completed multipart upload for {} with {} parts and checksum {}", complete.getObjectKey(), completed.size(), complete.getEtag());
            if(file.getType().contains(Path.Type.encrypted)) {
                log.warn("Skip checksum verification for {} with client side encryption enabled", file);
            }
            else {
                if(S3Session.isAwsHostname(session.getHost().getHostname())) {
                    completed.sort(new MultipartPart.PartNumberComparator());
                    final StringBuilder concat = new StringBuilder();
                    for(MultipartPart part : completed) {
                        concat.append(part.getEtag());
                    }
                    final String expected = String.format("%s-%d",
                            ChecksumComputeFactory.get(HashAlgorithm.md5).compute(concat.toString()), completed.size());
                    final String reference = StringUtils.remove(complete.getEtag(), "\"");
                    if(!StringUtils.equalsIgnoreCase(expected, reference)) {
                        throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                                MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                        expected, reference));
                    }
                }
            }
            final StorageObject object = new StorageObject(containerService.getKey(file));
            object.setETag(complete.getEtag());
            object.setContentLength(size);
            object.setStorageClass(multipart.getStorageClass());
            if(multipart.getMetadata() != null) {
                object.addAllMetadata(multipart.getMetadata());
            }
            // Mark parent status as complete
            status.withResponse(new S3AttributesAdapter(session.getHost()).toAttributes(object)).setComplete();
            return object;
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Upload {0} failed", e, file);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    private Future<MultipartPart> submit(final ThreadPool pool, final Path file, final Local local,
                                         final BandwidthThrottle throttle, final StreamListener listener,
                                         final TransferStatus overall, final MultipartUpload multipart,
                                         final int partNumber, final long offset, final long length, final ConnectionCallback callback) {
        log.info("Submit part {} of {} to queue with offset {} and length {}", partNumber, file, offset, length);
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .withLength(length)
                        .withOffset(offset);
                final Map<String, String> requestParameters = new HashMap<>();
                requestParameters.put("uploadId", multipart.getUploadId());
                requestParameters.put("partNumber", String.valueOf(partNumber));
                status.setParameters(requestParameters);
                status.setPart(partNumber);
                status.setHeader(overall.getHeader());
                final Set<Checksum> checksum = writer.checksum(file, status).computeAll(local.getInputStream(), status);
                switch(session.getSignatureVersion()) {
                    case AWS4HMACSHA256:
                        checksum.stream().filter(v -> v.algorithm.equals(HashAlgorithm.sha256)).findFirst().ifPresent(status::setChecksum);
                        break;
                }
                status.setSegment(true);
                final Optional<Checksum> md5 = checksum.stream().filter(v -> v.algorithm.equals(HashAlgorithm.md5)).findFirst();
                if(md5.isPresent()) {
                    final HashMap<String, String> metadata = new HashMap<>(status.getMetadata());
                    metadata.put(HttpHeaders.CONTENT_MD5, md5.get().base64);
                    status.setMetadata(metadata);
                }
                final StorageObject part = S3MultipartUploadService.this.upload(
                        file, local, throttle, counter, status, overall, status, callback);
                log.info("Received response {} for part number {}", part, partNumber);
                // Populate part with response data that is accessible via the object's metadata
                return new MultipartPart(partNumber,
                        null == part.getLastModifiedDate() ? new Date(System.currentTimeMillis()) : part.getLastModifiedDate(),
                        null == part.getETag() ? StringUtils.EMPTY : part.getETag(),
                        part.getContentLength());

            }
        }, overall, counter));
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final S3DefaultMultipartService multipartService = new S3DefaultMultipartService(session);
            final List<MultipartUpload> upload = multipartService.find(file);
            if(!upload.isEmpty()) {
                Long size = 0L;
                for(MultipartPart completed : multipartService.list(upload.iterator().next())) {
                    size += completed.getSize();
                }
                return new Write.Append(true).withStatus(status).withOffset(size);
            }
        }
        catch(AccessDeniedException | InteroperabilityException e) {
            log.warn("Ignore failure listing incomplete multipart uploads. {}", e.getMessage());
        }
        return Write.override;
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }
}
