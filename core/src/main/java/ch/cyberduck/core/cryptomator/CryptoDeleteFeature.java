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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Vault;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class CryptoDeleteFeature implements Delete {
    private final Delete delegate;
    private final Vault vault;

    public CryptoDeleteFeature(final Delete delegate, final Vault vault) {
        this.delegate = delegate;
        this.vault = vault;
    }

    @Override
    public void delete(final List<Path> files, final LoginCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> encrypted = new ArrayList<>();
        for(Path f : files) {
            if(f.isDirectory()) {
                final Path directoryMetafile = vault.encrypt(f, true);
                final Path directoryPath = vault.encrypt(f, false);
                encrypted.add(directoryMetafile);
                encrypted.add(directoryPath);
                //TODO muss silently failen für directoryPath.getParent(), wenn es noch andere Ordner unter diesem Firstlevel hat
                encrypted.add(directoryPath.getParent());
            }
            else {
                encrypted.add(vault.encrypt(f));
            }
        }
        delegate.delete(encrypted, prompt, callback);
    }
}
