package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.transfer.upload.UploadFilterOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;

public class DefaultUploadFeature<Reply> implements Upload<Reply> {
    private static final Logger log = LogManager.getLogger(DefaultUploadFeature.class);

    @Override
    public Reply upload(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle,
                        final ProgressListener progress, final StreamListener streamListener, final TransferStatus status,
                        final ConnectionCallback callback, final UploadFilterOptions options) throws BackgroundException {
        final Reply response = this.transfer(write, file, local, throttle, streamListener, status, status, status, callback, options);
        log.debug("Received response {}", response);
        return response;
    }

    protected Reply transfer(final Write<Reply> write, final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                             final TransferStatus status, final StreamCancelation cancel, final StreamProgress progress,
                             final ConnectionCallback callback, final UploadFilterOptions options) throws BackgroundException {
        final InputStream in = this.decorate(local.getInputStream(), status, options);
        final StatusOutputStream<Reply> out = write.write(file, status, callback);
        new StreamCopier(cancel, progress)
                .withOffset(status.getOffset())
                .withLimit(status.getLength())
                .withListener(listener)
                .transfer(in, new ThrottledOutputStream(out, throttle));
        return out.getStatus();
    }

    /**
     * Wrap input stream if checksum calculation is enabled.
     *
     * @param in      File input stream
     * @param status
     * @param options
     * @return Wrapped or same stream
     */
    protected InputStream decorate(final InputStream in, final TransferStatus status, final UploadFilterOptions options) throws BackgroundException {
        return in;
    }
}
