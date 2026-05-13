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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.vault.VaultLoader;
import ch.cyberduck.core.vault.VaultProvider;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultUnlockCancelException;
import ch.cyberduck.core.vault.VaultVersion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class VaultRegistryFindFeature implements Find {
    private static final Logger log = LogManager.getLogger(VaultRegistryFindFeature.class);

    private final Session<?> session;
    private final Find proxy;
    private final VaultRegistry registry;
    private final VaultLoader lookup;
    private final boolean autodetect;
    private final VaultProvider provider;

    public VaultRegistryFindFeature(final Session<?> session, final Find proxy, final VaultRegistry registry, final VaultLoader lookup) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
        this.lookup = lookup;
        this.autodetect = HostPreferencesFactory.get(session.getHost()).getBoolean("cryptomator.vault.autodetect")
                && HostPreferencesFactory.get(session.getHost()).getBoolean("cryptomator.enable");
        this.provider = session.getFeature(VaultProvider.class);
    }

    @Override
    public boolean find(final Path file, final ListProgressListener listener) throws BackgroundException {
        final Vault vault = registry.find(session, file);
        if(vault.equals(Vault.DISABLED)) {
            if(autodetect) {
                final Path directory = file.getParent();
                final Path vaultConfig = new Path(directory,
                        HostPreferencesFactory.get(session.getHost()).getProperty("cryptomator.vault.config.filename"), EnumSet.of(Path.Type.file));
                final Path key = new Path(directory,
                        HostPreferencesFactory.get(session.getHost()).getProperty("cryptomator.vault.masterkey.filename"), EnumSet.of(Path.Type.file));
                final VaultVersion version = provider.find(directory, proxy, listener);
                if(version != null) {
                    log.info("Found vault config {} or masterkey {}", vaultConfig, key);
                    try {
                        log.info("Found vault {}", directory);
                        return lookup.load(session, directory, version).getFeature(session, Find.class, proxy).find(file, listener);
                    }
                    catch(VaultUnlockCancelException e) {
                        // Continue
                    }
                }
            }
        }
        return vault.getFeature(session, Find.class, proxy).find(file, listener);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryFindFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
