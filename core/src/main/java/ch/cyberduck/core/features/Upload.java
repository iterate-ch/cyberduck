package ch.cyberduck.core.features;

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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

/**
 * @version $Id$
 */
public interface Upload<Output> {

    Output upload(final Path file, Local local, final BandwidthThrottle throttle, final StreamListener listener,
                  final TransferStatus status, final ConnectionCallback callback) throws BackgroundException;

    /**
     * @return True if the write has a pool with concurrent connections implemented. Applies to multipart uploads.
     */
    boolean pooled();

    Write.Append append(Path file, Long length, PathCache cache) throws BackgroundException;
}
