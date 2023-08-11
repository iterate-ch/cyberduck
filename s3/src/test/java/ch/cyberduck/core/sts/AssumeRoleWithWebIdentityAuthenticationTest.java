package ch.cyberduck.core.sts;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3ObjectListService;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.test.TestcontainerTest;

import org.jets3t.service.security.AWSSessionCredentials;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class AssumeRoleWithWebIdentityAuthenticationTest extends AbstractOidcTest {

    @Test
    public void testSuccessfulLoginViaOidc() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "rouser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback());

        Credentials creds = host.getCredentials();
        assertNotEquals(StringUtils.EMPTY, creds.getUsername());
        assertNotEquals(StringUtils.EMPTY, creds.getPassword());

        assertFalse(creds.getToken().isEmpty());
        assertNotNull(creds.getOauth().getIdToken());
        assertNotNull(creds.getOauth().getRefreshToken());
        assertNotEquals(Optional.of(Long.MAX_VALUE).get(), creds.getOauth().getExpiryInMilliseconds());
        session.close();
    }

    @Test
    public void testInvalidUserName() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("WrongUsername", "rouser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertThrows(LoginFailureException.class, () -> session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback()));
        session.close();
    }

    @Test
    public void testInvalidPassword() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "invalidPassword"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertThrows(LoginFailureException.class, () -> session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback()));
        session.close();
    }

    @Test
    public void testTokenRefresh() throws BackgroundException, InterruptedException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback());

        String firstAccessToken = host.getCredentials().getOauth().getIdToken();
        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();
        String firstSessionToken = ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken();

        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        Thread.sleep(1100 * 30);
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));

        assertNotEquals(firstAccessToken, host.getCredentials().getOauth().getIdToken());
        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
        assertNotEquals(firstSessionToken, ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken());
        session.close();
    }

    /**
     * only use with the below specified changes in the keycloak config json file and run as separate test
     * set config keycloak-realm.json:
     * "access.token.lifespan": "930"
     * "ssoSessionMaxLifespan": 1100,
     */
    @Test
    @Ignore
    public void testSTSCredentialsExpiredOAuthToken() throws BackgroundException, InterruptedException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback());

        String firstAccessToken = host.getCredentials().getOauth().getAccessToken();
        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();
        assertTrue(session.getClient().getProviderCredentials() instanceof AWSSessionCredentials);

        String firstSessionToken = ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken();
        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3ObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).isEmpty());
        Thread.sleep(1000 * 910);
        assertFalse(new S3ObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).isEmpty());
        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
        assertNotEquals(firstSessionToken, ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken());
        assertEquals(firstAccessToken, host.getCredentials().getOauth().getAccessToken());
    }

    /**
     * This test fails if the x-minio Headers are not read because of InvalidAccessKeyId error code which has no response body.
     * Adjust the sleep time according to the network latency
     */
    @Test
    @Ignore
    public void testBucketRequestBeforeTokenExpiryFailsBecauseOfLatency() throws BackgroundException, InterruptedException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback());

        String firstAccessToken = host.getCredentials().getOauth().getIdToken();
        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();

        // Time of latency may vary and so the time needs to be adjusted accordingly
        Thread.sleep(28820);
        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertFalse(new S3ObjectListService(session, new S3AccessControlListFeature(session)).list(container, new DisabledListProgressListener()).isEmpty());

        assertNotEquals(firstAccessToken, host.getCredentials().getOauth().getIdToken());
        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
        session.close();
    }
}