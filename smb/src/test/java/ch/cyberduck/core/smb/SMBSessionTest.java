package ch.cyberduck.core.smb;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.cdn.DistributionConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionRefusedException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBSessionTest extends AbstractSMBTest {

    @Test
    public void testLoginSuccessWithoutDomain() throws Exception {

        final Host host = new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445));
        host.setCredentials(new Credentials("smbj/user", "pass"));
        final SMBSession session = new SMBSession(host);

        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test
    public void testLoginSuccessWithDomain() throws Exception {

        final Host host = new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445));
        host.setCredentials(new Credentials("smbj@WORKGROUP/user", "pass"));
        final SMBSession session = new SMBSession(host);

        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = BackgroundException.class)
    public void testLoginFailMissingSharename() throws Exception {

        final Host host = new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445));
        host.setCredentials(new Credentials("smbj@WORKGROUP", "pass"));
        final SMBSession session = new SMBSession(host);

        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = BackgroundException.class)
    public void testLoginFailMissingUsername() throws Exception {

        final Host host = new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445));
        host.setCredentials(new Credentials("/user", "pass"));
        final SMBSession session = new SMBSession(host);

        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @Test(expected = ConnectionRefusedException.class)
    public void testConnectRefused() throws Exception {
        final Host host = new Host(new SMBProtocol(), container.getHost(), 135);
        host.setCredentials(new Credentials("smbj/user", "pass"));
        final SMBSession session = new SMBSession(host);

        try {
            session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
            session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
        }
        catch(ConnectionRefusedException e) {
            assertEquals("Connection failed", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testFeatures() {
        assertNull(session.getFeature(UnixPermission.class));
        assertNull(session.getFeature(DistributionConfiguration.class));
        assertNotNull(session.getFeature(Touch.class));
    }
}
