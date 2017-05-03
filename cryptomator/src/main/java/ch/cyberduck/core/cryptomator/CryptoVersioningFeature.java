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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.features.Versioning;

import java.util.Map;

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
    public Versioning withCache(final Map<Path, VersioningConfiguration> cache) {
        delegate.withCache(cache);
        return this;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) throws BackgroundException {
        return delegate.getConfiguration(vault.encrypt(session, container));
    }

    @Override
    public void setConfiguration(final Path container, final LoginCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        delegate.setConfiguration(vault.encrypt(session, container), prompt, configuration);
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        delegate.revert(vault.encrypt(session, file));
    }

    @Override
    public Credentials getToken(final LoginCallback controller) throws ConnectionCanceledException {
        return delegate.getToken(controller);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoVersioningFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
