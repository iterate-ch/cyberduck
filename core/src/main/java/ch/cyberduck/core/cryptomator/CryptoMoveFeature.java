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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

public class CryptoMoveFeature implements Move {

    private final Move delegate;
    private final CryptoVault cryptomator;

    public CryptoMoveFeature(final Move delegate, final CryptoVault cryptomator) {
        this.delegate = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        delegate.move(cryptomator.encrypt(file), cryptomator.encrypt(renamed), exists, callback);
    }

    @Override
    public boolean isSupported(final Path file) {
        return delegate.isSupported(file);
    }
}
