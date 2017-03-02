package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveSessionTest extends AbstractOneDriveTest {

    @Test
    public void testFeatures() throws Exception {
        final OneDriveSession session = new OneDriveSession(new Host(new OneDriveProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.getFeature(Read.class));
        assertNotNull(session.getFeature(Write.class));
        assertNotNull(session.getFeature(Directory.class));
        assertNotNull(session.getFeature(Touch.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(UrlProvider.class));
        assertNotNull(session.getFeature(AttributesFinder.class));
    }

    @Test
    public void testConnect() throws Exception {
        assertTrue(session.isConnected());
        session.close();
        assertFalse(session.isConnected());
    }

}
