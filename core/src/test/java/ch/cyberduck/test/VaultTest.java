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
    public static void credentials() {
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
            throw new RuntimeException("Failed to initialize vault", e);
        }
    }

    public static void add(final String key, final String value) {
        PROPERTIES.put(key, value);
        try {
            VAULT.logical().write(VAULT_PATH, new HashMap<>(PROPERTIES));
        }
        catch(VaultException e) {
            throw new RuntimeException("Failure adding key/value", e);
        }
    }
}
