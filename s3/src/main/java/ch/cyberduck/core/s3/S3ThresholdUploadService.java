package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

public class S3ThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = Logger.getLogger(S3ThresholdUploadService.class);

    private final Preferences preferences
        = PreferencesFactory.get();

    private final S3Session session;
    private final Long threshold;

    private Write<StorageObject> writer;

    public S3ThresholdUploadService(final S3Session session) {
        this(session, PreferencesFactory.get().getLong("s3.upload.multipart.threshold"));
    }

    public S3ThresholdUploadService(final S3Session session, final Long threshold) {
        this.session = session;
        this.threshold = threshold;
        this.writer = new S3WriteFeature(session);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return writer.append(file, length, cache);
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        if(status.getLength() >= threshold) {
            if(!preferences.getBoolean("s3.upload.multipart")) {
                log.warn("Multipart upload is disabled with property s3.upload.multipart");
                // Disabled by user
                if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                    // Use single upload service
                    return new S3SingleUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
                }
            }
            try {
                return new S3MultipartUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
            }
            catch(NotfoundException | InteroperabilityException e) {
                log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
            }
        }
        status.append(false);
        // Use single upload service
        return new S3SingleUploadService(session, new Write<StorageObject>() {
            @Override
            public StatusOutputStream<StorageObject> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
                return writer.write(file, status, callback);
            }

            @Override
            public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
                return writer.append(file, length, cache);
            }

            @Override
            public boolean temporary() {
                return writer.temporary();
            }

            @Override
            public boolean random() {
                return writer.random();
            }

            @Override
            public ChecksumCompute checksum(final Path file, final TransferStatus status) {
                return ChecksumComputeFactory.get(HashAlgorithm.sha256);
            }
        }).upload(file, local, throttle, listener, status, prompt);
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
