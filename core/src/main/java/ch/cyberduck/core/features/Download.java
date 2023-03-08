package ch.cyberduck.core.features;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

@Optional
public interface Download {

    /**
     * Copy file from server to disk
     *
     * @param file     File on server
     * @param local    File on local disk
     * @param throttle Bandwidth management
     * @param listener Progress callback
     * @param status   Transfer status holder
     * @param callback Prompt
     * @throws BackgroundException
     */
    void download(Path file, Local local, BandwidthThrottle throttle, StreamListener listener,
                  TransferStatus status, ConnectionCallback callback) throws BackgroundException;

    /**
     * Determine support for reading file with offset
     *
     * @param file File
     * @return True if reading with offset is supported
     */
    boolean offset(Path file) throws BackgroundException;

    Download withReader(Read reader);
}
