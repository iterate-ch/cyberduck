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
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;

public class CryptoIdProvider implements IdProvider {
    private final IdProvider delegate;
    private final CryptoVault vault;

    public CryptoIdProvider(final IdProvider delegate, final CryptoVault vault) {
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public String getFileid(final Path file) throws BackgroundException {
        return delegate.getFileid(vault.encrypt(file));
    }
}
