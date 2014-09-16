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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.model.StorageObject;

/**
 * @version $Id$
 */
public class S3ThresholdUploadService implements Upload<StorageObject> {
    private static final Logger log = Logger.getLogger(S3ThresholdUploadService.class);

    private PathContainerService containerService
            = new S3PathContainerService();

    private Preferences preferences
            = Preferences.instance();

    private S3Session session;

    private Long multipartThreshold;

    public S3ThresholdUploadService(final S3Session session) {
        this.session = session;
        this.multipartThreshold = preferences.getLong("s3.upload.multipart.threshold");
    }

    public S3ThresholdUploadService(final S3Session session, final Long multipartThreshold) {
        this.session = session;
        this.multipartThreshold = multipartThreshold;
    }

    @Override
    public StorageObject upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                final TransferStatus status, final LoginCallback prompt) throws BackgroundException {
        if(status.getLength() > multipartThreshold) {
            if(!preferences.getBoolean("s3.upload.multipart")) {
                // Disabled by user
                if(status.getLength() < preferences.getLong("s3.upload.multipart.required.threshold")) {
                    log.warn("Multipart upload is disabled with property s3.upload.multipart");
                    final S3SingleUploadService single = new S3SingleUploadService(session);
                    return single.upload(file, local, throttle, listener, status, prompt);
                }
            }
            final S3MultipartUploadService service = new S3MultipartUploadService(session);
            try {
                return service.upload(file, local, throttle, listener, status, prompt);
            }
            catch(NotfoundException e) {
                log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
                final S3SingleUploadService single = new S3SingleUploadService(session);
                return single.upload(file, local, throttle, listener, status, prompt);
            }
            catch(InteroperabilityException e) {
                log.warn(String.format("Failure using multipart upload %s. Fallback to single upload.", e.getMessage()));
                final S3SingleUploadService single = new S3SingleUploadService(session);
                return single.upload(file, local, throttle, listener, status, prompt);
            }
        }
        else {
            final S3SingleUploadService single = new S3SingleUploadService(session);
            return single.upload(file, local, throttle, listener, status, prompt);
        }
    }
}