package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.VaultTest;

import org.junit.After;
import org.junit.Before;

public class AbstractCteraDirectIOTest extends VaultTest {

    protected CteraSession session;

    @After
    public void disconnect() throws Exception {
        session.close();
    }

    @Before
    public void setup() throws Exception {
        final Host host = new Host(new CteraProtocol(), "dcdirect.ctera.me", new Credentials(PROPERTIES.get("ctera.directio.user"))) {
            @Override
            public String getProperty(final String key) {
                if("ctera.download.directio.enable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        host.setDefaultPath("/ServicesPortal/webdav/My Files");
        session = new CteraSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager(), new TestPasswordStore());
        final LoginConnectionService connect = new LoginConnectionService(new DisabledLoginCallback(), new DisabledHostKeyCallback(),
                new TestPasswordStore(), new DisabledProgressListener(), new DisabledProxyFinder());
        connect.check(session, new DisabledCancelCallback());
    }
}
