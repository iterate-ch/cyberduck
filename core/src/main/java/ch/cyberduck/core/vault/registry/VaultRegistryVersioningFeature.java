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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.vault.VaultRegistry;

import java.util.Map;

public class VaultRegistryVersioningFeature implements Versioning {

    private final Session<?> session;
    private final Versioning proxy;
    private final VaultRegistry registry;

    public VaultRegistryVersioningFeature(final Session<?> session, final Versioning proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Versioning withCache(final Map<Path, VersioningConfiguration> cache) {
        proxy.withCache(cache);
        return this;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) throws BackgroundException {
        return registry.find(session, container).getFeature(session, Versioning.class, proxy).getConfiguration(container);
    }

    @Override
    public void setConfiguration(final Path container, final LoginCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        registry.find(session, container).getFeature(session, Versioning.class, proxy).setConfiguration(container, prompt, configuration);
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        registry.find(session, file).getFeature(session, Versioning.class, proxy).revert(file);
    }

    @Override
    public Credentials getToken(final LoginCallback controller) throws ConnectionCanceledException {
        return proxy.getToken(controller);
    }
}
