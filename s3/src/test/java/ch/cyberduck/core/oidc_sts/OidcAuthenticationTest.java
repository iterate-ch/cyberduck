package ch.cyberduck.core.oidc_sts;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.sts.STSCredentialsRequestInterceptor;
import ch.cyberduck.test.TestcontainerTest;

import org.jets3t.service.security.AWSSessionCredentials;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class OidcAuthenticationTest extends AbstractOidcTest {
    @Test
    public void testSuccessfulLoginViaOidc() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "rouser"));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());

        Credentials creds = host.getCredentials();
        assertNotEquals(StringUtils.EMPTY, creds.getUsername());
        assertNotEquals(StringUtils.EMPTY, creds.getPassword());
        // credentials from STS are written to the S3Session's client object and not into the credential object from the Host.
        assertTrue(creds.getToken().isEmpty());
        assertNotNull(creds.getOauth().getIdToken());
        assertNotNull(creds.getOauth().getRefreshToken());
        assertNotEquals(Optional.of(Long.MAX_VALUE).get(), creds.getOauth().getExpiryInMilliseconds());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testInvalidUserName() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("WrongUsername", "rouser"));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test(expected = LoginFailureException.class)
    public void testInvalidPassword() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "invalidPassword"));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        session.close();
    }

    @Test
    public void testTokenRefresh() throws BackgroundException, InterruptedException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());

        String firstAccessToken = session.getAuthorizationService().getTokens().getIdToken();
        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();
        String firstSessionToken = ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken();

        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        Thread.sleep(1100 * 30);
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));

        assertNotEquals(firstAccessToken, session.getAuthorizationService().getTokens().getIdToken());
        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
        assertNotEquals(firstSessionToken, ((AWSSessionCredentials) session.getClient().getProviderCredentials()).getSessionToken());
        session.close();
    }

    @Test
    public void testSTSCredentialExpiryTimeIsBoundToOAuthExpiryTime() throws BackgroundException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        host.setProperty("s3.assumerole.durationseconds", "900");
        assertEquals(new HostPreferences(host).getInteger("s3.assumerole.durationseconds"), 900);
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());

        // assert that STS credentials expires with the oAuth token even though the duration seconds is valid for longer
        STSCredentialsRequestInterceptor authorizationService = session.getAuthorizationService();
        assertTrue(40 > ((authorizationService.getStsExpiryInMilliseconds() - System.currentTimeMillis()) / 1000));
        assertEquals(Optional.of(authorizationService.getStsExpiryInMilliseconds()).get() / 1000, authorizationService.getTokens().getExpiryInMilliseconds() / 1000);

        session.close();
    }

    /** only use with the below specified changes in the keycloak config json file and run as separate test
     * set config keycloak-realm.json:
     *      "access.token.lifespan": "930"
     *      "ssoSessionMaxLifespan": 1100,
     */
    /*@Test
    public void testSTSCredentialsExpiredValidOAuthToken() throws BackgroundException, InterruptedException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        host.setProperty("s3.assumerole.durationseconds", "900");
        assertEquals(new HostPreferences(host).getInteger("s3.assumerole.durationseconds"), 900);
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());

        STSCredentialsRequestInterceptor authorizationService = session.getAuthorizationService();
        String firstAccessToken = authorizationService.getTokens().getAccessToken();
        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();
        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        Thread.sleep(1000 * 910);
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
        assertEquals(firstAccessToken, authorizationService.getTokens().getAccessToken());
    }*/

    /**
     *     This test fails if the x-minio Headers are not read because of InvalidAccessKeyId error code which has no response body.
     *     Adjust the sleep time according to the network latency
     */
//    @Test
//    public void testBucketRequestBeforeTokenExpiryFailsBecauseOfLatency() throws BackgroundException, InterruptedException {
//        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
//        final S3Session session = new S3Session(host);
//        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
//        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
//
//        String firstAccessToken = session.getAuthorizationService().getTokens().getIdToken();
//        String firstAccessKey = session.getClient().getProviderCredentials().getAccessKey();
//
//        // Time of latency may vary and so the time needs to be adjusted accordingly
//        Thread.sleep(28820);
//        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
//        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
//
//        assertNotEquals(firstAccessToken, session.getAuthorizationService().getTokens().getIdToken());
//        assertNotEquals(firstAccessKey, session.getClient().getProviderCredentials().getAccessKey());
//        session.close();
//    }
}