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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.vault.VaultFinderListService;
import ch.cyberduck.core.vault.VaultLookupListener;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VaultRegistryListService implements ListService {
    private static final Logger log = LogManager.getLogger(VaultRegistryListService.class);

    private final VaultRegistry registry;
    private final VaultLookupListener lookup;
    private final Session<?> session;
    private final ListService proxy;
    private final boolean autodetect;

    public VaultRegistryListService(final Session<?> session, final ListService proxy, final VaultRegistry registry, final VaultLookupListener lookup) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
        this.lookup = lookup;
        this.autodetect = new HostPreferences(session.getHost()).getBoolean("cryptomator.vault.autodetect")
                && new HostPreferences(session.getHost()).getBoolean("cryptomator.enable");
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final Vault vault = registry.find(session, directory);
            if(vault.contains(directory)) {
                if(log.isDebugEnabled()) {
                    log.debug("Found vault {}", vault);
                }
                return vault.getFeature(session, ListService.class, proxy).list(directory, listener);
            }
            if(autodetect) {
                if(log.isDebugEnabled()) {
                    log.debug("Look for vault in {}", directory);
                }
                return new VaultFinderListService(session, proxy, lookup, listener).list(directory, listener);
            }
            return proxy.list(directory, listener);
        }
        catch(VaultUnlockCancelException e) {
            if(log.isWarnEnabled()) {
                log.warn("Canceled loading vault {} with failure {}", e.getVault(), e);
            }
            return proxy.list(directory, listener);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryListService{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void preflight(final Path directory) throws BackgroundException {
        proxy.preflight(directory);
    }
}
