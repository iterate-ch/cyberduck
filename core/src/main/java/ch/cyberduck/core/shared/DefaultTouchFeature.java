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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

public class DefaultTouchFeature<T> implements Touch<T> {
    private static final Logger log = Logger.getLogger(DefaultTouchFeature.class);

    private final Upload<T> feature;

    public DefaultTouchFeature(final Upload<T> upload) {
        this.feature = upload;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        final Local temp = TemporaryFileServiceFactory.get().create(file);
        LocalTouchFactory.get().touch(temp);
        try {
            final T reply = feature.upload(file, temp,
                    new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                    new DisabledStreamListener(), status, new DisabledConnectionCallback());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Received reply %s for creating file %s", reply, file));
            }
        }
        finally {
            temp.delete();
        }
        return file;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public DefaultTouchFeature<T> withWriter(final Write<T> writer) {
        feature.withWriter(writer);
        return this;
    }
}
