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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoadingVaultLookupListener implements VaultLookupListener {
    private static final Logger log = LogManager.getLogger(LoadingVaultLookupListener.class);

    private final VaultRegistry registry;
    private final PasswordStore keychain;
    private final PasswordCallback prompt;

    public LoadingVaultLookupListener(final VaultRegistry registry, final PasswordStore keychain, final PasswordCallback prompt) {
        this.registry = registry;
        this.keychain = keychain;
        this.prompt = prompt;
    }

    @Override
    public Vault load(final Session session, final Path directory, final String masterkey, final String config, final byte[] pepper) throws VaultUnlockCancelException {
        synchronized(registry) {
            if(registry.contains(directory)) {
                return registry.find(session, directory);
            }
            final Vault vault = VaultFactory.get(directory, masterkey, config, pepper);
            if(log.isInfoEnabled()) {
                log.info(String.format("Loading vault %s for session %s", vault, session));
            }
            try {
                registry.add(vault.load(session, prompt, keychain));
            }
            catch(BackgroundException e) {
                log.warn(String.format("Failure loading vault %s. %s", vault, e));
                throw new VaultUnlockCancelException(vault, e);
            }
            return vault;
        }
    }
}
