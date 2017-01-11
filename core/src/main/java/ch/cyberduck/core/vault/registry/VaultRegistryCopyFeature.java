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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.vault.DefaultVaultRegistry;

public class VaultRegistryCopyFeature implements Copy {

    private final Session<?> session;
    private final Copy proxy;
    private final DefaultVaultRegistry registry;

    public VaultRegistryCopyFeature(final Session<?> session, final Copy proxy, final DefaultVaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    public void copy(final Path source, final Path copy) throws BackgroundException {
        registry.find(session, source).getFeature(session, Copy.class, proxy).copy(source, copy);
    }
}
