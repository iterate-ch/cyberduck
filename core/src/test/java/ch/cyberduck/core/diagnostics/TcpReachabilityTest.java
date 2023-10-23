package ch.cyberduck.core.diagnostics;

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
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TcpReachabilityTest {

    @Test
    public void testIsReachable() {
        final Reachability r = new TcpReachability();
        assertTrue(r.isReachable(
                new Host(new TestProtocol(Scheme.http), "cloud.iterate.ch")
        ));
        assertTrue(r.isReachable(
                new Host(new TestProtocol(Scheme.https), "cloud.iterate.ch")
        ));
        assertTrue(r.isReachable(
                new Host(new TestProtocol(Scheme.sftp), "cloud.iterate.ch")
        ));
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.ftp), "cloud.iterate.ch")
        ));
    }

    @Test
    public void testNotReachableSubdomain() {
        final Reachability r = new TcpReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.http), "a.cyberduck.ch")
        ));
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.https), "a.cyberduck.ch")
        ));
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.ftp), "a.cyberduck.ch")
        ));
        assertFalse(r.isReachable(
                new Host(new TestProtocol(Scheme.sftp), "a.cyberduck.ch")
        ));
    }

    @Test
    public void testNotReachableWrongHostname() {
        final Reachability r = new TcpReachability();
        assertFalse(r.isReachable(
                new Host(new TestProtocol(), "cyberduck.ch.f")
        ));
    }
}