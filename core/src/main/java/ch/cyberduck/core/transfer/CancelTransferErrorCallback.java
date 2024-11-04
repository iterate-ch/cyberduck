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
import ch.cyberduck.core.threading.DefaultFailureDiagnostics;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CancelTransferErrorCallback implements TransferErrorCallback {
    private static final Logger log = LogManager.getLogger(CancelTransferErrorCallback.class);

    private final TransferErrorCallback proxy;
    private final DefaultFailureDiagnostics diagnostics = new DefaultFailureDiagnostics();

    public CancelTransferErrorCallback(final TransferErrorCallback proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) throws BackgroundException {
        switch(diagnostics.determine(failure)) {
            case cancel:
            case skip:
                // Interrupt transfer
                log.warn("Cancel {} with after failure {}", item, failure);
                return false;
        }
        return proxy.prompt(item, status, failure, pending);
    }
}
