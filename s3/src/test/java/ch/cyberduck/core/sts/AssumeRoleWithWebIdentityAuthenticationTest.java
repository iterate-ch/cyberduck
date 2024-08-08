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
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.TemporaryAccessTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3BucketListService;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.test.TestcontainerTest;

import org.jets3t.service.security.AWSSessionCredentials;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.shaded.org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class AssumeRoleWithWebIdentityAuthenticationTest extends AbstractAssumeRoleWithWebIdentityTest {

    @ClassRule
    public static ComposeContainer compose = prepareDockerComposeContainer();

    @Test
    public void testSuccessfulLogin() throws BackgroundException {
        final Protocol profile = new ProfilePlistReader(new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())))).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "rouser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());

        final Credentials credentials = host.getCredentials();
        assertNotEquals("rouser", credentials.getUsername());
        assertNotEquals(StringUtils.EMPTY, credentials.getPassword());

        assertNotNull(credentials.getTokens().getAccessKeyId());
        assertNotNull(credentials.getTokens().getSecretAccessKey());
        assertNotNull(credentials.getTokens().getSessionToken());
        assertNotNull(credentials.getOauth().getIdToken());
        assertNotNull(credentials.getOauth().getRefreshToken());
        assertNotEquals(Optional.of(Long.MAX_VALUE).get(), credentials.getOauth().getExpiryInMilliseconds());
        session.close();
    }

    @Test
    public void testInvalidUserName() throws BackgroundException {
        final Protocol profile = new ProfilePlistReader(new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())))).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("WrongUsername", "rouser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertThrows(LoginFailureException.class, () -> session.login(new DisabledLoginCallback(), new DisabledCancelCallback()));
        session.close();
    }

    @Test
    public void testInvalidPassword() throws BackgroundException {
        final Protocol profile = new ProfilePlistReader(new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())))).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rouser", "invalidPassword"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        assertThrows(LoginFailureException.class, () -> session.login(new DisabledLoginCallback(), new DisabledCancelCallback()));
        session.close();
    }

    @Test
    public void testTokenRefresh() throws BackgroundException, InterruptedException {
        final Protocol profile = new ProfilePlistReader(new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())))).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());

        final Credentials credentials = host.getCredentials();
        final OAuthTokens oauth = credentials.getOauth();
        assertTrue(oauth.validate());
        final TemporaryAccessTokens tokens = credentials.getTokens();
        assertTrue(tokens.validate());

        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));

        Thread.sleep(OAUTH_TTL_MILLIS);
        assertTrue(credentials.getOauth().isExpired());
        assertTrue(credentials.getTokens().isExpired());

        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));

        session.close();
    }

    /**
     * Test flow in <code>S3AuthenticationResponseInterceptor</code>> with invalid OAuth tokens found. Assuming role
     * initially fails with <code>InvalidParameterValue</code>
     * Fetch OpenID Connect Id token initially fails because of invalid refresh token. Must re-run OAuth flow.
     */
    @Test
    public void testLoginInvalidOAuthTokensLogin() throws Exception {
        final Protocol profile = new ProfilePlistReader(new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())))).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
        final Credentials credentials = new Credentials("rouser", "rouser")
                .withOauth(new OAuthTokens(
                        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDQmpaYUNSeU5USmZqV0VmMU1fZXZLRVliMEdGLXU0QzhjZ3RZYnBtZUlFIn0.eyJleHAiOjE2OTE5OTc3MzUsImlhdCI6MTY5MTk5NzcwNSwianRpIjoiNDA1MGUxMGYtNzZjNC00MjYwLTk1YTctZTMyMTE2YTA3N2NlIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9jeWJlcmR1Y2tyZWFsbSIsInN1YiI6IjMzNGRiZWIwLTE5NWQtNDJhMS1hMWQ2LTEyODFmMDBiZmIxZCIsInR5cCI6IkJlYXJlciIsImF6cCI6Im1pbmlvIiwic2Vzc2lvbl9zdGF0ZSI6IjNkZDY0MDVlLTNkMGMtNDVjOS05MTZkLTllYTNkNWY1ODVkYiIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIiwidXNlciJdfSwic2NvcGUiOiJvcGVuaWQgbWluaW8tYXV0aG9yaXphdGlvbiIsInNpZCI6IjNkZDY0MDVlLTNkMGMtNDVjOS05MTZkLTllYTNkNWY1ODVkYiIsInBvbGljeSI6WyJyZWFkb25seSJdfQ.uKxLmSW6j2EQEo86j0WZOKWgavhS8Ub7TjrnynUi4m1ls0SchvgCilVpzIzNdFL9Y7khiqxl7si5BezbTLPgwyh4GDgrHcJwBk5D6aOcaH6hYcAtcbOiu1KEyfj1O_lwvDCHb-J07TIEeuvquOs2nD7FxqafHjLe-3pL6JuTtBtlx8WKloO9PY-Dn-ntuyqikr7ysLcDBfFJda487cmeTADxiMQ_MmoidW3uGXn0Ps6vhRgteUQO5JTKMa7MT1PKMTY8iNnSdNVuhKkBodnkXMSo5JEt4veqR9Yh-WPT_XL8caUiGInYvHty-n6-yhGhNckrlvtmJc0dJsts4hi1Mw",
                        "eyJhbGciOiJIUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICIxNzA0N2Q0NS0wMTVhLTQwYWItYjc5NS03Y2Y1ZDE2ZmFhMmQifQ.eyJleHAiOjE2OTE5OTk1MDUsImlhdCI6MTY5MTk5NzcwNSwianRpIjoiY2U4OGVlMjMtOTQ1Yi00YzlmLWExMjAtZjU2ODk0NzIwZDk0IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL3JlYWxtcy9jeWJlcmR1Y2tyZWFsbSIsImF1ZCI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9yZWFsbXMvY3liZXJkdWNrcmVhbG0iLCJzdWIiOiIzMzRkYmViMC0xOTVkLTQyYTEtYTFkNi0xMjgxZjAwYmZiMWQiLCJ0eXAiOiJSZWZyZXNoIiwiYXpwIjoibWluaW8iLCJzZXNzaW9uX3N0YXRlIjoiM2RkNjQwNWUtM2QwYy00NWM5LTkxNmQtOWVhM2Q1ZjU4NWRiIiwic2NvcGUiOiJvcGVuaWQgbWluaW8tYXV0aG9yaXphdGlvbiIsInNpZCI6IjNkZDY0MDVlLTNkMGMtNDVjOS05MTZkLTllYTNkNWY1ODVkYiJ9.iRFLFjU-Uyv81flgieBht2K2BSlM-67fe5unvqI9PXA",
                        Long.MAX_VALUE,
                        "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJDQmpaYUNSeU5USmZqV0VmMU1fZXZLRVliMEdGLXU0QzhjZ3RZYnBtZUlFIn0.eyJleHAiOjE2OTE5OTc3MzUsImlhdCI6MTY5MTk5NzcwNSwiYXV0aF90aW1lIjowLCJqdGkiOiJlYWZiNWE5NS1lYmY3LTQ0OTEtODAwYy0yZjU1NTk2MjQ0YzIiLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvcmVhbG1zL2N5YmVyZHVja3JlYWxtIiwiYXVkIjoibWluaW8iLCJzdWIiOiIzMzRkYmViMC0xOTVkLTQyYTEtYTFkNi0xMjgxZjAwYmZiMWQiLCJ0eXAiOiJJRCIsImF6cCI6Im1pbmlvIiwic2Vzc2lvbl9zdGF0ZSI6IjNkZDY0MDVlLTNkMGMtNDVjOS05MTZkLTllYTNkNWY1ODVkYiIsImF0X2hhc2giOiJWX1lIZTVpc0UzY0IyOGF4cXQzRGpnIiwic2lkIjoiM2RkNjQwNWUtM2QwYy00NWM5LTkxNmQtOWVhM2Q1ZjU4NWRiIiwicG9saWN5IjpbInJlYWRvbmx5Il19.bXjcBJY7H79O9rtYr3b_EpKuclaRRsWGIVm5SEesqMM3aIkGq6ikWNmoL4Ffy48Frx1E3UnvG5PQfd8C2-XgNg_9EnWyR1MkgxJ67xQOAT10E77wZ0YbFWYIcdOojR98rmh4_TGVeTaGwDMMQZzRMr0nQwfZP3TQ8ciRhor8svnkFkk3FBzT1rSJA0bJv181HyerQl0f_TnTEnr3UjmmFmDrNASxHoXbwqiE4L-qZBnNiz97jLxGULfyVn4CZUub53x0ka0KGnLeicFHDh1asiHMW18o9-BUh8cGp-Ywm7Xu_f_c8XokNjG8ls56Xp7g8rQ4-d3J0F0-TAgnn7xO1g"))
                .withTokens(TemporaryAccessTokens.EMPTY);
        final Host host = new Host(profile, profile.getDefaultHostname(), credentials);
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        assertNotEquals(OAuthTokens.EMPTY, credentials.getOauth());
        assertNotEquals(TemporaryAccessTokens.EMPTY, credentials.getTokens());
    }

    /**
     * Test flow in <code>S3AuthenticationResponseInterceptor</code>> with no OAuth tokens found.
     * Fetch OpenID Connect Id token and swap for temporary access credentials assuming role
     */
    @Test
    public void testBucketListInvalidOAuthTokensList() throws Exception {
        final Protocol profile = new ProfilePlistReader(new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())))).read(
                AbstractAssumeRoleWithWebIdentityTest.class.getResourceAsStream("/S3 (OIDC).cyberduckprofile"));
        final Credentials credentials = new Credentials("rouser", "rouser")
                .withOauth(OAuthTokens.EMPTY)
                .withTokens(new TemporaryAccessTokens(
                        "5K1AVE34L4U1SQ7QTMWM",
                        "LfkexzCDPojZpdIoNLNvHxrUi1KI5yP3Yken+DGI",
                        "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NLZXkiOiI1SzFBVkUzNEw0VTFTUTdRVE1XTSIsImF0X2hhc2giOiJWX1lIZTVpc0UzY0IyOGF4cXQzRGpnIiwiYXVkIjoibWluaW8iLCJhdXRoX3RpbWUiOjAsImF6cCI6Im1pbmlvIiwiZXhwIjoxNjkxOTk3NzM1LCJpYXQiOjE2OTE5OTc3MDUsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODA4MC9yZWFsbXMvY3liZXJkdWNrcmVhbG0iLCJqdGkiOiJlYWZiNWE5NS1lYmY3LTQ0OTEtODAwYy0yZjU1NTk2MjQ0YzIiLCJwb2xpY3kiOiJyZWFkb25seSIsInNlc3Npb25fc3RhdGUiOiIzZGQ2NDA1ZS0zZDBjLTQ1YzktOTE2ZC05ZWEzZDVmNTg1ZGIiLCJzaWQiOiIzZGQ2NDA1ZS0zZDBjLTQ1YzktOTE2ZC05ZWEzZDVmNTg1ZGIiLCJzdWIiOiIzMzRkYmViMC0xOTVkLTQyYTEtYTFkNi0xMjgxZjAwYmZiMWQiLCJ0eXAiOiJJRCJ9.HmyC7XuJw9XnsNUd2ZuGSVIPjnGHPpgbXX1HSbNJuhis1kUjhcrYY2HnQZ-uScoX57o_C3fF1eEv_t1kW2U6Rw",
                        -1L
                ));
        final Host host = new Host(profile, profile.getDefaultHostname(), credentials);
        final S3Session session = new S3Session(host);
        assertNotNull(session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.getClient().setProviderCredentials(new AWSSessionCredentials(
                credentials.getTokens().getAccessKeyId(), credentials.getTokens().getSecretAccessKey(),
                credentials.getTokens().getSessionToken()));
        new S3BucketListService(session).list(
                new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledListProgressListener());
        assertNotEquals(OAuthTokens.EMPTY, credentials.getOauth());
        assertNotEquals(TemporaryAccessTokens.EMPTY, credentials.getTokens());
    }
}