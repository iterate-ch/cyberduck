package ch.cyberduck.core.profiles;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProfilesWorkerBackgroundActionTest {

    @Test
    public void testParseWithoutRegisteredProtocol() throws Exception {
        final Host host = ProfilesWorkerBackgroundAction.toHost("s3://profiles.cyberduck.io");
        assertEquals(S3Protocol.class, host.getProtocol().getClass());
        assertEquals(Protocol.Type.s3, host.getProtocol().getType());
        assertEquals("profiles.cyberduck.io", host.getHostname());
        assertEquals(PreferencesFactory.get().getProperty("connection.login.anon.name"), host.getCredentials().getUsername());
    }

    @Test
    public void testParseFallbackScheme() throws Exception {
        final Host host = ProfilesWorkerBackgroundAction.toHost("https://profiles.cyberduck.io");
        assertEquals(Scheme.https, host.getProtocol().getScheme());
        assertEquals("profiles.cyberduck.io", host.getHostname());
    }

    @Test(expected = HostParserException.class)
    public void testParseUnknownScheme() throws Exception {
        ProfilesWorkerBackgroundAction.toHost("unknown://profiles.cyberduck.io");
    }
}
