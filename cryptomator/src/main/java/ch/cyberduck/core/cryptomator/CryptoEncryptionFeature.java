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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Vault;

import java.util.Set;

public class CryptoEncryptionFeature implements Encryption {

    private final Session<?> session;
    private final Encryption delegate;
    private final Vault vault;

    public CryptoEncryptionFeature(final Session<?> session, final Encryption delegate, final CryptoVault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public Algorithm getEncryption(final Path file) throws BackgroundException {
        return delegate.getEncryption(vault.encrypt(session, file));
    }

    @Override
    public void setEncryption(final Path file, final Algorithm algorithm) throws BackgroundException {
        delegate.setEncryption(vault.encrypt(session, file), algorithm);
    }

    @Override
    public Algorithm getDefault(final Path file) throws BackgroundException {
        return delegate.getDefault(vault.encrypt(session, file));
    }

    @Override
    public Set<Algorithm> getKeys(final Path file, final LoginCallback prompt) throws BackgroundException {
        return delegate.getKeys(vault.encrypt(session, file), prompt);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoEncryptionFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
