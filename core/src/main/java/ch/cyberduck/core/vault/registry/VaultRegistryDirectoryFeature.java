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
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryDirectoryFeature<Reply> implements Directory<Reply> {

    private final Session<?> session;
    private final Directory<Reply> proxy;
    private final VaultRegistry registry;

    public VaultRegistryDirectoryFeature(final Session<?> session, final Directory<Reply> proxy, final VaultRegistry registry) {

        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        return registry.find(session, folder).getFeature(session, Directory.class, proxy).mkdir(folder, region, status);
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return proxy.isSupported(workdir);
    }

    @Override
    public Directory<Reply> withWriter(final Write<Reply> writer) {
        return proxy.withWriter(writer);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryDirectoryFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
