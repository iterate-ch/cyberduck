package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UnsecureHostPasswordStore;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpServer;

import static org.junit.Assert.*;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.*;

public class BrickCredentialsDeleteFeatureTest {
    @Test
    public void testDeletionRestoresOAuthTokensBeforeRemovingColdBookmarkCredentials() throws Exception {
        try(Fixture fixture = new Fixture("jane", 204)) {
            fixture.store.addPassword(fixture.host.getProtocol().getScheme(), fixture.host.getPort(), fixture.host.getHostname(), "jane", "legacy-key");
            fixture.host.getCredentials().setOauth(new OAuthTokens("api-key", null, Long.MAX_VALUE));
            fixture.store.save(fixture.host);
            fixture.host.getCredentials().setOauth(OAuthTokens.EMPTY);
            new BrickCredentialsDeleteFeature(fixture.store).delete(fixture.host);
            assertEquals("api-key", fixture.revoked.get());
            assertFalse(fixture.store.findOAuthTokens(fixture.host).validate());
            assertNull(fixture.store.findLoginPassword(fixture.host));
            assertFalse(fixture.host.getCredentials().isOAuthAuthentication());
            assertEquals("false", fixture.host.getProperty(BrickOAuth2AuthorizationService.VERIFIED));
        }
    }

    @Test
    public void testDeletionBeforeMigrationRemovesLegacyKeyIncludingUsernameLessSlot() throws Exception {
        for(String username : new String[]{"jane", ""}) {
            try(Fixture fixture = new Fixture(username, 204)) {
                fixture.store.addPassword(fixture.host.getProtocol().getScheme(), fixture.host.getPort(), fixture.host.getHostname(),
                        username.isEmpty() ? fixture.host.getProtocol().getPasswordPlaceholder() : username, "legacy-key");
                new BrickCredentialsDeleteFeature(fixture.store).delete(fixture.host);
                assertEquals("legacy-key", fixture.revoked.get());
                assertNull(fixture.store.findLoginPassword(fixture.host));
                assertFalse(fixture.store.findOAuthTokens(fixture.host).validate());
            }
        }
    }

    @Test
    public void testRevocationFailureStillDeletesLocalToken() throws Exception {
        try(Fixture fixture = new Fixture("jane", 503)) {
            fixture.host.getCredentials().setOauth(new OAuthTokens("api-key", null, Long.MAX_VALUE));
            fixture.store.save(fixture.host);
            assertThrows(BackgroundException.class, () -> new BrickCredentialsDeleteFeature(fixture.store).delete(fixture.host));
            assertEquals("api-key", fixture.revoked.get());
            assertFalse(fixture.store.findOAuthTokens(fixture.host).validate());
            assertFalse(fixture.host.getCredentials().isOAuthAuthentication());
        }
    }

    private static final class Fixture implements AutoCloseable {
        private final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        private final AtomicReference<String> revoked = new AtomicReference<>();
        private final Host host;
        private final HostPasswordStore store;

        private Fixture(final String username, final int status) throws Exception {
            server.createContext("/api/rest/v1/api_key", exchange -> {
                revoked.set(exchange.getRequestHeaders().getFirst("X-FilesAPI-Key"));
                exchange.sendResponseHeaders(status, -1);
                exchange.close();
            });
            server.start();
            final Profile profile = new ProfilePlistReader(new ProtocolFactory(Collections.singleton(new BrickProtocol())))
                    .read(this.getClass().getResourceAsStream("/Files.com.cyberduckprofile"));
            final Profile protocol = mock(Profile.class, delegatesTo(profile));
            doReturn(Scheme.http).when(protocol).getScheme();
            host = new Host(protocol, "127.0.0.1", server.getAddress().getPort(), new Credentials(username));
            host.setProperty("connection.retry", "0");
            final Properties passwords = new Properties();
            store = new UnsecureHostPasswordStore(null) {
                @Override
                protected Properties load() {
                    return passwords;
                }

                @Override
                protected void save(final Properties values) {
                }
            };
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
