package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;

import org.junit.Test;

import java.util.EnumSet;

import com.dropbox.core.v2.common.PathRoot;

import static org.junit.Assert.assertEquals;

public class DropboxPathContainerServiceTest {

    @Test
    public void testGetKeyWithNamespace() {
        final DropboxPathContainerService s = new DropboxPathContainerService().withNamespace(true);
        assertEquals("ns:r", s.getKey(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r"))));
        assertEquals("ns:r/f", s.getKey(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")), "f", EnumSet.of(Path.Type.file))));
        assertEquals("ns:a", s.getKey(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a"))));
        assertEquals("ns:a/d", s.getKey(new Path(new Path("/Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a")), "d", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testGetKey() {
        final DropboxPathContainerService s = new DropboxPathContainerService();
        assertEquals("", s.getKey(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r"))));
        assertEquals("/f", s.getKey(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")), "f", EnumSet.of(Path.Type.file))));
        assertEquals("/Business", s.getKey(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertEquals("", s.getKey(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a"))));
        assertEquals("/d", s.getKey(new Path(new Path("/Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a")), "d", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testNamespace() {
        final DropboxPathContainerService s = new DropboxPathContainerService();
        final Host host = new Host(new DropboxProtocol());
        assertEquals(PathRoot.namespaceId("r"), s.getNamespace(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r"))));
        assertEquals(PathRoot.namespaceId("r"), s.getNamespace(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")), "f", EnumSet.of(Path.Type.file))));
        assertEquals(PathRoot.HOME, s.getNamespace(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume))));
        assertEquals(PathRoot.namespaceId("a"), s.getNamespace(new Path(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("r")),
            "Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a"))));
        assertEquals(PathRoot.namespaceId("a"), s.getNamespace(new Path(new Path("/Business", EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(new PathAttributes().withVersionId("a")), "d", EnumSet.of(Path.Type.directory))));
    }
}
