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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.model.StorageObject;

import java.util.EnumSet;

public class S3ThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = LogManager.getLogger(S3ThresholdUploadService.class);

    private final S3Session session;
    private final S3AccessControlListFeature acl;
    private final Long threshold;
    private final S3SingleUploadService single;
    private final S3MultipartUploadService multipart;

    public S3ThresholdUploadService(final S3Session session, final S3AccessControlListFeature acl) {
        this(session, acl, HostPreferencesFactory.get(session.getHost()).getLong("s3.upload.multipart.threshold"));
    }

    public S3ThresholdUploadService(final S3Session session, final S3AccessControlListFeature acl, final Long threshold) {
        this.session = session;
        this.acl = acl;
        this.threshold = threshold;
        this.single = new S3SingleUploadService(session);
        this.multipart = new S3MultipartUploadService(session, acl);
    }

    @Override
    public Write.Append append(final Path file, final TransferStatus status) throws BackgroundException {
        if(this.threshold(status)) {
            return new S3MultipartUploadService(session, acl).append(file, status);
        }
        return new Write.Append(false).withStatus(status);
    }

    @Override
    public StorageObject upload(final Write<StorageObject> write, final Path file, Local local, final BandwidthThrottle throttle, final ProgressListener progress, final StreamListener listener,
                                final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        if(this.threshold(status)) {
            try {
                return multipart.upload(write, file, local, throttle, progress, listener, status, prompt);
            }
            catch(NotfoundException | InteroperabilityException e) {
                log.warn("Failure {} using multipart upload. Fallback to single upload.", e.getMessage());
                status.setAppend(false);
                try {
                    return single.upload(write, file, local, throttle, progress, listener, status, prompt);
                }
                catch(BackgroundException f) {
                    log.warn("Failure {} using single upload. Throw original multipart failure {}", e, e);
                    throw e;
                }
            }
        }
        // Use single upload service
        return single.upload(write, file, local, throttle, progress, listener, status, prompt);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        if(this.threshold(status)) {
            return new S3MultipartUploadService(session, acl).checksum(file, status);
        }
        return single.checksum(file, status);
    }

    @Override
    public EnumSet<Flags> features(final Path file, final TransferStatus status) throws BackgroundException {
        if(this.threshold(status)) {
            return new S3MultipartUploadService(session, acl).features(file, status);
        }
        return single.features(file, status);
    }

    protected boolean threshold(final TransferStatus status) {
        if(!HostPreferencesFactory.get(session.getHost()).getBoolean("s3.upload.multipart")) {
            log.warn("Multipart upload is disabled");
            return false;
        }
        if(status.isAppend()) {
            return true;
        }
        if(status.getLength() >= threshold) {
            return true;
        }
        return false;
    }

}
