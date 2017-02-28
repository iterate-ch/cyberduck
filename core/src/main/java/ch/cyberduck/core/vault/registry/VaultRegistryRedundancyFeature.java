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
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

import java.util.List;

public class VaultRegistryRedundancyFeature implements Redundancy {
    private final Session<?> session;
    private final Redundancy proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryRedundancyFeature(final Session<?> session, final Redundancy proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public String getDefault() {
        return proxy.getDefault();
    }

    @Override
    public List<String> getClasses() {
        return proxy.getClasses();
    }

    @Override
    public void setClass(final Path file, final String redundancy) throws BackgroundException {
        registry.find(session, file).getFeature(session, Redundancy.class, proxy).setClass(file, redundancy);
    }

    @Override
    public String getClass(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Redundancy.class, proxy).getClass(file);
    }
}
