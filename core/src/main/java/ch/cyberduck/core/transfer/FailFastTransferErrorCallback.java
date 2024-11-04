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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FailFastTransferErrorCallback implements TransferErrorCallback {
    private static final Logger log = LogManager.getLogger(FailFastTransferErrorCallback.class);

    private final TransferErrorCallback proxy;

    public FailFastTransferErrorCallback(final TransferErrorCallback proxy) {
        this.proxy = proxy;
    }

    @Override
    public boolean prompt(final TransferItem item, final TransferStatus status, final BackgroundException failure, final int pending) throws BackgroundException {
        if(pending == 0) {
            // Fail fast when first item in queue fails preparing
            log.warn("Cancel {} with no pending file after failure {}", item, failure);
            throw failure;
        }
        if(pending == 1) {
            // Fail fast when transferring single file
            log.warn("Cancel {} with no pending file after failure {}", item, failure);
            throw failure;
        }
        return proxy.prompt(item, status, failure, pending);
    }
}
