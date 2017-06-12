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
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProtocolFactoryTest {

    @Test
    public void getProtocols() throws Exception {
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
}