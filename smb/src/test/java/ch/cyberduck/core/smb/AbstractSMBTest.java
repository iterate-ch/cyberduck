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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.proxy.Proxy;

import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;

public abstract class AbstractSMBTest {

    @ClassRule
    public static SmbTestContainer container = SmbTestContainer.getInstance();

    SMBSession session;

    @Before
    public void setup() throws BackgroundException {
        container.stop();
        container.start();
        final Host host = new Host(new SMBProtocol(), container.getHost(), container.getMappedPort(445));
        host.setCredentials(new Credentials("smbj/user", "pass"));
        session = new SMBSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new DisabledCancelCallback());
    }

    @After
    public void disconnect() throws Exception {
        session.close();
    }
}
