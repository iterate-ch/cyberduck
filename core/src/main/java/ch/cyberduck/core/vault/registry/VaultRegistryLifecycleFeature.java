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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryLifecycleFeature implements Lifecycle {
    private final Session<?> session;
    private final Lifecycle proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryLifecycleFeature(final Session<?> session, final Lifecycle proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public LifecycleConfiguration getConfiguration(final Path container) throws BackgroundException {
        return registry.find(session, container).getFeature(session, Lifecycle.class, proxy).getConfiguration(container);
    }

    @Override
    public void setConfiguration(final Path container, final LifecycleConfiguration configuration) throws BackgroundException {
        registry.find(session, container).getFeature(session, Lifecycle.class, proxy);
    }
}
