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
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.model.StorageObject;

public class S3ThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = LogManager.getLogger(S3ThresholdUploadService.class);

    private final S3Session session;
    private final S3AccessControlListFeature acl;
    private final Long threshold;

    private Write<StorageObject> writer;

    public S3ThresholdUploadService(final S3Session session, final S3AccessControlListFeature acl) {
        this(session, acl, new HostPreferences(session.getHost()).getLong("s3.upload.multipart.threshold"));
    }

    public S3ThresholdUploadService(final S3Session session, final S3AccessControlListFeature acl, final Long threshold) {
        this.session = session;
        this.acl = acl;
        this.threshold = threshold;
        this.writer = new S3WriteFeature(session, acl);
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(this.threshold(status)) {
            return new S3MultipartUploadService(session, writer, acl).append(file, status);
        }
        return new Write.Append(false).withStatus(status);
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        if(this.threshold(status)) {
            try {
                return new S3MultipartUploadService(session, writer, acl).upload(file, local, throttle, listener, status, prompt);
            }
            catch(NotfoundException | InteroperabilityException e) {
                log.warn("Failure {} using multipart upload. Fallback to single upload.", e.getMessage());
                status.append(false);
                try {
                    return new S3SingleUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
                }
                catch(BackgroundException f) {
                    log.warn("Failure {} using single upload. Throw original multipart failure {}", e, e);
                    throw e;
                }
            }
        }
        // Use single upload service
        return new S3SingleUploadService(session, writer).upload(file, local, throttle, listener, status, prompt);
    }

    protected boolean threshold(final TransferStatus status) {
        if(status.getLength() >= threshold) {
            if(!new HostPreferences(session.getHost()).getBoolean("s3.upload.multipart")) {
                log.warn("Multipart upload is disabled with property s3.upload.multipart");
                // Disabled by user
                if(status.getLength() < new HostPreferences(session.getHost()).getLong("s3.upload.multipart.required.threshold")) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Upload<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
