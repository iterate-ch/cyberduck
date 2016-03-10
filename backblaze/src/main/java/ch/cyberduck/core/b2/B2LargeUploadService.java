package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
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
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.request.B2RequestProperties;
import synapticloop.b2.response.B2StartLargeFileResponse;
import synapticloop.b2.response.B2UploadPartResponse;

public class B2LargeUploadService extends HttpUploadFeature<B2UploadPartResponse, MessageDigest> {
    private static final Logger log = Logger.getLogger(B2LargeUploadService.class);

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    private final B2Session session;

    private final ChecksumCompute checksum
            = new SHA1ChecksumCompute();

    private final PathContainerService containerService
            = new B2PathContainerService();

    private ThreadPool pool;

    private Long partsize;

    public B2LargeUploadService(final B2Session session) {
        this(session, new B2PartWriteFeature(session));
    }

    public B2LargeUploadService(final B2Session session, final B2PartWriteFeature writer) {
        this(session, writer, PreferencesFactory.get().getLong("b2.upload.multipart.size"),
                PreferencesFactory.get().getInteger("b2.upload.multipart.concurrency"));
    }

    public B2LargeUploadService(final B2Session session, final B2PartWriteFeature writer, final Long partsize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.partsize = partsize;
        this.pool = new ThreadPool(concurrency, "largeupload");
    }

    @Override
    public B2UploadPartResponse upload(final Path file, final Local local, final BandwidthThrottle throttle,
                                       final StreamListener listener, final TransferStatus status,
                                       final StreamCancelation cancel, final StreamProgress progress) throws BackgroundException {

        try {
            final B2StartLargeFileResponse multipart = session.getClient().startLargeFileUpload(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                    containerService.getKey(file), status.getMime(), Collections.emptyMap());
            final String id = multipart.getFileId();
            final String token = multipart.getUploadAuthToken();

            final List<B2UploadPartResponse> completed = new ArrayList<B2UploadPartResponse>();
            final List<Future<B2UploadPartResponse>> parts = new ArrayList<Future<B2UploadPartResponse>>();
            long remaining = status.getLength();
            long offset = 0;
            // A number from 1 to 10000. The parts uploaded for one file must have contiguous numbers, starting with 1.
            for(int partNumber = 1; remaining > 0; partNumber++) {
                boolean skip = false;
                if(status.isAppend()) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Determine if part number %d can be skipped", partNumber));
                    }
                    for(B2UploadPartResponse c : completed) {
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
                final Long length = Math.min(Math.max((status.getLength() / B2LargeUploadService.MAXIMUM_UPLOAD_PARTS), partsize), remaining);
                if(!skip) {
                    // Submit to queue
                    parts.add(this.submit(file, local, throttle, listener, status, multipart, partNumber, offset, length));
                }
                remaining -= length;
                offset += length;
            }
            for(Future<B2UploadPartResponse> future : parts) {
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

            status.setChecksum(checksum.compute(local.getInputStream()));
            return super.upload(file, local, throttle, listener, status, cancel, progress);
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    private Future<B2UploadPartResponse> submit(final Path part, final Local local,
                                                final BandwidthThrottle throttle, final StreamListener listener,
                                                final TransferStatus overall, final B2StartLargeFileResponse multipart,
                                                final int partNumber, final long offset, final long length) throws BackgroundException {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d",
                    partNumber, part, offset, length));
        }
        return pool.execute(new Callable<B2UploadPartResponse>() {
            @Override
            public B2UploadPartResponse call() throws BackgroundException {
                if(overall.isCanceled()) {
                    return null;
                }
                final TransferStatus status = new TransferStatus()
                        .length(length)
                        .skip(offset);
                final InputStream in = new BoundedInputStream(local.getInputStream(), offset + length);
                try {
                    StreamCopier.skip(in, offset);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
                status.setChecksum(new SHA256ChecksumCompute().compute(in));

                final Map<String, String> requestParameters = new HashMap<String, String>();
                requestParameters.put(B2RequestProperties.KEY_FILE_ID, multipart.getFileId());
                requestParameters.put(null, String.valueOf(partNumber));
                status.parameters(requestParameters);

                return B2LargeUploadService.super.upload(
                        part, local, throttle, listener, status, overall, overall);
            }
        });
    }

    @Override
    protected InputStream decorate(final InputStream in, final MessageDigest digest) throws IOException {
        if(null == digest) {
            return super.decorate(in, null);
        }
        else {
            return new DigestInputStream(super.decorate(in, digest), digest);
        }
    }

    @Override
    protected MessageDigest digest() throws IOException {
        MessageDigest digest = null;
        if(PreferencesFactory.get().getBoolean("b2.upload.checksum")) {
            try {
                digest = MessageDigest.getInstance("SHA1");
            }
            catch(NoSuchAlgorithmException e) {
                throw new IOException(e.getMessage(), e);
            }
        }
        return digest;
    }

    @Override
    protected void post(final Path file, final MessageDigest digest, final B2UploadPartResponse response) throws BackgroundException {
        this.verify(file, digest, Checksum.parse(response.getContentSha1()));
    }

    protected void verify(final Path file, final MessageDigest digest, final Checksum checksum) throws ChecksumException {
        final String expected = Hex.encodeHexString(digest.digest());
        // Compare our locally-calculated hash with the ETag returned by S3.
        if(!checksum.equals(Checksum.parse(expected))) {
            throw new ChecksumException(MessageFormat.format(LocaleFactory.localizedString("Upload {0} failed", "Error"), file.getName()),
                    MessageFormat.format("Mismatch between MD5 hash {0} of uploaded data and ETag {1} returned by the server",
                            expected, checksum.hash));
        }
    }
}
