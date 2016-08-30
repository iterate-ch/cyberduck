package ch.cyberduck.core.udt;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

public interface UDTTransferOption {

    /**
     * @param bookmark Connection
     * @param status   File transfer status
     * @param prompt   Prompt
     * @return True if the connection should be proxied
     */
    boolean prompt(Host bookmark, final TransferStatus status, ConnectionCallback prompt)
            throws BackgroundException;

    UDTProxyProvider provider();
}
