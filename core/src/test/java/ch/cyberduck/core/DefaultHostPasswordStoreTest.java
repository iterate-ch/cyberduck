package ch.cyberduck.core;

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

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.*;

public class DefaultHostPasswordStoreTest {

    @Test
    public void testLoginPasswordRoundTripWithAndWithoutUsername() throws Exception {
        final UnsecureHostPasswordStore store = new UnsecureHostPasswordStore(null) {
            private final Properties properties = new Properties();

            @Override
            protected Properties load() {
                return properties;
            }

            @Override
            protected void save(final Properties properties) {
                // Keep credentials in memory for this test.
            }
        };
        final Protocol protocol = new TestProtocol(Scheme.https) {
            @Override
            public String getPasswordPlaceholder() {
                return "API Key";
            }
        };
        for(String username : new String[]{"", "jane"}) {
            final Host host = new Host(protocol, "customer.files.com", new Credentials(username, "api-key"));
            store.addLoginPassword(host, "api-key");
            assertEquals("api-key", store.findLoginPassword(host));
            store.deleteLoginPassword(host);
            assertNull(store.findLoginPassword(host));
            store.save(host);
            assertEquals("api-key", store.findLoginPassword(host));
            store.deleteLoginPassword(host);
            assertNull(store.findLoginPassword(host));
        }
    }

    @Test
    public void testGetOAuthPrefix() {
        final String[] prefix = DefaultHostPasswordStore.getOAuthPrefix(new Host(new TestProtocol(Scheme.https) {
            @Override
            public String getOAuthClientId() {
                return "clientid";
            }

            @Override
            public String getOAuthClientSecret() {
                return "clientsecret";
            }

            @Override
            public String getOAuthRedirectUrl() {
                return "x-cyberduck-action:oauth";
            }
        })).toArray(new String[0]);
        assertEquals("clientid", prefix[0]);
        assertEquals("Test", prefix[1]);
    }

    @Test
    public void testGetOAuthPrefixWithUsername() {
        final String[] prefix = DefaultHostPasswordStore.getOAuthPrefix(new Host(new TestProtocol(Scheme.https) {
            @Override
            public String getOAuthClientId() {
                return "clientid";
            }

            @Override
            public String getOAuthClientSecret() {
                return "clientsecret";
            }

            @Override
            public String getOAuthRedirectUrl() {
                return "x-cyberduck-action:oauth";
            }
        }).setCredentials(new Credentials("user"))).toArray(new String[0]);
        assertEquals("clientid (user)", prefix[0]);
        assertEquals("Test (user)", prefix[1]);
    }
}
