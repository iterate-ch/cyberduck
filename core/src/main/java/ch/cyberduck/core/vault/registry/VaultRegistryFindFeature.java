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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.PasswordStore;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Vault;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.vault.VaultFactory;
import ch.cyberduck.core.vault.VaultLookupListener;
import ch.cyberduck.core.vault.VaultRegistry;

import org.apache.log4j.Logger;

import java.util.EnumSet;

public class VaultRegistryFindFeature implements Find {
    private static final Logger log = Logger.getLogger(VaultRegistryFindFeature.class);

    private static final String MASTERKEY_FILE_NAME = "masterkey.cryptomator";

    private final Preferences preferences = PreferencesFactory.get();

    private final Session<?> session;
    private final Find proxy;
    private final VaultRegistry registry;
    private final VaultLookupListener lookup;
    private final PasswordStore keychain;

    private boolean autodetect = preferences.getBoolean("cryptomator.vault.autodetect")
        && preferences.getBoolean("cryptomator.enable");

    private Cache<Path> cache = PathCache.empty();

    public VaultRegistryFindFeature(final Session<?> session, final Find proxy, final VaultRegistry registry, final VaultLookupListener lookup, final PasswordStore keychain) {
        this.session = session;
        this.proxy = proxy;
        this.registry = registry;
        this.lookup = lookup;
        this.keychain = keychain;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        final Vault vault = registry.find(session, file);
        if(vault.equals(Vault.DISABLED)) {
            if(autodetect) {
                final Path key = new Path(file.getParent(), MASTERKEY_FILE_NAME, EnumSet.of(Path.Type.file));
                if(proxy.withCache(cache).find(key)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Found master key %s", key));
                    }
                    final Vault cryptomator = VaultFactory.get(file.getParent(), keychain);
                    if(!cryptomator.equals(Vault.DISABLED)) {
                        lookup.found(cryptomator);
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Found vault %s", cryptomator));
                        }
                        return cryptomator.getFeature(session, Find.class, proxy)
                            .withCache(cache)
                            .find(file);
                    }
                }
            }
        }
        return vault.getFeature(session, Find.class, proxy)
            .withCache(cache)
            .find(file);
    }

    @Override
    public Find withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }

    public VaultRegistryFindFeature withAutodetect(final boolean autodetect) {
        this.autodetect = autodetect && preferences.getBoolean("cryptomator.enable");
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("VaultRegistryFindFeature{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
