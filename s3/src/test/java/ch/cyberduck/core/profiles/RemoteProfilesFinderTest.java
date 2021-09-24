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
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
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
        // Check for versions of S3 (HTTP).cyberduckprofile
        assertFalse(stream.stream().filter(ProfileDescription::isLatest).collect(Collectors.toSet()).isEmpty());
        assertFalse(stream.stream().filter(description -> !description.isLatest()).collect(Collectors.toSet()).isEmpty());
        assertTrue(stream.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("b9afd8d6da91e7b520559fa9eaac54c1"))));
        assertTrue(stream.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("19ecbfe2d8f09644197c1ef53e207792"))));
        session.close();
    }
}
