package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBDeleteFeatureTest extends AbstractSMBTest {

    @Test
    public void testDeleteFile() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, "folder", EnumSet.of(Path.Type.directory));
        final Path file = new Path(home, "folder/L0-file.txt", EnumSet.of(Path.Type.file));

        int listCount = session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).size();
        Path[] paths = {file};
        assertTrue(session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).contains(file));

        new SMBDeleteFeature(session).delete(Arrays.asList(paths), new DisabledPasswordCallback(), new Delete.DisabledCallback());

        assertFalse(session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).contains(file));
        assertEquals(listCount - 1, session.getFeature(ListService.class).list(folder, new DisabledListProgressListener()).size());
    }

    @Test
    public void testDeleteFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, "folder", EnumSet.of(Path.Type.directory));

        int listCount = session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).size();
        assertTrue(session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).contains(folder));
        Path[] paths = {folder};

        new SMBDeleteFeature(session).delete(Arrays.asList(paths), new DisabledPasswordCallback(), new Delete.DisabledCallback());

        assertFalse(session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).contains(folder));
        assertEquals(listCount - 1, session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).size());
    }

    @Test
    public void testDeleteFileAndFolder() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new Path(home, "userTest.txt", EnumSet.of(Path.Type.file));
        final Path folder = new Path(home, "empty_folder", EnumSet.of(Path.Type.directory));

        int listCount = session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).size();
        assertTrue(session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).contains(file));
        assertTrue(session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).contains(folder));

        Path[] paths = {file, folder};
        new SMBDeleteFeature(session).delete(Arrays.asList(paths), new DisabledPasswordCallback(), new Delete.DisabledCallback());

        assertFalse(session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).contains(file));
        assertFalse(session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).contains(folder));
        assertEquals(listCount - 2, session.getFeature(ListService.class).list(home, new DisabledListProgressListener()).size());
    }
}
