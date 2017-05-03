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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.logging.LoggingConfiguration;

public class CryptoLoggingFeature implements Logging {
    private final Session<?> session;
    private final Logging delegate;
    private final Vault vault;

    public CryptoLoggingFeature(final Session<?> session, final Logging delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public LoggingConfiguration getConfiguration(final Path container) throws BackgroundException {
        return delegate.getConfiguration(vault.encrypt(session, container));
    }

    @Override
    public void setConfiguration(final Path container, final LoggingConfiguration configuration) throws BackgroundException {
        delegate.setConfiguration(vault.encrypt(session, container), configuration);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoLoggingFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
