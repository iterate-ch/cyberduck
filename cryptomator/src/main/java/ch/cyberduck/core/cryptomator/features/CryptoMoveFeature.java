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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.TransferStatus;

public class CryptoMoveFeature implements Move {

    private final Session<?> session;
    private final Move proxy;
    private final Vault vault;

    public CryptoMoveFeature(final Session<?> session, final Move delegate, final Delete delete, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = delegate.withDelete(new CryptoDeleteFeature(session, delete, cryptomator));
        this.vault = cryptomator;
    }

    @Override
    public void move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        // Move inside vault moves actual files and only metadata files for directories but not the actual directories
        proxy.move(
                vault.contains(file) ? vault.encrypt(session, file, file.isDirectory()) : file,
                vault.contains(renamed) ? vault.encrypt(session, renamed, file.isDirectory()) : renamed,
                status, callback, connectionCallback);
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        // No need to handle recursion with encrypted filenames
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return proxy.isSupported(source, target);
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoMoveFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
