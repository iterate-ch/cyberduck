package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.UnsecureHostPasswordStore;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.serializer.impl.dd.PlistSerializer;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.ServiceLoader;

import static org.junit.Assert.*;

public class BrickProtocolTest {

    @Test
    public void testFeatures() {
        assertEquals(Protocol.Case.insensitive, new BrickProtocol().getCaseSensitivity());
        assertEquals(Protocol.DirectoryTimestamp.explicit, new BrickProtocol().getDirectoryTimestamp());
    }

    @Test
    public void testSessionClass() throws Exception {
        final BrickProtocol protocol = new BrickProtocol();
        assertEquals("Files.com", protocol.getDescription());
        assertEquals(BrickSession.class, Class.forName(protocol.getPrefix() + "Session"));
    }

    @Test
    public void testServiceDiscovery() {
        for(Protocol protocol : ServiceLoader.load(Protocol.class)) {
            if(protocol instanceof BrickProtocol) {
                return;
            }
        }
        fail("Files.com protocol is not registered");
    }

    @Test
    public void testBookmarkRoundTrip() throws Exception {
        final BrickProtocol protocol = new BrickProtocol();
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(protocol)));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Brick.cyberduckprofile"));
        factory.register(profile);
        assertEquals(profile, factory.forName("brick", profile.getProvider()));
        assertEquals(profile, factory.forName("brick-" + profile.getProvider()));
        assertEquals("brick", profile.getIdentifier());
        assertEquals("brick", protocol.getIdentifier());
        final PlistSerializer saved = new PlistSerializer();
        saved.setStringForKey("brick", "Protocol");
        saved.setStringForKey(profile.getProvider(), "Provider");
        saved.setStringForKey("customer.files.com", "Hostname");
        saved.setStringForKey("jane", "Username");
        saved.setStringForKey("old-bookmark", "Nickname");
        final Host bookmark = new HostDictionary<>(factory).deserialize(saved.getSerialized());
        assertEquals(profile, bookmark.getProtocol());
        assertEquals("customer.files.com", bookmark.getHostname());
        assertEquals("jane", bookmark.getCredentials().getUsername());
        assertEquals("old-bookmark", bookmark.getNickname());
        assertEquals("brick", bookmark.serialize(new PlistSerializer()).objectForKey("Protocol").toString());
    }

    @Test
    public void testKeyUsesExistingPasswordSlot() throws Exception {
        final Properties passwords = new Properties();
        final UnsecureHostPasswordStore store = new UnsecureHostPasswordStore(null) {
            @Override
            protected Properties load() {
                return passwords;
            }

            @Override
            protected void save(final Properties properties) {
            }
        };
        final Host legacy = new Host(new TestProtocol(Scheme.https), "customer.files.com", new Credentials("jane", "legacy-key"));
        store.save(legacy);
        final Host current = new Host(new BrickProtocol(), legacy.getHostname(), new Credentials("jane"));
        assertEquals("legacy-key", store.findLoginPassword(current));
        current.getCredentials().setPassword("oauth-issued-key");
        store.save(current);
        assertEquals("oauth-issued-key", store.findLoginPassword(legacy));
        assertEquals(1, passwords.size());
    }

    @Test
    public void testOAuthConfiguration() {
        final BrickProtocol protocol = new BrickProtocol();
        assertEquals("cyberduck", protocol.getOAuthClientId());
        assertEquals("", protocol.getOAuthClientSecret());
        assertEquals("/auth/oauth/authorize", protocol.getOAuthAuthorizationUrl());
        assertEquals("/auth/oauth/token", protocol.getOAuthTokenUrl());
        assertEquals("http://127.0.0.1/oauth/callback", protocol.getOAuthRedirectUrl());
        assertEquals(Collections.singletonList("desktop"), protocol.getOAuthScopes());
        assertTrue(protocol.isOAuthPKCE());
        assertFalse(protocol.isUsernameConfigurable());
        assertFalse(protocol.isPasswordConfigurable());
        assertTrue(protocol.isOAuthConfigurable());
        assertEquals(LocaleFactory.localizedString("Password", "Credentials"), protocol.getPasswordPlaceholder());
    }

    @Test
    public void testBundledOAuthProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(Collections.singleton(new BrickProtocol()));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Files.com.cyberduckprofile"));
        final LoginOptions options = new LoginOptions(profile);
        assertTrue(options.oauth);
        assertFalse(options.password);
        assertFalse(options.user);
        assertFalse(options.token);
        assertTrue(new Credentials().validate(profile, options));
        assertEquals("cyberduck", profile.getOAuthClientId());
        assertEquals("/auth/oauth/authorize", profile.getOAuthAuthorizationUrl());
        assertEquals("/auth/oauth/token", profile.getOAuthTokenUrl());
        assertEquals("http://127.0.0.1/oauth/callback", profile.getOAuthRedirectUrl());
        assertEquals("/desktopSetup?client_id=cyberduck", profile.getOAuthSuccessUrl());
        assertEquals(Collections.singletonList("desktop"), profile.getOAuthScopes());
        final PlistSerializer serialized = new PlistSerializer();
        profile.serialize(serialized);
        assertEquals(profile.getOAuthSuccessUrl(), serialized.getSerialized().objectForKey(Profile.OAUTH_SUCCESS_URL_KEY).toString());
    }
}
