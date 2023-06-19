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
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.*;

public class SMBMoveFeatureTest extends AbstractSMBTest {

    @Test
    public void testRename() throws Exception {
        final Path home = new DefaultHomeFinderService(session).find();
        final Path folder = new Path(home, "folder-to-rename", EnumSet.of(Path.Type.directory));
        session.getFeature(Directory.class).mkdir(folder, new TransferStatus());
        final Path file = new Path(folder, "userTest_v1.txt", EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(file, new TransferStatus());
        PathAttributes fileAttributes = session.getFeature(AttributesFinder.class).find(file);

        final Move move = session.getFeature(Move.class);

        // rename file
        final Path fileRenamed = move.move(file, new Path(folder, "userTest_v2.txt", EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());

        assertFalse(session.getFeature(Find.class).find(file));
        assertTrue(session.getFeature(Find.class).find(fileRenamed));
        assertEquals(fileAttributes, session.getFeature(AttributesFinder.class).find(fileRenamed));

        // rename folder
        final Path folderRenamed = new Path(home, "folder-renamed", EnumSet.of(Path.Type.directory));
        move.move(folder, folderRenamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());

        assertFalse(session.getFeature(Find.class).find(folder));
        assertTrue(session.getFeature(Find.class).find(folderRenamed));
        final Path fileRenamedInRenamedFolder = new Path(folderRenamed, "userTest_v2.txt", EnumSet.of(Path.Type.file));
        assertTrue(session.getFeature(Find.class).find(fileRenamedInRenamedFolder));
        assertEquals(fileAttributes, session.getFeature(AttributesFinder.class).find(fileRenamedInRenamedFolder));
    }
}
