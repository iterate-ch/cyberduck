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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryAttributesFeature implements AttributesFinder {

    private final Session<?> session;
    private final DefaultVaultRegistry registry;
    private final AttributesFinder proxy;

    private Cache<Path> cache;

    public VaultRegistryAttributesFeature(final Session<?> session, final AttributesFinder proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.registry = registry;
        this.proxy = proxy;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, AttributesFinder.class, proxy)
                .withCache(cache)
                .find(file);
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

}
