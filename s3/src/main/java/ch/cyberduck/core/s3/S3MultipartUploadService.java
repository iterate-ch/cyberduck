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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.MD5ChecksumCompute;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.DefaultThreadPool;
import ch.cyberduck.core.threading.RetryCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.input.BoundedInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.MultipartCompleted;
import org.jets3t.service.model.MultipartPart;
import org.jets3t.service.model.MultipartUpload;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.model.StorageObject;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class S3MultipartUploadService extends HttpUploadFeature<StorageObject, MessageDigest> {
    private static final Logger log = Logger.getLogger(S3MultipartUploadService.class);

    private S3Session session;

    private PathContainerService containerService
            = new S3PathContainerService();

    private S3DefaultMultipartService multipartService;

    /**
     * At any point, at most <tt>nThreads</tt> threads will be active processing tasks.
     */
    private ThreadPool pool;

    /**
     * A split smaller than 5M is not allowed
     */
    private Long partsize;

    private Preferences preferences
            = PreferencesFactory.get();

    public S3MultipartUploadService(final S3Session session) {
        this(session, PreferencesFactory.get().getLong("s3.upload.multipart.size"),
                PreferencesFactory.get().getInteger("s3.upload.multipart.concurrency"));
    }

    public S3MultipartUploadService(final S3Session session, final Long partsize, final Integer concurrency) {
        super(new S3WriteFeature(session));
        this.session = session;
        this.pool = new DefaultThreadPool(concurrency, "multipart");
        this.multipartService = new S3DefaultMultipartService(session);
        this.partsize = partsize;
    }

    @Override
    public StorageObject upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            MultipartUpload multipart = null;
            if(status.isAppend()) {
                final List<MultipartUpload> list = multipartService.find(file);
                if(!list.isEmpty()) {
                    multipart = list.iterator().next();
                }
            }
            final List<MultipartPart> completed = new ArrayList<MultipartPart>();
            // Not found or new upload
            if(null == multipart) {
                if(log.isInfoEnabled()) {
                    log.info("No pending multipart upload found");
                }
                final S3Object object = new S3WriteFeature(session)
                        .getDetails(containerService.getKey(file), status);
                // ID for the initiated multipart upload.
                multipart = session.getClient().multipartStartUpload(
                        containerService.getContainer(file).getName(), object);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Multipart upload started for %s with ID %s",
                            multipart.getObjectKey(), multipart.getUploadId()));
                }
            }
            else {
                if(status.isAppend()) {
                    // Add already completed parts
                    completed.addAll(multipartService.list(multipart));
                }
            }
            try {
                final List<Future<MultipartPart>> parts = new ArrayList<Future<MultipartPart>>();
                long remaining = status.getLength();
                long offset = 0;
                for(int partNumber = 1; remaining > 0; partNumber++) {
                    boolean skip = false;
                    if(status.isAppend()) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Determine if part number %d can be skipped", partNumber));
                        }
                        for(MultipartPart c : completed) {
                            if(c.getPartNumber().equals(partNumber)) {
                                if(log.isInfoEnabled()) {
                                    log.info(String.format("Skip completed part number %d", partNumber));
                                }
                                skip = true;
                                break;
                            }
                        }
                    }
                    // Last part can be less than 5 MB. Adjust part size.
                    final Long length = Math.min(Math.max((status.getLength() / S3DefaultMultipartService.MAXIMUM_UPLOAD_PARTS), partsize), remaining);
                    if(!skip) {
                        // Submit to queue
                        parts.add(this.submit(file, local, throttle, listener, status, multipart, partNumber, offset, length));
                    }
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
                final StringBuilder concat = new StringBuilder();
                for(MultipartPart part : completed) {
                    concat.append(part.getEtag());
                }
                final String expected = String.format("%s-%d",
                        new MD5ChecksumCompute().compute(concat.toString()), completed.size());
                final String reference;
                if(complete.getEtag().startsWith("\"") && complete.getEtag().endsWith("\"")) {
                    reference = complete.getEtag().substring(1, complete.getEtag().length() - 1);
                }
                else {
                    reference = complete.getEtag();
                }
                if(!expected.equals(reference)) {
                    throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                            MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                                    expected, reference));
                }
                // Mark parent status as complete
                status.setComplete();
                final StorageObject object = new StorageObject(containerService.getKey(file));
                object.setETag(complete.getEtag());
                return object;
            }
            finally {
                // Cancel future tasks
                pool.shutdown();
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    private Future<MultipartPart> submit(final Path file, final Local local,
                                         final BandwidthThrottle throttle, final StreamListener listener,
                                         final TransferStatus overall, final MultipartUpload multipart,
                                         final int partNumber, final long offset, final long length) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, file, offset, length));
        }
        return pool.execute(new RetryCallable<MultipartPart>() {
            @Override
            public MultipartPart call() throws BackgroundException {
                final Map<String, String> requestParameters = new HashMap<String, String>();
                requestParameters.put("uploadId", multipart.getUploadId());
                requestParameters.put("partNumber", String.valueOf(partNumber));
                final TransferStatus status = new TransferStatus()
                        .length(length)
                        .skip(offset)
                        .parameters(requestParameters);
                try {
                    if(overall.isCanceled()) {
                        return null;
                    }
                    switch(session.getSignatureVersion()) {
                        case AWS4HMACSHA256:
                            final InputStream in = new BoundedInputStream(local.getInputStream(), offset + length);
                            try {
                                StreamCopier.skip(in, offset);
                            }
                            catch(IOException e) {
                                throw new DefaultIOExceptionMappingService().map(e);
                            }
                            status.setChecksum(new SHA256ChecksumCompute().compute(in));
                            break;
                    }
                    final StorageObject part = S3MultipartUploadService.super.upload(
                            file, local, throttle, listener, status, overall, overall);
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Received response %s for part number %d", part, partNumber));
                    }
                    // Populate part with response data that is accessible via the object's metadata
                    return new MultipartPart(partNumber,
                            null == part.getLastModifiedDate() ? new Date(System.currentTimeMillis()) : part.getLastModifiedDate(),
                            null == part.getETag() ? StringUtils.EMPTY : part.getETag(),
                            part.getContentLength());
                }
                catch(BackgroundException e) {
                    // Discard sent bytes in overall progress if there is an error reply for segment.
                    final long sent = status.getOffset() - offset;
                    overall.progress(-sent);
                    if(this.retry(e, new DisabledProgressListener(), overall)) {
                        return this.call();
                    }
                    else {
                        throw e;
                    }
                }
            }
        });
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        if(null == digest) {
            log.warn("MD5 calculation disabled");
            return super.decorate(in, null);
        }
        else {
            return new DigestInputStream(super.decorate(in, digest), digest);
        }
    }

    @Override
    protected MessageDigest digest() throws IOException {
        MessageDigest digest = null;
        if(PreferencesFactory.get().getBoolean("s3.upload.md5")) {
            try {
                digest = MessageDigest.getInstance("MD5");
            }
            catch(NoSuchAlgorithmException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return digest;
    }

    @Override
    protected void post(final Path file, final MessageDigest digest, final StorageObject multipart) throws BackgroundException {
        if(null == multipart.getServerSideEncryptionAlgorithm()) {
            this.verify(file, digest, Checksum.parse(multipart.getETag()));
        }
    }
}
