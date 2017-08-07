package ch.cyberduck.core.cryptomator.features;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.random.RandomNonceGenerator;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.shared.DefaultCopyFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.cryptomator.cryptolib.api.Cryptor;
import org.cryptomator.cryptolib.api.FileHeader;

public class CryptoCopyFeature implements Copy {

    private final Session<?> session;
    private final Copy proxy;
    private final CryptoVault vault;

    private Session<?> target;

    public CryptoCopyFeature(final Session<?> session, final Copy proxy, final CryptoVault vault) {
        this.session = session;
        this.target = session;
        this.proxy = proxy;
        this.vault = vault;
    }

    @Override
    public void copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        if(vault.contains(copy)) {
            // Write header to be reused in writer
            final Cryptor cryptor = vault.getCryptor();
            final FileHeader header = cryptor.fileHeaderCryptor().create();
            status.setHeader(cryptor.fileHeaderCryptor().encryptHeader(header));
            status.setNonces(new RandomNonceGenerator());
        }
        if(vault.contains(source) && vault.contains(copy)) {
            proxy.withTarget(target).copy(
                    vault.contains(source) ? vault.encrypt(session, source) : source,
                    vault.contains(copy) ? vault.encrypt(session, copy) : copy, status, callback);
        }
        else {
            // Copy files from or into vault requires to pass through encryption features
            new DefaultCopyFeature(session).withTarget(target).copy(
                    vault.contains(source) ? vault.encrypt(session, source) : source,
                    vault.contains(copy) ? vault.encrypt(session, copy) : copy,
                    status,
                    new DisabledConnectionCallback());
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path copy) {
        // Due to the encrypted folder layout copying is never recursive even when supported by the native implementation
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path copy) {
        if(vault.contains(source) && vault.contains(copy)) {
            return proxy.withTarget(target).isSupported(source, copy);
        }
        return new DefaultCopyFeature(session).withTarget(target).isSupported(source, copy);
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        this.target = session;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoCopyFeature{");
        sb.append("delegate=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
