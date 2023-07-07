package ch.cyberduck.core.oidc.testenv;

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
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.test.TestcontainerTest;

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
        // credentials from STS are written to the client object in the S3Session and not into the Credential object from the Host.
        assertTrue(creds.getToken().isEmpty());
        assertNotNull(creds.getOauth().getAccessToken());
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
        host.setProperty("s3.bucket.virtualhost.disable", String.valueOf(true));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        String firstAccessToken = host.getCredentials().getOauth().getAccessToken();
        String firstRefreshToken = host.getCredentials().getOauth().getRefreshToken();
        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        Thread.sleep(1100 * 30);
        assertTrue(host.getCredentials().getOauth().isExpired());
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        String secondAccessToken = host.getCredentials().getOauth().getAccessToken();
        String secondRefreshToken = host.getCredentials().getOauth().getRefreshToken();
        assertNotEquals(firstAccessToken, secondAccessToken);
        assertNotEquals(firstRefreshToken, secondRefreshToken);
    }

    @Test
    public void testOauthTokenExpired() throws BackgroundException, InterruptedException {
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        host.setProperty("s3.bucket.virtualhost.disable", String.valueOf(true));
        final S3Session session = new S3Session(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        Thread.sleep(1100 * 30);
        assertTrue(host.getCredentials().getOauth().isExpired());
    }

/*    @Test(expected = ExpiredTokenException.class)
    public void testSTSTokenExpired() throws BackgroundException, InterruptedException {
        final Credentials c = new Credentials("rawuser", "rawuser");
        final Host host = new Host(profile, profile.getDefaultHostname(), c);
        host.setProperty("s3.bucket.virtualhost.disable", String.valueOf(true));

        String expiredAccessToken = "expired_access_token";
        String refreshToken = host.getCredentials().getOauth().getRefreshToken();
        c.setOauth(new OAuthTokens(expiredAccessToken, refreshToken, -1L));


        final S3Session session = new S3Session(host);

        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());

    }*/


    //separate STS Service test - maybe not possible
//    @Test
//    public void testSTSAssumeRoleWithWebIdentity() throws BackgroundException {
//        NonAwsSTSCredentialsConfigurator sts = new NonAwsSTSCredentialsConfigurator(new DefaultX509TrustManager(), new DefaultX509KeyManager(), new DisabledLoginCallback());
//    }
}