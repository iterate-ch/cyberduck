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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.concurrency.Interruptibles;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.SegmentRetryCallable;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
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
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class S3MultipartUploadService extends HttpUploadFeature<StorageObject> {

    private static final Logger log = LogManager.getLogger(S3MultipartUploadService.class);

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3MultipartService multipartService;
    private final S3AccessControlListFeature acl;

    /**
     * A split smaller than 5M is not allowed
     */
    private final Long partsize;
    private final Integer concurrency;

    public S3MultipartUploadService(final S3Session session, final S3AccessControlListFeature acl) {
        this(session, acl, HostPreferencesFactory.get(session.getHost()).getLong("s3.upload.multipart.size"),
                HostPreferencesFactory.get(session.getHost()).getInteger("s3.upload.multipart.concurrency"));
    }

    public S3MultipartUploadService(final S3Session session, final S3AccessControlListFeature acl, final Long partsize, final Integer concurrency) {
        this.session = session;
        this.multipartService = new S3DefaultMultipartService(session);
        this.containerService = new S3PathContainerService(session.getHost());
        this.acl = acl;
        this.partsize = Math.max(HostPreferencesFactory.get(session.getHost()).getLong("s3.upload.multipart.partsize.minimum"), partsize);
        this.concurrency = concurrency;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.md5);
    }

    @Override
    public StorageObject upload(final Write<StorageObject> write, final Path file, final Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            MultipartUpload multipart = null;
            try {
                if(status.isAppend()) {
                    final List<MultipartUpload> list = multipartService.find(file);
                    if(!list.isEmpty()) {
                        multipart = list.iterator().next();
                        log.debug("Resume multipart upload {}", multipart);
                    }
                }
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn("Ignore failure listing incomplete multipart uploads. {}", e.getMessage());
            }
            final Path bucket = containerService.getContainer(file);
            final List<MultipartPart> completed = new ArrayList<>();
            // Checksum of parts calculated prior to the transfer
            final Map<Integer, Checksum> checksums = new ConcurrentHashMap<>();
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
                    log.debug("Add completed parts {} for multipart upload {}", completed, multipart);
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
                    parts.add(this.submit(pool, write, file, local, throttle, listener, status, multipart, partNumber, offset, length, checksums, callback));
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
            final StorageObject object = new StorageObject(containerService.getKey(file));
            object.setETag(complete.getEtag());
            object.setContentLength(size);
            object.setStorageClass(multipart.getStorageClass());
            if(multipart.getMetadata() != null) {
                object.addAllMetadata(multipart.getMetadata());
            }
            // Only objects stored as plaintext or encrypted with SSE-S3 have an ETag derived from the MD5 of the data
            object.setServerSideEncryptionAlgorithm(status.getEncryption().algorithm);
            final PathAttributes attributes = new S3AttributesAdapter(session.getHost()).toAttributes(object);
            if(HashAlgorithm.md5_multipart == attributes.getChecksum().algorithm) {
                final Checksum expected = this.checksum(completed, checksums);
                if(!Checksum.NONE.equals(expected)) {
                    // Compared with the checksum returned by the server by the upload filter when the upload is complete
                    status.setVerify(expected.algorithm).setDigest(expected);
                }
            }
            // Mark parent status as complete
            status.setResponse(attributes).setComplete();
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

    /**
     * @param completed Parts in the multipart upload
     * @param checksums Checksum of the parts calculated prior to the transfer. Falls back to the ETag of parts not
     *                  uploaded in this transfer when resuming
     * @return Checksum calculated like the ETag of an object uploaded in multiple parts
     */
    private Checksum checksum(final List<MultipartPart> completed, final Map<Integer, Checksum> checksums) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final List<MultipartPart> sorted = new ArrayList<>(completed);
            sorted.sort(new MultipartPart.PartNumberComparator());
            for(MultipartPart part : sorted) {
                final Checksum md5 = checksums.get(part.getPartNumber());
                digest.update(Hex.decodeHex(null == md5 ? StringUtils.remove(part.getEtag(), '"') : md5.hex));
            }
            return new Checksum(HashAlgorithm.md5_multipart, String.format("%s-%d", Hex.encodeHexString(digest.digest()), sorted.size()));
        }
        catch(NoSuchAlgorithmException | DecoderException e) {
            log.warn("Failure {} calculating checksum of {} parts", e, completed.size());
            return Checksum.NONE;
        }
    }

    private Future<MultipartPart> submit(final ThreadPool pool, final Write<StorageObject> write, final Path file, final Local local,
                                         final BandwidthThrottle throttle, final StreamListener listener,
                                         final TransferStatus overall, final MultipartUpload multipart,
                                         final int partNumber, final long offset, final long length, final Map<Integer, Checksum> checksums, final ConnectionCallback callback) throws ConnectionCanceledException {
        overall.validate();
        log.info("Submit part {} of {} to queue with offset {} and length {}", partNumber, file, offset, length);
        final BytecountStreamListener counter = new BytecountStreamListener(listener);
        return pool.execute(new SegmentRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                        .setParent(overall)
                        .setSegment(true)
                        .setLength(length)
                        .setOffset(offset);
                final Map<String, String> requestParameters = new HashMap<>();
                requestParameters.put("uploadId", multipart.getUploadId());
                requestParameters.put("partNumber", String.valueOf(partNumber));
                status.setParameters(requestParameters);
                status.setPart(partNumber);
                status.setHeader(overall.getHeader());
                final HashMap<String, String> metadata = new HashMap<>(status.getMetadata());
                // Content-MD5 is required for upload to a bucket with Object Lock. The payload is not signed.
                final Checksum md5 = S3MultipartUploadService.this.checksum(file, status).compute(local.getInputStream(), status);
                checksums.put(partNumber, md5);
                metadata.put(HttpHeaders.CONTENT_MD5, md5.base64);
                status.setMetadata(metadata);
                final StorageObject part = S3MultipartUploadService.this.upload(
                        write, file, local, throttle, ProgressListener.noop, counter, status, overall, status, callback);
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
    public EnumSet<Flags> features(final Path file, final TransferStatus status) {
        return EnumSet.of(Flags.checksumResponse);
    }
}
