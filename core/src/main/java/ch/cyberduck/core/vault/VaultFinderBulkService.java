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

import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.util.Map;

public class VaultFinderBulkService<R> implements Bulk<R> {
    private static final Logger log = Logger.getLogger(VaultFinderBulkService.class);

    private final PasswordStore keychain;
    /**
     * Current open vault
     */
    private final Vault vault;
    private final Bulk<R> delegate;
    private final VaultLookupListener listener;

    public VaultFinderBulkService(final PasswordStore keychain, final Vault vault, final Bulk<R> delegate, final VaultLookupListener listener) {
        this.keychain = keychain;
        this.vault = vault;
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public R pre(final Transfer.Type type, final Map<Path, TransferStatus> files) throws BackgroundException {
        for(Path f : files.keySet()) {
            if(f.getType().contains(Path.Type.decrypted)) {
                if(vault.contains(f)) {
                    break;
                }
                Path directory = f.getParent();
                do {
                    final Vault vault = VaultFactory.get(directory, keychain);
                    if(vault.equals(Vault.DISABLED)) {
                        break;
                    }
                    try {
                        listener.found(vault);
                        break;
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure loading vault in %s. %s", directory, e.getDetail()));
                    }
                }
                while(!directory.isRoot());
            }
        }
        return delegate.pre(type, files);
    }
}
