package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FileActionsApi;
import ch.cyberduck.core.brick.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.brick.io.swagger.client.model.BeginUploadPathBody;
import ch.cyberduck.core.brick.io.swagger.client.model.FileUploadPartEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FilesPathBody;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundExceptionCallable;
import ch.cyberduck.core.threading.DefaultRetryCallable;
import ch.cyberduck.core.threading.ThreadPool;
import ch.cyberduck.core.threading.ThreadPoolFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class BrickUploadFeature extends HttpUploadFeature<Void, MessageDigest> {
    private static final Logger log = Logger.getLogger(BrickUploadFeature.class);

    /**
     * The maximum allowed parts in a multipart upload.
     */
    public static final int MAXIMUM_UPLOAD_PARTS = 10000;

    private final BrickSession session;

    private final Long partsize;
    private final Integer concurrency;

    public BrickUploadFeature(final BrickSession session, final Write<Void> writer) {
        this(session, writer, PreferencesFactory.get().getLong("s3.upload.multipart.size"),
            PreferencesFactory.get().getInteger("s3.upload.multipart.concurrency"));
    }

    public BrickUploadFeature(final BrickSession session, final Write<Void> writer, final Long partsize, final Integer concurrency) {
        super(writer);
        this.session = session;
        this.partsize = partsize;
        this.concurrency = concurrency;
    }

    @Override
    public Void upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final ThreadPool pool = ThreadPoolFactory.get("multipart", concurrency);
        try {
            // Full size of file
            final long size = status.getLength() + status.getOffset();
            final List<Future<TransferStatus>> parts = new ArrayList<>();
            long offset = 0;
            long remaining = status.getLength();
            String ref = null;
            for(int partNumber = 1; remaining > 0; partNumber++) {
                final List<FileUploadPartEntity> uploadPartEntities = new FileActionsApi(new BrickApiClient(session.getApiKey(), session.getClient()))
                    .beginUpload(StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)), new BeginUploadPathBody().ref(ref).part(partNumber));
                for(FileUploadPartEntity uploadPartEntity : uploadPartEntities) {
                    final long length = Math.min(Math.max(size / (MAXIMUM_UPLOAD_PARTS - 1), partsize), remaining);
                    parts.add(this.submit(pool, file, local, throttle, listener, status,
                        uploadPartEntity.getUploadUri(), partNumber, offset, length, callback));
                    remaining -= length;
                    offset += length;
                    ref = uploadPartEntity.getRef();
                }
            }
            for(Future<TransferStatus> future : parts) {
                try {
                    final TransferStatus part = future.get();
                }
                catch(InterruptedException e) {
                    log.error("Part upload failed with interrupt failure");
                    status.setCanceled();
                    throw new ConnectionCanceledException(e);
                }
                catch(ExecutionException e) {
                    log.warn(String.format("Part upload failed with execution failure %s", e.getMessage()));
                    if(e.getCause() instanceof BackgroundException) {
                        throw (BackgroundException) e.getCause();
                    }
                    throw new BackgroundException(e.getCause());
                }
            }
            new FilesApi(new BrickApiClient(session.getApiKey(), session.getClient())).postFilesPath(new FilesPathBody()
                .providedMtime(new DateTime(status.getTimestamp()))
                .ref(ref)
                .action("end"), StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)));
            // Mark parent status as complete
            status.setComplete();
            return null;
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Upload {0} failed", e, file);
        }
        finally {
            // Cancel future tasks
            pool.shutdown(false);
        }
    }

    private Future<TransferStatus> submit(final ThreadPool pool, final Path file, final Local local,
                                          final BandwidthThrottle throttle, final StreamListener listener,
                                          final TransferStatus overall, final String url, final Integer partNumber,
                                          final long offset, final long length, final ConnectionCallback callback) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Submit part %d of %s to queue with offset %d and length %d", partNumber, file, offset, length));
        }
        return pool.execute(new DefaultRetryCallable<>(session.getHost(), new BackgroundExceptionCallable<TransferStatus>() {
            @Override
            public TransferStatus call() throws BackgroundException {
                overall.validate();
                final TransferStatus status = new TransferStatus()
                    .segment(true)
                    .withLength(length)
                    .withOffset(offset);
                status.setUrl(url);
                status.setPart(partNumber);
                status.setHeader(overall.getHeader());
                BrickUploadFeature.super.upload(
                    file, local, throttle, listener, status, overall, status, callback);
                if(log.isInfoEnabled()) {
                    log.info(String.format("Received response for part number %d", partNumber));
                }
                return status;
            }
        }, overall));
    }
}
