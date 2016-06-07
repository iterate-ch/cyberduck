package ch.cyberduck.core.transfer;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;

public interface TransferPathFilter {

    TransferPathFilter withCache(final PathCache cache);


    /**
     * @param file   File
     * @param parent Parent transfer status
     * @return True if file should be transferred
     */
    boolean accept(Path file, Local local, TransferStatus parent) throws BackgroundException;

    /**
     * Called before the file will actually get transferred. Should prepare for the transfer such as calculating its size.
     *
     * @param file   File
     * @param parent Parent transfer status
     * @return Transfer status
     */
    TransferStatus prepare(Path file, Local local, TransferStatus parent)
            throws BackgroundException;

    void apply(Path file, Local local, TransferStatus status, final ProgressListener listener)
            throws BackgroundException;

    /**
     * Post processing of completed transfer.
     *
     * @param file     File
     * @param options  Options
     * @param status   Transfer status
     * @param listener Progress callback
     */
    void complete(Path file, Local local, TransferOptions options,
                  TransferStatus status, ProgressListener listener) throws BackgroundException;
}