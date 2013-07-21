package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.transfer.TransferStatus;

/**
 * @version $Id$
 */
public class DefaultTouchFeature implements Touch {

    private Session session;

    public DefaultTouchFeature(final Session session) {
        this.session = session;
    }

    @Override
    public void touch(final Path file) throws BackgroundException {
        final Local temp = TemporaryFileServiceFactory.get().create(file);
        temp.touch();
        file.setLocal(temp);
        final TransferStatus status = new TransferStatus();
        try {
            session.upload(file, new BandwidthThrottle(BandwidthThrottle.UNLIMITED),
                    new AbstractStreamListener(), status);
        }
        finally {
            temp.delete();
        }
    }
}
