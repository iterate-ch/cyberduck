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
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeepboxIdProviderTest extends AbstractDeepboxTest {

    @Test
    public void testRoot() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertNull(nodeid.getFileId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getDeepBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testCompany() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals(ORG4, nodeid.getFileId(directory));
        assertEquals(ORG4, nodeid.getCompanyNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getDeepBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testDeepBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals(ORG4_DEEPBOX4, nodeid.getFileId(directory));
        assertEquals(ORG4, nodeid.getCompanyNodeId(directory));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testSharedWithMe() {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path(String.format("/ORG 1 - DeepBox Desktop App/%s", DeepboxListService.SHARED), EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertThrows(NotfoundException.class, () -> nodeid.getFileId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getDeepBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testBox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getFileId(directory));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(directory));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testSharedWithMe_Box() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path(String.format("/ORG 1 - DeepBox Desktop App/%s/Demo 1 (1 Christian Gruber)", DeepboxListService.SHARED), EnumSet.of(Path.Type.directory, Path.Type.volume, AbstractPath.Type.shared));
        assertEquals(SHARED_DEEPBOX_BOX, nodeid.getFileId(directory));
        assertEquals(SHARED_DEEPBOX, nodeid.getDeepBoxNodeId(directory));
        assertEquals(SHARED_DEEPBOX_BOX, nodeid.getBoxNodeId(directory));
        assertThrows(NotfoundException.class, () -> nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testInbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals("dc37e9db-36e9-4330-881c-730789aaa8ce", nodeid.getFileId(directory));
        assertEquals(ORG4, nodeid.getCompanyNodeId(directory));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(directory));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getBoxNodeId(directory));
        assertEquals("dc37e9db-36e9-4330-881c-730789aaa8ce", nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testSharedWithMe_Inbox() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path(String.format("/ORG 1 - DeepBox Desktop App/%s/Demo 1 (1 Christian Gruber)/Inbox", DeepboxListService.SHARED), EnumSet.of(Path.Type.directory, Path.Type.volume, AbstractPath.Type.shared));
        assertEquals("b13b6754-2b9a-4867-888c-cbd72fe353c3", nodeid.getFileId(directory));
        assertEquals(ORG1, nodeid.getCompanyNodeId(directory));
        assertEquals(SHARED_DEEPBOX, nodeid.getDeepBoxNodeId(directory));
        assertEquals(SHARED_DEEPBOX_BOX, nodeid.getBoxNodeId(directory));
        assertEquals("b13b6754-2b9a-4867-888c-cbd72fe353c3", nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testTrash() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Trash/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals("1fc77175-f2a7-4b65-bd38-9aaeb9272a90", nodeid.getFileId(directory));
        assertEquals(ORG4, nodeid.getCompanyNodeId(directory));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(directory));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getBoxNodeId(directory));
        assertEquals("1fc77175-f2a7-4b65-bd38-9aaeb9272a90", nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testDocuments() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getFileId(directory));
        assertEquals(ORG4, nodeid.getCompanyNodeId(directory));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(directory));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getBoxNodeId(directory));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testAuditing() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertEquals("e9ad9677-0883-4751-817f-8407a590dc9d", nodeid.getFileId(directory));
        assertEquals(ORG4, nodeid.getCompanyNodeId(directory));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(directory));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getBoxNodeId(directory));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getFourthLevelId(directory));
    }

    @Test
    public void testFile() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path("/ORG 4 - DeepBox Desktop App/ORG 4 - DeepBox Desktop App/ORG3:Box1/Documents/Invoices : Receipts/RE-IN - Copy1.pdf", EnumSet.of(Path.Type.file));
        assertEquals("c38d7abf-4d37-4303-8061-d76efd4e77b0", nodeid.getFileId(file));
        assertEquals(ORG4, nodeid.getCompanyNodeId(file));
        assertEquals(ORG4_DEEPBOX4, nodeid.getDeepBoxNodeId(file));
        assertEquals(ORG4_DEEPBOX4_BOX1, nodeid.getBoxNodeId(file));
        assertEquals("ec5f9666-f99e-47ad-bc8c-41da9f1324e2", nodeid.getFourthLevelId(file));
    }

    @Test
    public void testSharedWithMe_File() throws Exception {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path file = new Path(String.format("/ORG 1 - DeepBox Desktop App/%s/Demo 1 (1 Christian Gruber)/Documents/Bookkeeping/screenshot.png", DeepboxListService.SHARED), EnumSet.of(Path.Type.directory, Path.Type.volume, AbstractPath.Type.shared));
        assertEquals("0fb9536b-391c-4d07-bcff-0d6d0e7cd2d7", nodeid.getFileId(file));
        assertEquals(ORG1, nodeid.getCompanyNodeId(file));
        assertEquals(SHARED_DEEPBOX, nodeid.getDeepBoxNodeId(file));
        assertEquals(SHARED_DEEPBOX_BOX, nodeid.getBoxNodeId(file));
        assertEquals("8e40548c-6367-4e19-9ee3-2590a89c8f1a", nodeid.getFourthLevelId(file));
    }

    @Test
    public void testNormalizeInboxInSharedWithMe() {
        final DeepboxIdProvider nodeid = new DeepboxIdProvider(session);
        final Path directory = new Path(String.format("/ORG 1 - DeepBox Desktop App/%s/Demo 1 (1 Christian Gruber)/Inbox/", DeepboxListService.SHARED), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path normalized = new Path("/ORG 1 - DeepBox Desktop App/Testing/1 Christian Gruber/Inbox/", EnumSet.of(Path.Type.directory, Path.Type.volume, AbstractPath.Type.shared));
        assertEquals(new SimplePathPredicate(normalized), new SimplePathPredicate(nodeid.normalize(directory)));
        Path p = nodeid.normalize(directory);
        final DeepboxPathContainerService container = new DeepboxPathContainerService(session, nodeid);
        while(!p.isRoot()) {
            if(container.isCompany(p)) {
                assertFalse(p.getType().contains(AbstractPath.Type.shared));
            }
            else {
                assertTrue(p.getType().contains(AbstractPath.Type.shared));
            }
            p = p.getParent();
        }
    }
}