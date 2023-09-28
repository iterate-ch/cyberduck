package ch.cyberduck.core.googledrive;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveTrashedListServiceTest extends AbstractDriveTest {

    @Test
    public void testList() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(home,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DriveTrashFeature(session, fileid).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final AttributedList<Path> list = new DriveTrashedListService(session, fileid).list(DriveHomeFinderService.TRASH_FOLDER, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path f : list) {
            assertTrue(f.attributes().isHidden());
            assertEquals(f.attributes(), new DriveAttributesFinderFeature(session, fileid).find(f));
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}