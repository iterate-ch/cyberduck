package ch.cyberduck.test;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultHostPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.junit.BeforeClass;

import java.util.HashMap;
import java.util.Map;

import com.bettercloud.vault.SslConfig;
import com.bettercloud.vault.Vault;
import com.bettercloud.vault.VaultConfig;
import com.bettercloud.vault.VaultException;

public class VaultTest {

    private static final String VAULT_PATH = "kv/test.properties";
    private static final long VAULT_RENEW_THRESHOLD = 86400; // Renew if TTL is less

    protected static Vault VAULT;
    protected static Map<String, String> PROPERTIES;

    @BeforeClass
    public static void vault() {
        final VaultConfig config;
        try {
            config = new VaultConfig()
                    .engineVersion(2)
                    .sslConfig(new SslConfig().verify(false))
                    .address("https://vault.iterate.ch")
                    .token(System.getenv("VAULT_TOKEN"))
                    .build();
            VAULT = new Vault(config);
            if(VAULT.auth().lookupSelf().getTTL() < VAULT_RENEW_THRESHOLD) {
                VAULT.auth().renewSelf();
            }
            PROPERTIES = VAULT.logical().read(VAULT_PATH).getData();
        }
        catch(VaultException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeClass
    public static void register() {
        PreferencesFactory.get().setDefault("factory.passwordstore.class", TestPasswordStore.class.getName());
    }

    public static class TestPasswordStore extends DefaultHostPasswordStore {

        private final HostUrlProvider provider = new HostUrlProvider().withPath(false);

        @Override
        public void save(final Host bookmark) throws AccessDeniedException {
            super.save(bookmark);
            try {
                VAULT.logical().write(VAULT_PATH, new HashMap<>(PROPERTIES));
            }
            catch(VaultException e) {
                throw new AccessDeniedException(e.getMessage(), e);
            }
        }

        @Override
        public void delete(final Host bookmark) throws AccessDeniedException {
            super.delete(bookmark);
            try {
                VAULT.logical().write(VAULT_PATH, new HashMap<>(PROPERTIES));
            }
            catch(VaultException e) {
                throw new AccessDeniedException(e.getMessage(), e);
            }
        }

        @Override
        public void addPassword(final String serviceName, final String accountName, final String password) throws AccessDeniedException {
            PROPERTIES.put(String.format("%s/%s", serviceName, accountName), password);
        }

        @Override
        public String getPassword(final String serviceName, final String accountName) throws AccessDeniedException {
            return PROPERTIES.get(String.format("%s/%s", serviceName, accountName));
        }

        @Override
        public void deletePassword(final String serviceName, final String user) {
            PROPERTIES.remove(String.format("%s/%s", serviceName, user));
        }

        @Override
        public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) throws AccessDeniedException {
            PROPERTIES.put(provider.get(scheme, port, user, hostname, StringUtils.EMPTY), password);
        }

        @Override
        public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) throws AccessDeniedException {
            return PROPERTIES.get(provider.get(scheme, port, user, hostname, StringUtils.EMPTY));
        }

        @Override
        public void deletePassword(final Scheme scheme, final int port, final String hostname, final String user) {
            PROPERTIES.remove(provider.get(scheme, port, user, hostname, StringUtils.EMPTY));
        }
    }
}
