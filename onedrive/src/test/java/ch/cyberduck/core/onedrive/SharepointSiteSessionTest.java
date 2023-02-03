package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SharepointSiteSessionTest {
    private SharepointSiteSession session;

    @Before
    public void setup() {
        session = new SharepointSiteSession(new Host(new SharepointSiteProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
    }

    @Test
    public void testHome() {
        Assert.assertFalse(session.isHome(SharepointListService.DEFAULT_NAME));
        Assert.assertTrue(session.isHome(Home.ROOT));
    }
}
