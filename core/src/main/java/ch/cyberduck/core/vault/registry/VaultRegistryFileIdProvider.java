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
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryFileIdProvider implements FileIdProvider {

    private final Session<?> session;
    private final FileIdProvider proxy;
    private final VaultRegistry registry;

    public VaultRegistryFileIdProvider(final Session<?> session, final FileIdProvider proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, FileIdProvider.class, proxy).getFileId(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryFileIdProvider{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
