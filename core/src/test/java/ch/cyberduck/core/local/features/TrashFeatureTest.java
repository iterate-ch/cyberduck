package ch.cyberduck.core.local.features;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.LocalTouchFactory;
import ch.cyberduck.core.local.LocalTrashFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinder;
import ch.cyberduck.core.preferences.TemporarySupportDirectoryFinder;

import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.util.UUID;

public class TrashFeatureTest {
    @Test
    public void testTrash() throws Exception {
        final Trash trash = LocalTrashFactory.get();
        final SupportDirectoryFinder finder = new TemporarySupportDirectoryFinder();

        final Local temp = finder.find();
        final Local directory = LocalFactory.get(temp, UUID.randomUUID().toString());
        directory.mkdir();

        trash.trash(directory);
    }

    @Test
    public void testTrashNonEmpty() throws Exception {
        final Trash trash = LocalTrashFactory.get();
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

    @Test(expected = LocalAccessDeniedException.class)
    public void testTrashOpenFile() throws Exception {
        final Trash trash = LocalTrashFactory.get();
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
}
