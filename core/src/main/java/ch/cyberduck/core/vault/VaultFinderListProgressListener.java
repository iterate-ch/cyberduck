package ch.cyberduck.core.vault;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class VaultFinderListProgressListener extends IndexedListProgressListener {
    private static final Logger log = Logger.getLogger(VaultFinderListProgressListener.class);

    private static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";

    private final PasswordStore keychain;
    private final VaultLookupListener listener;
    private final AtomicBoolean found = new AtomicBoolean();

    public VaultFinderListProgressListener(final PasswordStore keychain, final VaultLookupListener listener) {
        this.keychain = keychain;
        this.listener = listener;
    }

    @Override
    public void message(final String message) {
        //
    }

    @Override
    public IndexedListProgressListener reset() {
        found.set(false);
        return super.reset();
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) throws ListCanceledException {
        if(!found.get()) {
            for(int i = index; i < list.size(); i++) {
                final Path f = list.get(i);
                final Path directory = f.getParent();
                if(f.equals(new Path(directory, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file, Path.Type.vault)))) {
                    final Vault vault = VaultFactory.get(directory, keychain);
                    if(vault.equals(Vault.DISABLED)) {
                        return;
                    }
                    try {
                        listener.found(vault);
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure loading vault in %s. %s", directory, e.getDetail()));
                        return;
                    }
                    finally {
                        found.set(true);
                    }
                    throw new VaultFinderListCanceledException(vault, list);
                }
            }
        }
    }
}
