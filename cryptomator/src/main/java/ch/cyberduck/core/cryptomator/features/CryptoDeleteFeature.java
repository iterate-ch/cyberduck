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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoVault;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.util.ArrayList;
import java.util.List;

public class CryptoDeleteFeature implements Delete {

    private final Session<?> session;
    private final Delete proxy;
    private final CryptoVault vault;
    private final CryptoFilenameProvider filenameProvider;

    public CryptoDeleteFeature(final Session<?> session, final Delete proxy, final CryptoVault vault) {
        this.session = session;
        this.proxy = proxy;
        this.vault = vault;
        this.filenameProvider = vault.getFilenameProvider();
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> encrypted = new ArrayList<>();
        for(Path f : files) {
            final Path encrypt = vault.encrypt(session, f);
            encrypted.add(encrypt);
            if(f.isDirectory()) {
                // Delete metadata file for directory
                encrypted.add(vault.encrypt(session, f, true));
            }
            if(filenameProvider.isDeflated(encrypt.getName())) {
                final Path metadataFile = filenameProvider.resolve(encrypt.getName());
                encrypted.add(metadataFile);
            }
        }
        proxy.delete(encrypted, prompt, callback);
    }

    @Override
    public boolean isSupported(final Path file) {
        return proxy.isSupported(file);
    }

    @Override
    public boolean isRecursive() {
        return proxy.isRecursive();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoDeleteFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
