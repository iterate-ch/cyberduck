package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static ch.cyberduck.core.deepbox.DeepboxIdProvider.FILES_ID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class DeepboxIdProviderTest extends AbstractDeepboxTest {
    @Test
    public void testRoot() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertNull(nodeid.getFileId(directory));
        assertNull(nodeid.getDeepBoxNodeId(directory));
        assertNull(nodeid.getBoxNodeId(directory));
        assertNull(nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testDeepBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/Mountainduck Buddies/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("8e0b546e-fe1b-47ba-b82d-c11682b9360b", nodeid.getFileId(directory));
        assertEquals("8e0b546e-fe1b-47ba-b82d-c11682b9360b", nodeid.getDeepBoxNodeId(deepBox));
        assertNull(nodeid.getBoxNodeId(directory));
        assertNull(nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/Mountainduck Buddies/My Box/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("5402aff1-0f0b-416b-a225-4f55d3812059", nodeid.getFileId(directory));
        assertEquals("8e0b546e-fe1b-47ba-b82d-c11682b9360b", nodeid.getDeepBoxNodeId(directory));
        assertEquals("5402aff1-0f0b-416b-a225-4f55d3812059", nodeid.getBoxNodeId(directory));
        assertNull(nodeid.getThirdLevelId(box));
    }

    @Test
    public void testDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/Mountainduck Buddies/My Box/Documents/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals(FILES_ID, nodeid.getFileId(directory));
        assertEquals("8e0b546e-fe1b-47ba-b82d-c11682b9360b", nodeid.getDeepBoxNodeId(directory));
        assertEquals("5402aff1-0f0b-416b-a225-4f55d3812059", nodeid.getBoxNodeId(directory));
        assertEquals(FILES_ID, nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testAuditing() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/Mountainduck Buddies/My Box/Documents/Auditing/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("423ab938-57b0-4b28-ad84-d27dd41aa7c4", nodeid.getFileId(directory));
        assertEquals("8e0b546e-fe1b-47ba-b82d-c11682b9360b", nodeid.getDeepBoxNodeId(directory));
        assertEquals("5402aff1-0f0b-416b-a225-4f55d3812059", nodeid.getBoxNodeId(directory));
        assertEquals(FILES_ID, nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path("/Mountainduck Buddies/My Box/Documents/Auditing/nix4.txt", EnumSet.of(Path.Type.file));
        assertEquals("3faf6efe-1ecc-47f9-9a6c-5efb9216b3dd", nodeid.getFileId(file));
        assertEquals("8e0b546e-fe1b-47ba-b82d-c11682b9360b", nodeid.getDeepBoxNodeId(file));
        assertEquals("5402aff1-0f0b-416b-a225-4f55d3812059", nodeid.getBoxNodeId(file));
        assertEquals(FILES_ID, nodeid.getThirdLevelId(file));
    }
}