package ch.cyberduck.core.vault.registry;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryDirectoryFeature implements Directory {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final Directory proxy;

    public VaultRegistryDirectoryFeature(final Session<?> session, final Directory proxy, final DefaultVaultRegistry registry) {

        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void mkdir(final Path file) throws BackgroundException {
        registry.find(file).getFeature(session, Directory.class, proxy).mkdir(file);
    }

    @Override
    public void mkdir(final Path file, final String region, final TransferStatus status) throws BackgroundException {
        registry.find(file).getFeature(session, Directory.class, proxy).mkdir(file, region, status);
    }
}
