package ch.cyberduck.core.ctera;

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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.dav.DAVListService;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.threading.CancelCallback;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CteraSessionTest extends AbstractCteraTest {

    @Test
    public void testLoginNonSAML() throws Exception {
        new CteraListService(session).list(new Path(session.getHost().getDefaultPath(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
    }

    @Test
    public void testLoginRefreshCookie() throws Exception {
        final Host host = new Host(new CteraProtocol(), "mountainduck.ctera.me", new Credentials(
                StringUtils.EMPTY, StringUtils.EMPTY,
                PROPERTIES.get("ctera.token")
        ));
        host.setDefaultPath("/ServicesPortal/webdav");
        final CteraSession session = new CteraSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(Proxy.DIRECT, new DisabledLoginCallback(), new CancelCallback() {
            @Override
            public void verify() throws ConnectionCanceledException {
                fail("OAuth tokens need to be refreshed");
            }
        });
        assertEquals("mountainduck@cterasendbox1.onmicrosoft.com", host.getCredentials().getUsername());
        assertTrue(host.getCredentials().isSaved());
        new DAVListService(session).list(new Path(host.getDefaultPath(), EnumSet.of(Path.Type.directory)), new DisabledListProgressListener());
        session.close();
    }
}
