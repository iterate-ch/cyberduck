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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.HostKeyCallback;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class RemoteIndexProfilesFinderTest {

    @Test
    public void testFind() throws Exception {
        final S3Protocol protocol = new S3Protocol() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        };
        final ProtocolFactory protocols = new ProtocolFactory(Collections.singleton(protocol));
        final Session<?> session = new S3Session(new HostParser(protocols).get("s3:/profiles.cyberduck.io")
                .setCredentials(new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name"))), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledProxyFinder(), HostKeyCallback.noop, LoginCallback.noop, CancelCallback.noop);
        final RemoteIndexProfilesFinder finder = new RemoteIndexProfilesFinder(protocols, session);
        final Set<ProfileDescription> set = finder.find();
        assertFalse(set.isEmpty());
        // Check for versions of S3 (HTTP).cyberduckprofile
        assertFalse(set.stream().filter(ProfileDescription::isLatest).collect(Collectors.toSet()).isEmpty());
        assertFalse(set.stream().filter(description -> !description.isLatest()).collect(Collectors.toSet()).isEmpty());
        assertTrue(set.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("b9afd8d6da91e7b520559fa9eaac54c1"))));
        assertTrue(set.stream().anyMatch(description -> description.getChecksum().equals(Checksum.parse("19ecbfe2d8f09644197c1ef53e207792"))));
        set.forEach(d -> {
            if(protocol.getIdentifier().equals(d.getIdentifier())) {
                assertNotNull(d.getName());
            }
            else {
                assertNull(d.getName());
            }
            assertTrue(new SearchProfilePredicate(StringUtils.EMPTY).test(d));
        });
        session.close();
    }
}
