package ch.cyberduck.core.smb;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.TestcontainerTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(TestcontainerTest.class)
public class SMBMoveFeatureTest extends AbstractSMBTest {

    @Test
    public void testRename() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, "folder-to-rename", EnumSet.of(Path.Type.directory));
        new SMBDirectoryFeature(session).mkdir(folder, new TransferStatus());
        final Path file = new Path(folder, "userTest_v1.txt", EnumSet.of(Path.Type.file));
        new SMBTouchFeature(session).touch(file, new TransferStatus());
        PathAttributes fileAttributes = new SMBAttributesFinderFeature(session).find(file);

        final Move move = new SMBMoveFeature(session);

        // rename file
        final Path fileRenamed = move.move(file, new Path(folder, "userTest_v2.txt", EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());

        assertFalse(new SMBFindFeature(session).find(file));
        assertTrue(new SMBFindFeature(session).find(fileRenamed));
        assertEquals(fileAttributes, new SMBAttributesFinderFeature(session).find(fileRenamed));

        // rename folder
        final Path folderRenamed = new Path(home, "folder-renamed", EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());

        assertFalse(new SMBFindFeature(session).find(folder));
        assertTrue(new SMBFindFeature(session).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "userTest_v2.txt", EnumSet.of(Path.Type.file));
        assertTrue(new SMBFindFeature(session).find(fileRenamedInRenamedFolder));
        assertEquals(fileAttributes, new SMBAttributesFinderFeature(session).find(fileRenamedInRenamedFolder));
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path workdir = new DefaultHomeFinderService(session).find();
        final Path test = new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new SMBMoveFeature(session).move(test, new Path(workdir, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }
}
