package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.io.StreamCancelation;
import ch.cyberduck.core.io.StreamProgress;
import ch.cyberduck.core.transfer.ProxyTransferStatus;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CryptoTransferStatus extends ProxyTransferStatus implements StreamCancelation, StreamProgress {
    private static final Logger log = LogManager.getLogger(CryptoTransferStatus.class);

    private final CryptoVault vault;

    public CryptoTransferStatus(final CryptoVault vault, final TransferStatus proxy) {
        super(proxy);
        this.vault = vault;
        this.withLength(vault.toCiphertextSize(proxy.getOffset(), proxy.getLength()))
                // Assume single chunk upload
                .withOffset(0L == proxy.getOffset() ? 0L : vault.toCiphertextSize(0L, proxy.getOffset()))
                .withMime(null);
    }

    @Override
    public void setResponse(final PathAttributes attributes) {
        try {
            super.setResponse(attributes.withSize(vault.toCleartextSize(0L, attributes.getSize())));
        }
        catch(CryptoInvalidFilesizeException e) {
            log.warn(String.format("Failure %s translating file size from response %s", e, attributes));
        }
    }
}
