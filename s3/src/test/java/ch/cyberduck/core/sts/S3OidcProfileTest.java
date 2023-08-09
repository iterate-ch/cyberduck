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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

public class S3OidcProfileTest {

    @Test
    public void testDefaultProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/S3-OIDC-Testing.cyberduckprofile"));
        assertEquals("minio", profile.getOAuthClientId());
        assertEquals("password", profile.getOAuthClientSecret());
        assertNotNull(profile.getOAuthAuthorizationUrl());
        assertEquals("http://localhost:8080/realms/cyberduckrealm/protocol/openid-connect/token", profile.getOAuthTokenUrl());
        assertEquals("http://localhost:9000", profile.getSTSEndpoint());
        assertFalse(profile.getOAuthScopes().isEmpty());
        assertTrue(profile.getOAuthScopes().contains("openid"));
        assertEquals("", new HostPreferences(new Host(profile)).getProperty("s3.assumerole.rolearn"));
    }
}
