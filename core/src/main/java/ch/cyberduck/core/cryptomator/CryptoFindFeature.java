package ch.cyberduck.core.cryptomator;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;

public class CryptoFindFeature implements Find {

    private final Find delegate;
    private final Vault vault;

    public CryptoFindFeature(final Find delegate, final Vault vault) {
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        try {
            return delegate.find(vault.encrypt(file));
        }
        catch(NotfoundException e) {
            return false;
        }
    }

    @Override
    public Find withCache(final PathCache cache) {
        delegate.withCache(cache);
        return this;
    }
}
