package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.features.Metadata;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class GoogleStorageSessionTest extends AbstractGoogleStorageTest {

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidRefreshToken() throws Exception {
        session.close();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore() {
                @Override
                public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                    if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Refresh Token")) {
                        return "a";
                    }
                    return null;
                }
            }, new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    @Test
    public void testConnectInvalidAccessTokenRefreshToken() throws Exception {
        session.close();
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore() {
                @Override
                public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                    if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Access Token")) {
                        // Mark as not expired
                        PreferencesFactory.get().setProperty("googlestorage.oauth.expiry", System.currentTimeMillis() + 60 * 1000);
                        return "a";
                    }
                    if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Refresh Token")) {
                        return System.getProperties().getProperty("googlestorage.refreshtoken");
                    }
                    return null;
                }
            }, new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectInvalidProjectId() throws Exception {
        session.close();
        session.getHost().setCredentials(
            new Credentials(System.getProperties().getProperty("google.projectid") + "1", null)
        );
        final LoginConnectionService login = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
            new DisabledPasswordStore() {
                @Override
                public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
                    if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Access Token")) {
                        return System.getProperties().getProperty("googlestorage.accesstoken");
                    }
                    if(user.equals("Google Cloud Storage (api-project-408246103372) OAuth2 Refresh Token")) {
                        return System.getProperties().getProperty("googlestorage.refreshtoken");
                    }
                    return null;
                }
            }, new DisabledProgressListener());
        login.check(session, new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testConnectMissingKey() throws Exception {
        session.close();
        session.getHost().getCredentials().setOauth(OAuthTokens.EMPTY);
        session.login(Proxy.DIRECT, new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username,
                                      final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                assertEquals("OAuth2 Authentication", title);
                throw new LoginCanceledException();
            }
        }, null);
    }

    @Test
    public void testFeatures() {
        assertNotNull(session.getFeature(Versioning.class));
        assertNotNull(session.getFeature(AclPermission.class));
        assertNotNull(session.getFeature(Lifecycle.class));
        assertNotNull(session.getFeature(DistributionConfiguration.class));
        assertNotNull(session.getFeature(Logging.class));
        assertNotNull(session.getFeature(Metadata.class));
        assertNotNull(session.getFeature(Headers.class));
    }

    @Test(expected = LoginCanceledException.class)
    public void testInvalidProjectId() throws Exception {
        session.getHost().setCredentials(
            new Credentials("duck-1432", "")
        );
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = LoginCanceledException.class)
    public void testProjectIdNoAuthorization() throws Exception {
        session.getHost().setCredentials(
            new Credentials("stellar-perigee-775", "")
        );
        session.login(Proxy.DIRECT, new DisabledLoginCallback() {
            @Override
            public Credentials prompt(final Host bookmark, final String username, final String title, final String reason, final LoginOptions options) {
                // OAuth2
                return new Credentials("", "");
            }
        }, new DisabledCancelCallback());
    }
}
