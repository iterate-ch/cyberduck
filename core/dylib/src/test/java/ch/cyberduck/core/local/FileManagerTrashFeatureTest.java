package ch.cyberduck.core.local;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.features.Touch;
import ch.cyberduck.core.local.features.Trash;
import ch.cyberduck.core.preferences.SupportDirectoryFinder;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;

import org.junit.Test;

import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FileManagerTrashFeatureTest {

    @Test
    public void testTrash() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        new FileManagerTrashFeature().trash(l);
        assertFalse(l.exists());
    }

    @Test(expected = LocalAccessDeniedException.class)
    public void testTrashNotfound() throws Exception {
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        assertFalse(l.exists());
        new FileManagerTrashFeature().trash(l);
    }

    @Test
    public void testTrashRepeated() throws Exception {
        final FileManagerTrashFeature f = new FileManagerTrashFeature();
        Local l = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        new DefaultLocalTouchFeature().touch(l);
        assertTrue(l.exists());
        f.trash(l);
        assertFalse(l.exists());
    }

    @Test
    public void testTrashNonEmpty() throws Exception {
        final Trash trash = new FileManagerTrashFeature();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();
        final Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
        sub.mkdir();
        final Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
        final Touch touch = LocalTouchFactory.get();
        touch.touch(file);

        trash.trash(directory);
    }

    @Test
    public void testTrashOpenFile() throws Exception {
        final Trash trash = new FileManagerTrashFeature();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();
        final Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
        sub.mkdir();
        final Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
        final Touch touch = LocalTouchFactory.get();
        touch.touch(file);

        try (final OutputStream stream = file.getOutputStream(false)) {
            trash.trash(directory);
        }
    }

    @Test
    public void testTrashOpenDirectoryEnumeration() throws Exception {
        final Trash trash = new FileManagerTrashFeature();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();
        final Local sub = LocalFactory.get(directory, UUID.randomUUID().toString());
        sub.mkdir();
        final Local file = LocalFactory.get(sub, UUID.randomUUID().toString());
        final Touch touch = LocalTouchFactory.get();
        touch.touch(file);

        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(sub.getAbsolute()))) {
            trash.trash(directory);
        }
    }
}
