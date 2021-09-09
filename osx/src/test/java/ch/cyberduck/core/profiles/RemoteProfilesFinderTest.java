package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RemoteProfilesFinderTest {

    @Test
    public void testFind() throws Exception {
        final ProtocolFactory protocols = new ProtocolFactory(Collections.singleton(new S3Protocol() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        }));
        final Session session = new S3Session(new HostParser(protocols).get("s3:/profiles.cyberduck.io"), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final RemoteProfilesFinder finder = new RemoteProfilesFinder(session);
        final Set<ProfileDescription> stream = finder.find();
        assertFalse(stream.isEmpty());
        // Check for versions
        assertFalse(stream.stream().filter(ProfileDescription::isLatest).collect(Collectors.toSet()).isEmpty());
        assertFalse(stream.stream().filter(description -> !description.isLatest()).collect(Collectors.toSet()).isEmpty());
        assertTrue(stream.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("30298e0b4a1bd3ce954289281347c6ad"))));
        assertTrue(stream.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("ed214eca3b4521a6d567326b2e6e24e4"))));
        assertTrue(stream.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("283e922b59c8e716608763364dc63fb4"))));
        session.close();
    }
}
