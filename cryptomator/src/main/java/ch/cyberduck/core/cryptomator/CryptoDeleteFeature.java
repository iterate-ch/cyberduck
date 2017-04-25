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

import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.impl.CryptoFilenameProvider;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.util.ArrayList;
import java.util.List;

public class CryptoDeleteFeature implements Delete {
    private final Session<?> session;
    private final Delete delegate;
    private final CryptoVault vault;
    private final CryptoFilenameProvider filenameProvider;

    public CryptoDeleteFeature(final Session<?> session, final Delete delegate, final CryptoVault vault) {
        this.session = session;
        this.delegate = delegate;
        this.vault = vault;
        this.filenameProvider = vault.getFilenameProvider();
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> encrypted = new ArrayList<>();
        for(Path f : files) {
            if(vault.contains(f)) {
                final Path encrypt = vault.encrypt(session, f);
                encrypted.add(encrypt);
                if(f.isDirectory()) {
                    final Path metadataFile = vault.encrypt(session, f, true);
                    if(metadataFile.getType().contains(Path.Type.encrypted)) {
                        encrypted.add(metadataFile);
                    }
                    if(encrypt.getType().contains(Path.Type.encrypted)) {
                        encrypted.add(encrypt.getParent());
                    }
                }
                if(filenameProvider.isDeflated(encrypt.getName())) {
                    final Path metadataFile = filenameProvider.resolve(encrypt.getName());
                    encrypted.add(metadataFile);
                }
            }
            else {
                encrypted.add(f);
            }
        }
        delegate.delete(encrypted, prompt, callback);
    }

    @Override
    public boolean isSupported(final Path file) {
        return delegate.isSupported(file);
    }

    @Override
    public boolean isRecursive() {
        return delegate.isRecursive();
    }
}
