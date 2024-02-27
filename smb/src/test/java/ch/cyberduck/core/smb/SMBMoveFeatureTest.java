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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBMoveFeatureTest extends AbstractSMBTest {

    @Test
    public void testRename() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new SMBDirectoryFeature(session).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new SMBTouchFeature(session).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final PathAttributes attr = new SMBAttributesFinderFeature(session).find(file);
        assertEquals(file.attributes(), attr);

        // rename file
        final Path fileRenamed = new SMBMoveFeature(session).move(file, new Path(folder,
                        new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(),
                new Delete.DisabledCallback(), new DisabledConnectionCallback());

        assertFalse(new SMBFindFeature(session).find(file));
        assertTrue(new SMBFindFeature(session).find(fileRenamed));
        assertEquals(file.attributes(), fileRenamed.attributes());
        assertEquals(file.attributes(), new SMBAttributesFinderFeature(session).find(fileRenamed));

        // rename folder
        final Path folderRenamed = new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new SMBMoveFeature(session).move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());

        assertFalse(new SMBFindFeature(session).find(folder));
        assertTrue(new SMBFindFeature(session).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, fileRenamed.getName(), EnumSet.of(Path.Type.file));
        assertTrue(new SMBFindFeature(session).find(fileRenamedInRenamedFolder));
        assertEquals(file.attributes(), new SMBAttributesFinderFeature(session).find(fileRenamedInRenamedFolder));
        new SMBDeleteFeature(session).delete(Collections.singletonList(folderRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testRenameFileOverride() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path file = new SMBTouchFeature(session).touch(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final Path target = new SMBTouchFeature(session).touch(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        // rename file
        final Path fileRenamed = new SMBMoveFeature(session).move(file, target, new TransferStatus().exists(true),
                new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SMBFindFeature(session).find(file));
        assertTrue(new SMBFindFeature(session).find(fileRenamed));
        assertEquals(file.attributes(), new SMBAttributesFinderFeature(session).find(fileRenamed));
        new SMBDeleteFeature(session).delete(Collections.singletonList(fileRenamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path workdir = new DefaultHomeFinderService(session).find();
        final Path test = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path target = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SMBMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}
