package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.FallbackAttributesFinderFeature;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.dav.DAVAttributesFinderFeature;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dav.DAVSession;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.proxy.DisabledProxyFinder;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class FallbackAttributesFinderFeatureTest {

    @Test
    public void testFindNoWebDAV() throws Exception {
        final DAVSession session = new DAVSession(new Host(new DAVSSLProtocol(), "ftp.gnu.org"), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        session.open(new DisabledProxyFinder(), new DisabledHostKeyCallback(), new DisabledLoginCallback(), new DisabledCancelCallback());
        session.login(new DisabledLoginCallback(), new DisabledCancelCallback());
        // Handle 405 Method Not Allowed
        final AttributesFinder f = new FallbackAttributesFinderFeature(new DefaultAttributesFinderFeature(session), new DAVAttributesFinderFeature(session));
        final PathAttributes attr = f.find(new Path("/gnu/wget/wget-1.19.4.tar.gz", EnumSet.of(Path.Type.file)));
        assertNotNull(attr);
        assertNotEquals(PathAttributes.EMPTY, attr);
        assertEquals(4310657L, attr.getSize());
    }
}
