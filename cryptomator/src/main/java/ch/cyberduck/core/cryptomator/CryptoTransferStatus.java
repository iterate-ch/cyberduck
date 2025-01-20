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

    private final CryptoVaultInterface vault;

    public CryptoTransferStatus(final CryptoVaultInterface vault, final TransferStatus proxy) {
        super(proxy);
        this.vault = vault;
        this.setLength(vault.toCiphertextSize(proxy.getOffset(), proxy.getLength()))
                // Assume single chunk upload
                .setOffset(0L == proxy.getOffset() ? 0L : vault.toCiphertextSize(0L, proxy.getOffset()))
                .setMime(null);
    }

    @Override
    public TransferStatus setResponse(final PathAttributes attributes) {
        try {
            attributes.setSize(vault.toCleartextSize(0L, attributes.getSize()));
            attributes.setVault(vault.getHome());
            super.setResponse(attributes);
        }
        catch(CryptoInvalidFilesizeException e) {
            log.warn("Failure {} translating file size from response {}", e, attributes);
        }
        return this;
    }
}
