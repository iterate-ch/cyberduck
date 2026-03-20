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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class LoadingVaultLookupListener implements VaultLookupListener {
    private static final Logger log = LogManager.getLogger(LoadingVaultLookupListener.class);

    private final VaultRegistry registry;
    private final PasswordCallback prompt;

    public LoadingVaultLookupListener(final VaultRegistry registry, final PasswordCallback prompt) {
        this.registry = registry;
        this.prompt = prompt;
    }

    @Override
    public Vault load(final Session<?> session, final Path directory, final VaultMetadata metadata) throws VaultUnlockCancelException {
        synchronized(registry) {
            if(registry.contains(directory)) {
                return registry.find(session, directory);
            }
            log.info("Loading vault for session {}", session);
            final Vault vault = VaultProviderFactory.get(session).provide(session, directory, metadata);
            try {
                if(registry.add(vault.load(session, new DefaultVaultMetadataCallbackProvider(prompt)))) {
                    final EnumSet<Path.Type> type = directory.getType();
                    type.add(Path.Type.vault);
                    directory.setType(type);
                }
            }
            catch(BackgroundException e) {
                log.warn("Failure {} loading vault", e);
                throw new VaultUnlockCancelException(vault, e);
            }
            return vault;
        }
    }
}
