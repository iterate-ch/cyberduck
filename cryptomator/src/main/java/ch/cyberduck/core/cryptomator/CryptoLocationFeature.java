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
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.features.Vault;

import java.util.Set;

public class CryptoLocationFeature implements Location {

    private final Session<?> session;
    private final Location delegate;
    private final Vault vault;

    public CryptoLocationFeature(final Session<?> session, final Location delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public Set<Name> getLocations() {
        return delegate.getLocations();
    }

    @Override
    public Name getLocation(final Path file) throws BackgroundException {
        return delegate.getLocation(vault.encrypt(session, file));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoLocationFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
