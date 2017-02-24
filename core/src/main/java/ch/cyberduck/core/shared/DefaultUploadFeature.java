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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.io.ThrottledOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;

public class DefaultUploadFeature<Reply> implements Upload<Reply> {

    private Write<Reply> writer;

    public DefaultUploadFeature(final Write<Reply> writer) {
        this.writer = writer;
    }

    @Override
    public Reply upload(final Path file, final Local local, final BandwidthThrottle throttle,
                        final StreamListener listener, final TransferStatus status,
                        final ConnectionCallback callback) throws BackgroundException {
        final InputStream in = local.getInputStream();
        final StatusOutputStream<Reply> out = writer.write(file, status, callback);
        new StreamCopier(status, status)
                .withOffset(status.getOffset())
                .withLimit(status.getLength())
                .withListener(listener)
                .transfer(in, new ThrottledOutputStream(out, throttle));
        return out.getStatus();
    }

    @Override
    public Write.Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return writer.append(file, length, cache);
    }

    @Override
    public Upload<Reply> withWriter(final Write<Reply> writer) {
        this.writer = writer;
        return this;
    }
}
