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
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.model.StorageObject;

public class S3SingleUploadService extends HttpUploadFeature<StorageObject> {
    private static final Logger log = LogManager.getLogger(S3SingleUploadService.class);

    private final S3Session session;

    public S3SingleUploadService(final S3Session session) {
        this.session = session;
    }

    @Override
    public StorageObject upload(final Write<StorageObject> write, final Path file, final Local local, final BandwidthThrottle throttle,
                                final ProgressListener progress, final StreamListener streamListener, final TransferStatus status, final ConnectionCallback callback, final UploadFilterOptions options) throws BackgroundException {
        final S3Protocol.AuthenticationHeaderSignatureVersion signatureVersion = session.getSignatureVersion();
        switch(signatureVersion) {
            case AWS4HMACSHA256:
                if(!HashAlgorithm.sha256.equals(status.getChecksum().algorithm)) {
                    // Checksum not set in upload filter
                    status.setChecksum(write.checksum(file, status).compute(local.getInputStream(), status));
                }
                break;
        }
        try {
            final StorageObject response = super.upload(write, file, local, throttle, progress, streamListener, status, callback, options);
            if(null != response.getServerSideEncryptionAlgorithm()) {
                log.warn("Skip checksum verification for {} with server side encryption enabled", file);
                status.setChecksum(Checksum.NONE);
            }
            return response;
        }
        catch(InteroperabilityException e) {
            if(!session.getSignatureVersion().equals(signatureVersion)) {
                // Retry if upload fails with Header "x-amz-content-sha256" set to the hex-encoded SHA256 hash of the
                // request payload is required for AWS Version 4 request signing
                return this.upload(write, file, local, throttle, progress, streamListener, status, callback, options);
            }
            throw e;
        }
    }

}
