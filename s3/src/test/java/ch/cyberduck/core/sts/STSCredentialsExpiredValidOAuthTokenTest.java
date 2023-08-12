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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.OAuthTokens;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.STSTokens;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.testcontainers.containers.DockerComposeContainer;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import static ch.cyberduck.core.sts.AbstractAssumeRoleWithWebIdentityTest.LAG;
import static ch.cyberduck.core.sts.AbstractAssumeRoleWithWebIdentityTest.MILLIS;
import static ch.cyberduck.core.sts.STSTestSetup.*;
import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class STSCredentialsExpiredValidOAuthTokenTest {

    /**
     * Adjust OAuth token TTL in Keycloak:
     * "access.token.lifespan": "930"
     * "ssoSessionMaxLifespan": 1100,
     */
    private static Map<String, String> overrideKeycloakDefaults() {
        Map<String, String> m = new HashMap<>();
        m.put("access.token.lifespan", Integer.toString(930));
        m.put("ssoSessionMaxLifespan", Integer.toString(1100));
        return m;
    }

    @ClassRule
    public static DockerComposeContainer<?> compose = prepareDockerComposeContainer(
            getKeyCloakFile(overrideKeycloakDefaults())
    );

    @Test
    @Ignore("Takes 15 minutes, skip by default")
    public void testSTSCredentialsExpiredValidOAuthToken() throws BackgroundException, InterruptedException {
        final Profile profile = readProfile();
        // 900 secs = 15 min is mininmum value: https://min.io/docs/minio/linux/developers/security-token-service/AssumeRoleWithWebIdentity.html
        final int assumeRoleDurationSeconds = 900;
        final Host host = new Host(profile, profile.getDefaultHostname(), new Credentials("rawuser", "rawuser"));
        host.setProperty("s3.assumerole.durationseconds", Integer.toString(assumeRoleDurationSeconds));

        assertEquals(new HostPreferences(host).getInteger("s3.assumerole.durationseconds"), assumeRoleDurationSeconds);
        final S3Session session = new S3Session(host);
        session.open(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledProxyFinder().find(new HostUrlProvider().get(host)), new DisabledLoginCallback(), new DisabledCancelCallback());

        final OAuthTokens oauth = host.getCredentials().getOauth();
        assertTrue(oauth.validate());
        final STSTokens tokens = host.getCredentials().getTokens();
        assertTrue(tokens.validate());

        Path container = new Path("cyberduckbucket", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));

        Thread.sleep(MILLIS * assumeRoleDurationSeconds + LAG);

        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(container));
        assertEquals(oauth, host.getCredentials().getOauth());
        assertNotEquals(tokens, host.getCredentials().getTokens());
    }
}