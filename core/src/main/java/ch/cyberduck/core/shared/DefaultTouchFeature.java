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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DefaultTouchFeature<T> implements Touch<T> {
    private static final Logger log = LogManager.getLogger(DefaultTouchFeature.class);

    protected Write<T> write;

    public DefaultTouchFeature(final Write<T> writer) {
        this.write = writer;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final StatusOutputStream<T> writer = write.write(file, status.withLength(0L), new DisabledConnectionCallback());
            writer.close();
            if(!PathAttributes.EMPTY.equals(status.getResponse())) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Received reply %s for creating file %s", status.getResponse(), file));
                }
                return new Path(file).withAttributes(status.getResponse());
            }
            log.warn(String.format("Missing status from writer %s", writer));
            return file;
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }

    @Override
    public DefaultTouchFeature<T> withWriter(final Write<T> write) {
        this.write = write;
        return this;
    }
}
