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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryIdProvider implements IdProvider {

    private final Session<?> session;
    private final IdProvider proxy;
    private final VaultRegistry registry;

    public VaultRegistryIdProvider(final Session<?> session, final IdProvider proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public String getFileid(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, IdProvider.class, proxy).getFileid(file);
    }

    @Override
    public IdProvider withCache(final Cache<Path> cache) {
        proxy.withCache(cache);
        return this;
    }
}
