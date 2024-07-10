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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;


public class DeepboxPathContainerServiceTest {

    @Test
    public void TestRoot() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = Home.ROOT;
        assertFalse(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertNull(container.getKey(folder));
        assertNull(container.getDeepboxPath(folder));
        assertNull(container.getBoxPath(folder));
        assertNull(container.getThirdLevelPath(folder));
    }

    @Test
    public void TestDeepBox() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = new Path("/Mountainduck Buddies", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertTrue(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertNull(container.getKey(folder));
        assertEquals(folder, container.getDeepboxPath(folder));
        assertNull(container.getBoxPath(folder));
        assertNull(container.getThirdLevelPath(folder));
    }

    @Test
    public void TestBox() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = new Path("/Mountainduck Buddies/My Box", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertTrue(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertNull(container.getKey(folder));
        assertEquals(folder.getParent(), container.getDeepboxPath(folder));
        assertEquals(folder, container.getBoxPath(folder));
        assertNull(container.getThirdLevelPath(folder));
    }

    @Test
    public void TestInbox() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s", DeepboxListService.INBOX), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertTrue(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertTrue(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertNull(container.getKey(folder));
        assertEquals(folder.getParent().getParent(), container.getDeepboxPath(folder));
        assertEquals(folder.getParent(), container.getBoxPath(folder));
        assertEquals(folder, container.getThirdLevelPath(folder));
    }

    @Test
    public void TestDocuments() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s", DeepboxListService.DOCUMENTS), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertTrue(container.isThirdLevel(folder));
        assertTrue(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertNull(container.getKey(folder));
        assertEquals(folder.getParent().getParent(), container.getDeepboxPath(folder));
        assertEquals(folder.getParent(), container.getBoxPath(folder));
        assertEquals(folder, container.getThirdLevelPath(folder));
    }

    @Test
    public void TestTrash() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s", DeepboxListService.TRASH), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertTrue(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertTrue(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertNull(container.getKey(folder));
        assertEquals(folder.getParent().getParent(), container.getDeepboxPath(folder));
        assertEquals(folder.getParent(), container.getBoxPath(folder));
        assertEquals(folder, container.getThirdLevelPath(folder));
    }

    @Test
    public void TestAuditing() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s/Auditing", DeepboxListService.DOCUMENTS), EnumSet.of(AbstractPath.Type.directory));
        assertFalse(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder.getParent(), container.getContainer(folder));
        assertEquals(Home.ROOT, container.getRoot(folder));
        assertEquals("Auditing", container.getKey(folder));
        assertEquals(folder.getParent().getParent().getParent(), container.getDeepboxPath(folder));
        assertEquals(folder.getParent().getParent(), container.getBoxPath(folder));
        assertEquals(folder.getParent(), container.getThirdLevelPath(folder));
    }

    @Test
    public void TestFile() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService(new DeepboxSession(new Host(new TestProtocol()), new DisabledX509TrustManager(), new DefaultX509KeyManager()));
        final Path file = new Path(String.format("/Mountainduck Buddies/My Box/%s/Auditing/nix4.txt", DeepboxListService.DOCUMENTS), EnumSet.of(AbstractPath.Type.file));
        assertFalse(container.isContainer(file));
        assertFalse(container.isDeepbox(file));
        assertFalse(container.isBox(file));
        assertFalse(container.isThirdLevel(file));
        assertFalse(container.isDocuments(file));
        assertFalse(container.isInbox(file));
        assertFalse(container.isTrash(file));
        assertEquals(file.getParent().getParent(), container.getContainer(file));
        assertEquals(Home.ROOT, container.getRoot(file));
        assertEquals("Auditing/nix4.txt", container.getKey(file));
        assertEquals(file.getParent().getParent().getParent().getParent(), container.getDeepboxPath(file));
        assertEquals(file.getParent().getParent().getParent(), container.getBoxPath(file));
        assertEquals(file.getParent().getParent(), container.getThirdLevelPath(file));
    }
}