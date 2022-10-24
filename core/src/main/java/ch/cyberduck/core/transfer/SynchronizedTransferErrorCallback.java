package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.exception.BackgroundException;

public class SynchronizedTransferErrorCallback implements TransferErrorCallback {

    private final TransferErrorCallback proxy;

    public SynchronizedTransferErrorCallback(final TransferErrorCallback proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) throws BackgroundException {
        synchronized(proxy) {
            return proxy.prompt(item, status, failure, pending);
        }
    }
}
