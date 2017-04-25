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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

public class CryptoCopyFeature implements Copy {

    private final Session<?> session;
    private final Copy proxy;
    private final CryptoVault cryptomator;

    public CryptoCopyFeature(final Session<?> session, final Copy delegate, final CryptoVault cryptomator) {
        this.session = session;
        this.proxy = delegate;
        this.cryptomator = cryptomator;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status) throws BackgroundException {
        proxy.copy(cryptomator.encrypt(session, source), cryptomator.encrypt(session, target), status);
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return proxy.isRecursive(source, target);
    }
}
