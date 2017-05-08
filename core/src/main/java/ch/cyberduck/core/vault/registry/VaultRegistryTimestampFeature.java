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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.vault.VaultRegistry;

public class VaultRegistryTimestampFeature implements Timestamp {

    private final Session<?> session;
    private final Timestamp proxy;
    private final VaultRegistry registry;

    public VaultRegistryTimestampFeature(final Session<?> session, final Timestamp proxy, final VaultRegistry registry) {
        this.registry = registry;
        this.session = session;
        this.proxy = proxy;
    }

    @Override
    public void setTimestamp(final Path file, final Long modified) throws BackgroundException {
        registry.find(session, file).getFeature(session, Timestamp.class, proxy).setTimestamp(file, modified);
    }

    @Override
    public Long getDefault(final Local file) {
        return proxy.getDefault(file);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryTimestampFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
