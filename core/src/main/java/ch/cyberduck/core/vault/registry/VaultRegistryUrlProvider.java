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

import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryUrlProvider implements UrlProvider {
    private final DefaultVaultRegistry registry;
    private final Session<?> session;
    private final UrlProvider proxy;

    public VaultRegistryUrlProvider(final Session<?> session, final UrlProvider proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        return registry.find(file).getFeature(session, UrlProvider.class, proxy).toUrl(file);
    }
}
