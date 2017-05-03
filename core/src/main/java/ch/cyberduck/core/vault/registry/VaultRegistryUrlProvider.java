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
import ch.cyberduck.core.vault.VaultUnlockCancelException;

public class VaultRegistryUrlProvider implements UrlProvider {

    private final Session<?> session;
    private final UrlProvider proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryUrlProvider(final Session<?> session, final UrlProvider proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        try {
            return registry.find(session, file, false).getFeature(session, UrlProvider.class, proxy).toUrl(file);
        }
        catch(VaultUnlockCancelException e) {
            return DescriptiveUrlBag.empty();
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryUrlProvider{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
