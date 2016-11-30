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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.IndexedListProgressListener;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.cryptomator.impl.CryptoVault;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.pool.SessionPool;

import org.apache.log4j.Logger;

import java.util.EnumSet;

public class VaultFinderListProgressListener extends IndexedListProgressListener {
    private static final Logger log = Logger.getLogger(VaultFinderListProgressListener.class);

    private final SessionPool pool;
    private final PasswordStore keychain;
    private final LoginCallback prompt;

    public VaultFinderListProgressListener(final SessionPool pool,
                                           final PasswordStore keychain,
                                           final LoginCallback prompt) {
        this.pool = pool;
        this.keychain = keychain;
        this.prompt = prompt;
    }

    @Override
    public void message(final String message) {
        //
    }

    @Override
    public void visit(final AttributedList<Path> list, final int index, final Path file) throws ListCanceledException {
        for(int i = index; i < list.size(); i++) {
            final Path f = list.get(i);
            final Path directory = f.getParent();
            if(f.equals(new Path(directory, CryptoVault.MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file)))) {
                try {
                    final CryptoVault vault = new CryptoVault(pool, directory, keychain, prompt).load();
                    throw new VaultFinderListCanceledException(vault, list);
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Failure loading vault in %s. %s", directory, e.getMessage()));
                    return;
                }
            }
        }
    }
}
