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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ListCanceledException;
import ch.cyberduck.core.features.Vault;

import org.apache.log4j.Logger;

import java.util.EnumSet;

public class VaultFinderListProgressListener extends IndexedListProgressListener {
    private static final Logger log = Logger.getLogger(VaultFinderListProgressListener.class);

    private static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";

    private final PasswordStore keychain;
    private final PasswordCallback prompt;
    private final VaultLookupListener listener;
    private final Session<?> session;

    public VaultFinderListProgressListener(final Session<?> session, final PasswordStore keychain, final PasswordCallback prompt, final VaultLookupListener listener) {
        this.session = session;
        this.keychain = keychain;
        this.prompt = prompt;
        this.listener = listener;
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
            if(f.equals(new Path(directory, MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file, Path.Type.vault)))) {
                final Vault vault = VaultFactory.get(directory, keychain, prompt, listener);
                if(session.getFeature(Vault.class).equals(vault)) {
                    log.warn(String.format("Ignore vault %sfound already loaded", vault));
                    return;
                }
                try {
                    session.withVault(vault.load(session));
                    listener.found(vault);
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Failure loading vault in %s. %s", directory, e.getDetail()));
                    return;
                }
                throw new VaultFinderListCanceledException(vault, list);
            }
        }
    }
}
