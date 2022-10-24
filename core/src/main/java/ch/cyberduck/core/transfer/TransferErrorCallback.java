package ch.cyberduck.core.transfer;

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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.BackgroundException;

public interface TransferErrorCallback {

    /**
     * @param item    Transfer
     * @param status  Transfer Status
     * @param failure Failure transferring file
     * @param pending Number of pending transfers
     * @return True to ignore failure continue regardless. False to abort file transfer silently
     * @throws BackgroundException Abort file transfer with exception
     */
    boolean prompt(TransferItem item, TransferStatus status, BackgroundException failure, int pending) throws BackgroundException;

    TransferErrorCallback ignore = new TransferErrorCallback() {
        @Override
        public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) {
            return true;
        }
    };
}
