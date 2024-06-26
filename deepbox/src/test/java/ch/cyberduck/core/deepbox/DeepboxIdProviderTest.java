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

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class DeepboxIdProviderTest extends AbstractDeepboxTest {

    @Before
    public void setup() throws Exception {
        setup("deepbox.deepboxapp3.user");
    }
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
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getFileId(directory));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(directory));
        assertNull(nodeid.getBoxNodeId(directory));
        assertNull(nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/Box1/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getFileId(directory));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(directory));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getBoxNodeId(directory));
        assertNull(nodeid.getThirdLevelId(box));
    }

    @Test
    public void testInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/Box1/Inbox/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("dc37e9db-36e9-4330-881c-730789aaa8ce", nodeid.getFileId(directory));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(directory));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getBoxNodeId(directory));
        assertEquals("dc37e9db-36e9-4330-881c-730789aaa8ce", nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/Box1/Trash/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("1fc77175-f2a7-4b65-bd38-9aaeb9272a90", nodeid.getFileId(directory));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(directory));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getBoxNodeId(directory));
        assertEquals("1fc77175-f2a7-4b65-bd38-9aaeb9272a90", nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getFileId(directory));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(directory));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getBoxNodeId(directory));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testAuditing() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/ Receipts/", EnumSet.of(AbstractPath.Type.directory, Path.Type.volume));
        assertEquals("c215b052-3b46-4760-9cca-48eefb2a75f3", nodeid.getFileId(directory));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(directory));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getBoxNodeId(directory));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getThirdLevelId(directory));
    }

    @Test
    public void testFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path("/ORG 4 - DeepBox Desktop App/Box1/Documents/ Receipts/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.file));
        assertEquals("62a0d967-41b5-4cb4-bc06-3b7cac6d0c11", nodeid.getFileId(file));
        assertEquals("a548e68e-5584-42c1-b2bc-9e051dc78e5e", nodeid.getDeepBoxNodeId(file));
        assertEquals("366a7117-0ad3-4dcb-9e79-a4270c3f6fb5", nodeid.getBoxNodeId(file));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getThirdLevelId(file));
    }
}