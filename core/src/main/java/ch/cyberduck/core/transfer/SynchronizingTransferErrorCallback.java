package ch.cyberduck.core.transfer;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.exception.BackgroundException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizingTransferErrorCallback implements TransferErrorCallback {
    private static final Logger log = LogManager.getLogger(SynchronizingTransferErrorCallback.class);

    private final TransferErrorCallback proxy;
    private final Lock lock = new ReentrantLock();

    public SynchronizingTransferErrorCallback(final TransferErrorCallback proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug("Await lock {}", lock);
        }
        lock.lock();
        try {
            return proxy.prompt(item, status, failure, pending);
        }
        finally {
            lock.unlock();
        }
    }
}
