package ch.cyberduck.core.cryptomator;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

public class CryptoCopyFeature implements Copy {

    private final Session<?> session;
    private final Copy proxy;
    private final CryptoVault vault;

    public CryptoCopyFeature(final Session<?> session, final Copy delegate, final CryptoVault vault) {
        this.session = session;
        this.proxy = delegate;
        this.vault = vault;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        if(vault.contains(source) && vault.contains(target)) {
            // Copy inside vault may use server side copy
            proxy.copy(vault.encrypt(session, source), vault.encrypt(session, target), status);
        }
        else {
            if(vault.contains(target)) {
                // Write header to be reused in writer
                final Cryptor cryptor = vault.getCryptor();
                final FileHeader header = cryptor.fileHeaderCryptor().create();
                status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
                status.setNonces(new RandomNonceGenerator());
            }
            // Copy files from or into vault requires to pass through encryption features
            new DefaultCopyFeature(session.getFeature(Read.class), session.getFeature(Write.class)).copy(
                    vault.contains(source) ? vault.encrypt(session, source) : source,
                    vault.contains(target) ? vault.encrypt(session, target) : target, status);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return proxy.isRecursive(source, target);
    }
}
