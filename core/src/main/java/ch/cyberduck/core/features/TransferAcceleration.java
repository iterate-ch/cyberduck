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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

public interface TransferAcceleration {
    /**
     * @param file File
     * @return True if enabled
     */
    boolean getStatus(Path file) throws BackgroundException;

    /**
     * @param file    File
     * @param enabled True to enable
     */
    void setStatus(Path file, boolean enabled) throws BackgroundException;

    /**
     * @param bookmark Connection
     * @param file     File to transfer
     * @param status   File transfer status
     * @param prompt   Prompt
     * @return True if the connection should be proxied
     */
    boolean prompt(Host bookmark, Path file, TransferStatus status, ConnectionCallback prompt)
            throws BackgroundException;

    /**
     * Configure connectino to use transfer accelerated endpoint
     */
    void configure(boolean enable, Path file) throws BackgroundException;
}
