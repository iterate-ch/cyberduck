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
import ch.cyberduck.core.features.Lock;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryLockFeature<T> implements Lock<T> {
    private final Session<?> session;
    private final Lock<T> proxy;
    private final VaultRegistry registry;

    public VaultRegistryLockFeature(final Session<?> session, final Lock<T> proxy, final VaultRegistry registry) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T lock(final Path file) throws BackgroundException {
        return (T) registry.find(session, file).getFeature(session, Lock.class, proxy).lock(file);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void unlock(final Path file, final T token) throws BackgroundException {
        registry.find(session, file).getFeature(session, Lock.class, proxy).unlock(file, token);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryLockFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}