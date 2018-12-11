package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class SharepointTest {
    @Test
    public void test() throws Exception {
        PreferencesFactory.get().setProperty("webdav.ntlm.domain", System.getProperties().getProperty("webdav.sharepoint.domain"));
        final Host host = new Host(new DAVProtocol(), "51.144.72.59", new Credentials(
            System.getProperties().getProperty("webdav.sharepoint.user"), System.getProperties().getProperty("webdav.sharepoint.password")
        ));
        final DAVSession session = new DAVSession(host);
        session.open(Proxy.DIRECT, new DisabledHostKeyCallback(), new DisabledLoginCallback());
        session.login(Proxy.DIRECT, new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
    }
}
