package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class StoregateSessionTest extends AbstractStoregateTest {

    @Test
    public void testFeatures() {
        final Host host = new Host(new StoregateProtocol(), "ws1-stage.storegate.se");
        final Session session = new StoregateSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.getFeature(ListService.class));
    }

    @Test
    public void testConnect() throws Exception {
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

}
