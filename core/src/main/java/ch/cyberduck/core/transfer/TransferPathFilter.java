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
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.TransferStatusCanceledException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;

public interface TransferPathFilter {

    /**
     * @param file   File
     * @param parent Parent transfer status
     * @return True if file should be transferred
     */
    boolean accept(Path file, Local local, TransferStatus parent) throws BackgroundException;

    /**
     * Called before the file will actually get transferred. Should prepare for the transfer such as calculating its
     * size.
     *
     * @param file     Remote file
     * @param local    File on disk
     * @param parent   Parent transfer status
     * @param listener Progress listener
     * @return Transfer status
     * @throws TransferStatusCanceledException To skip item
     */
    TransferStatus prepare(Path file, Local local, TransferStatus parent, ProgressListener listener)
            throws TransferStatusCanceledException, BackgroundException;

    /**
     * Apply filter outcome such as renaming file prior transfer
     *
     * @param file     Remote file
     * @param local    File on disk
     * @param status   Transfer status
     * @param listener Progress listener
     * @throws BackgroundException
     */
    void apply(Path file, Local local, TransferStatus status, ProgressListener listener)
        throws BackgroundException;

    /**
     * Post processing of completed transfer.
     *
     * @param file     File
     * @param status   Transfer status
     * @param listener Progress listener
     */
    void complete(Path file, Local local,
                  TransferStatus status, ProgressListener listener) throws BackgroundException;

    TransferPathFilter withFinder(Find finder);

    TransferPathFilter withAttributes(AttributesFinder attributes);

}
