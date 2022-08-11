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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.proxy.Proxy;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class RemoteProfilesFinderTest {

    @Test
    public void find() throws Exception {
        final ProtocolFactory protocols = new ProtocolFactory(new HashSet<>(Arrays.asList(new TestProtocol() {
            @Override
            public String getIdentifier() {
                return "s3";
            }

            @Override
            public Type getType() {
                return Type.s3;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        }, new TestProtocol() {
            @Override
            public String getIdentifier() {
                return "davs";
            }

            @Override
            public Type getType() {
                return Type.dav;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        })));
        final TestProtocol protocol = new TestProtocol() {
            @Override
            public String getIdentifier() {
                return "davs";
            }

            @Override
            public Scheme getScheme() {
                return Scheme.https;
            }

            @Override
            public Type getType() {
                return Type.dav;
            }

            @Override
            public boolean isEnabled() {
                return false;
            }
        };
        final Host host = new HostParser(protocols, protocol).get("https://svn.cyberduck.io/trunk/profiles");
        final NullSession session = new NullSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final RemoteProfilesFinder finder = new RemoteProfilesFinder(session);
        final Set<ProfileDescription> stream = finder.find();
        assertTrue(stream.isEmpty());
        session.close();
    }
}
