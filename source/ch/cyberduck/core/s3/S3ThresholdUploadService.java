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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

/**
 * @version $Id:$
 */
public class S3ThresholdUploadService implements Upload {

    private S3Session session;

    /**
     * Default size threshold for when to use multipart uploads.
     */
    private static final long DEFAULT_MULTIPART_UPLOAD_THRESHOLD =
            Preferences.instance().getLong("s3.upload.multipart.threshold");

    public S3ThresholdUploadService(final S3Session session) {
        this.session = session;
    }

    @Override
    public void upload(final Path file, final BandwidthThrottle throttle, final StreamListener listener,
                       final TransferStatus status) throws BackgroundException {
        if(Preferences.instance().getBoolean("s3.upload.multipart")
                && status.getLength() > DEFAULT_MULTIPART_UPLOAD_THRESHOLD) {
            new S3MultipartUploadService(session).upload(file, throttle, listener, status);
        }
        else {
            new S3SingleUploadService(session).upload(file, throttle, listener, status);
        }
    }
}
