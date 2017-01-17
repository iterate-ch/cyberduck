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
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Vault;

import java.util.List;

public class CryptoRedundancyFeature implements Redundancy {

    private final Session<?> session;
    private final Redundancy delegate;
    private final Vault vault;

    public CryptoRedundancyFeature(final Session<?> session, final Redundancy delegate, final Vault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public String getDefault() {
        return delegate.getDefault();
    }

    @Override
    public List<String> getClasses() {
        return delegate.getClasses();
    }

    @Override
    public void setClass(final Path file, final String redundancy) throws BackgroundException {
        delegate.setClass(vault.encrypt(session, file), redundancy);
    }

    @Override
    public String getClass(final Path file) throws BackgroundException {
        return delegate.getClass(vault.encrypt(session, file));
    }
}
