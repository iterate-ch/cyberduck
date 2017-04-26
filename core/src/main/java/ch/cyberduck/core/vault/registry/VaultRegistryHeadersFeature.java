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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.vault.VaultRegistry;

import java.util.Map;

public class VaultRegistryHeadersFeature implements Headers {

    private final Session<?> session;
    private final Headers proxy;
    private final VaultRegistry registry;

    public VaultRegistryHeadersFeature(final Session<?> session, final Headers proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public Map<String, String> getDefault(final Local local) {
        return proxy.getDefault(local);
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        return registry.find(session, file).getFeature(session, Headers.class, proxy).getMetadata(file);
    }

    @Override
    public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
        registry.find(session, file).getFeature(session, Headers.class, proxy).setMetadata(file, metadata);
    }
}
