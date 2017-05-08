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

    private Long multipartThreshold
            = preferences.getLong("s3.upload.multipart.threshold");

    private Write<StorageObject> writer;

    public S3ThresholdUploadService(final S3Session session) {
        this(session, PreferencesFactory.get().getLong("s3.upload.multipart.threshold"));
    }

    public S3ThresholdUploadService(final S3Session session, final Long multipartThreshold) {
        this.session = session;
        this.multipartThreshold = multipartThreshold;
        this.writer = new S3WriteFeature(session);
    }

    @Override
    public Write.Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return writer.append(file, length, cache);
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        if(status.getLength() > multipartThreshold) {
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
        // Use single upload service
        return new S3SingleUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
    }

    public S3ThresholdUploadService withMultipartThreshold(final Long threshold) {
        this.multipartThreshold = threshold;
        return this;
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}