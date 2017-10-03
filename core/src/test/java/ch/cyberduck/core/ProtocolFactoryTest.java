package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import static org.junit.Assert.*;

public class ProtocolFactoryTest {

    @Test
    public void testGetProtocols() throws Exception {
        final TestProtocol defaultProtocol = new TestProtocol(Scheme.ftp);
        final TestProtocol providerProtocol = new TestProtocol(Scheme.ftp) {
            @Override
            public String getProvider() {
                return "c";
            }
        };
        final TestProtocol disabledProtocol = new TestProtocol(Scheme.sftp) {
            @Override
            public boolean isEnabled() {
                return false;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new HashSet<>(
                Arrays.asList(defaultProtocol, providerProtocol, disabledProtocol)));
        final List<Protocol> protocols = f.find();
        assertTrue(protocols.contains(defaultProtocol));
        assertTrue(protocols.contains(providerProtocol));
        assertFalse(protocols.contains(disabledProtocol));
    }

    @Test
    public void testFindUnknownDefaultProtocol() throws Exception {
        final TestProtocol dav = new TestProtocol(Scheme.dav);
        final TestProtocol davs = new TestProtocol(Scheme.davs);
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(dav, davs)));
        assertEquals(dav, f.forName("dav"));
        assertEquals(dav, f.forScheme(Scheme.http));
        assertEquals(davs, f.forName("davs"));
        assertEquals(davs, f.forScheme(Scheme.https));
        assertNull(f.forName("ftp"));
    }

    @Test
    public void testFindProtocolWithProviderInIdentifier() throws Exception {
        final TestProtocol dav = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "dav";
            }

            @Override
            public String getProvider() {
                return "provider";
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Collections.singletonList(dav)));
        assertEquals(dav, f.forName("dav"));
        assertEquals(dav, f.forName("dav-provider"));
    }

    @Test
    public void testFindProtocolProviderMismatch() throws Exception {
        final TestProtocol dav_provider1 = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "dav";
            }

            @Override
            public String getProvider() {
                return "provider_1";
            }
        };
        final TestProtocol dav_provider2 = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "dav";
            }

            @Override
            public String getProvider() {
                return "provider_2";
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(dav_provider1, dav_provider2)));
        assertEquals(dav_provider1, f.forName("dav"));
        assertEquals(dav_provider1, f.forName("dav", "provider_1"));
        assertEquals(dav_provider1, f.forName("dav", "g"));
        assertEquals(dav_provider2, f.forName("dav", "provider_2"));
    }

    @Test
    public void testSchemeFallbackType() throws Exception {
        final TestProtocol dav = new TestProtocol(Scheme.dav);
        final TestProtocol swift = new TestProtocol(Scheme.dav) {
            @Override
            public String getIdentifier() {
                return "swift-p";
            }

            @Override
            public Type getType() {
                return Type.swift;
            }
        };
        final ProtocolFactory f = new ProtocolFactory(new LinkedHashSet<>(Arrays.asList(dav, swift)));
        assertEquals(swift, f.forName("swift"));
    }
}
