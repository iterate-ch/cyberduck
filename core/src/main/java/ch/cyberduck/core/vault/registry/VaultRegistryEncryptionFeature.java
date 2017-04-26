package ch.cyberduck.core.vault.registry;

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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.vault.VaultRegistry;

import java.util.Set;

public class VaultRegistryEncryptionFeature implements Encryption {

    private final Session<?> session;
    private final Encryption proxy;
    private final VaultRegistry registry;

    public VaultRegistryEncryptionFeature(final Session<?> session, final Encryption proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Set<Algorithm> getKeys(final Path file, final LoginCallback prompt) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Encryption.class, proxy).getKeys(file, prompt);
    }

    @Override
    public Algorithm getEncryption(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Encryption.class, proxy).getEncryption(file);
    }

    @Override
    public void setEncryption(final Path file, final Algorithm algorithm) throws BackgroundException {
        registry.find(session, file).getFeature(session, Encryption.class, proxy).setEncryption(file, algorithm);
    }

    @Override
    public Algorithm getDefault(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Encryption.class, proxy).getDefault(file);
    }
}
