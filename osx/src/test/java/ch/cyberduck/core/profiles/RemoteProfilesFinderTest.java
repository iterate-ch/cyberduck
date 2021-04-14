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
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;

public class RemoteProfilesFinderTest {

    @Test
    public void find() throws Exception {
        final ProtocolFactory protocols = new ProtocolFactory(Collections.singleton(new DAVSSLProtocol() {
            @Override
            public boolean isEnabled() {
                return true;
            }
        }));
        final DAVSession session = new DAVSession(new HostParser(protocols).get("https://svn.cyberduck.io/trunk/profiles"), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.connect(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final RemoteProfilesFinder finder = new RemoteProfilesFinder(new ProfilePlistReader(protocols), session);
        final Stream<ProfilesFinder.ProfileDescription> stream = finder.find();
        assertFalse(stream.collect(Collectors.toList()).isEmpty());
        session.close();
    }
}
