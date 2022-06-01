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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.vault.DecryptingListProgressListener;

public class CryptoVersioningFeature implements Versioning {

    private final Session<?> session;
    private final Versioning delegate;
    private final Vault vault;

    public CryptoVersioningFeature(final Session<?> session, final Versioning delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) throws BackgroundException {
        return delegate.getConfiguration(vault.encrypt(session, container));
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        delegate.setConfiguration(vault.encrypt(session, container), prompt, configuration);
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        delegate.revert(vault.encrypt(session, file));
    }

    @Override
    public boolean isRevertable(final Path file) {
        return delegate.isRevertable(file);
    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        return delegate.list(vault.encrypt(session, file), new DecryptingListProgressListener(session, vault, listener));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoVersioningFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
