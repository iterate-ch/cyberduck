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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;


public class DeepboxPathContainerServiceTest {

    @Test
    public void TestRoot() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertFalse(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertNull(container.getKey(folder));
    }

    @Test
    public void TestDeepBox() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path("/Mountainduck Buddies", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertTrue(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertNull(container.getKey(folder));
    }

    @Test
    public void TestBox() {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path("/Mountainduck Buddies/My Box", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertTrue(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertNull(container.getKey(folder));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Inbox"})
    public void TestInbox(final String name) {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s", name), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertTrue(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertTrue(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertNull(container.getKey(folder));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Documents", "Dokumente"})
    public void TestDocuments(final String name) {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s", name), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertTrue(container.isThirdLevel(folder));
        assertTrue(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertNull(container.getKey(folder));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Trash", "Papierkorb", "Corbeille", "Cestino"})
    public void TestTrash(final String name) {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s", name), EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume));
        assertTrue(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertTrue(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertTrue(container.isTrash(folder));
        assertEquals(folder, container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertNull(container.getKey(folder));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Documents", "Dokumente"})
    public void TestAuditing(final String name) {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path folder = new Path(String.format("/Mountainduck Buddies/My Box/%s/Auditing", name), EnumSet.of(AbstractPath.Type.directory));
        assertFalse(container.isContainer(folder));
        assertFalse(container.isDeepbox(folder));
        assertFalse(container.isBox(folder));
        assertFalse(container.isThirdLevel(folder));
        assertFalse(container.isDocuments(folder));
        assertFalse(container.isInbox(folder));
        assertFalse(container.isTrash(folder));
        assertEquals(folder.getParent(), container.getContainer(folder));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(folder));
        assertEquals("Auditing", container.getKey(folder));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Documents", "Dokumente"})
    public void TestFile(final String name) {
        final DeepboxPathContainerService container = new DeepboxPathContainerService();
        final Path file = new Path(String.format("/Mountainduck Buddies/My Box/%s/Auditing/nix4.txt", name), EnumSet.of(AbstractPath.Type.file));
        assertFalse(container.isContainer(file));
        assertFalse(container.isDeepbox(file));
        assertFalse(container.isBox(file));
        assertFalse(container.isThirdLevel(file));
        assertFalse(container.isDocuments(file));
        assertFalse(container.isInbox(file));
        assertFalse(container.isTrash(file));
        assertEquals(file.getParent().getParent(), container.getContainer(file));
        assertEquals(new Path("/", EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume)), container.getRoot(file));
        assertEquals("Auditing/nix4.txt", container.getKey(file));
    }
}