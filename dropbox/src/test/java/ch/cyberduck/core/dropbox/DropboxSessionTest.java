package ch.cyberduck.core.dropbox;

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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import com.dropbox.core.v2.common.PathRoot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DropboxSessionTest {

    @Test
    public void testFeatures() {
        final Host host = new Host(new DropboxProtocol());
        final DropboxSession session = new DropboxSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertNotNull(session.getFeature(Directory.class));
        assertNotNull(session.getFeature(Delete.class));
        assertNotNull(session.getFeature(Touch.class));
    }

    @Test
    public void testRoot() {
        final Host host = new Host(new DropboxProtocol());
        final DropboxSession session = new DropboxSession(host, new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertEquals(PathRoot.namespaceId("r"), session.getRoot(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r"))));
        assertEquals(PathRoot.namespaceId("r"), session.getRoot(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")), "f", EnumSet.of(Path.Type.file))));
        assertEquals(PathRoot.namespaceId("r"), session.getRoot(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertEquals(PathRoot.namespaceId("r"), session.getRoot(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a"))));
        assertEquals(PathRoot.namespaceId("a"), session.getRoot(new Path(new Path("/Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a")), "d", EnumSet.of(Path.Type.directory))));
    }
}
