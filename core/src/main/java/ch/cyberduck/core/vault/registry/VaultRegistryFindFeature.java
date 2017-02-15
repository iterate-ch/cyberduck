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
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryFindFeature implements Find {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final Find proxy;

    public VaultRegistryFindFeature(final Session<?> session, final Find proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Find.class, proxy).find(file);
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        proxy.withCache(cache);
        return this;
    }

}
