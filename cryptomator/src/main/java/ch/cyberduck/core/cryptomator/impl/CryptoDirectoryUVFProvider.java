package ch.cyberduck.core.cryptomator.impl;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.cryptomator.AbstractVault;
import ch.cyberduck.core.cryptomator.CryptoFilename;
import ch.cyberduck.core.cryptomator.CryptorCache;
import ch.cyberduck.core.exception.BackgroundException;

public class CryptoDirectoryUVFProvider extends CryptoDirectoryV7Provider {

    private final Path home;

    public CryptoDirectoryUVFProvider(final AbstractVault vault, final CryptoFilename filenameProvider, final CryptorCache filenameCryptor) {
        super(vault, filenameProvider, filenameCryptor);
        this.home = vault.getHome();
    }

    @Override
    protected byte[] toDirectoryId(final Session<?> session, final Path directory, final byte[] directoryId) throws BackgroundException {
        if(new SimplePathPredicate(home).test(directory)) {
            return directoryId;
        }
        return super.toDirectoryId(session, directory, directoryId);
    }
}
