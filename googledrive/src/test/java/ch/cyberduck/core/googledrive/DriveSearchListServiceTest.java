package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DriveSearchListServiceTest extends AbstractDriveTest {

    @Test
    public void testQuery() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path directory = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Drive.Files.Create insert = session.getClient().files().create(new File()
                .setName(name)
                .setParents(Collections.singletonList(fileid.getFileId(directory))));
        final File execute = insert.execute();
        execute.setVersion(1L);
        final Path file = new Path(directory, name, EnumSet.of(Path.Type.file), new DriveAttributesFinderFeature(session, fileid).toAttributes(execute));
        {
            final Path subdirectory = new DriveDirectoryFeature(session, fileid).mkdir(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
            assertTrue(new DriveSearchListService(session, fileid, name).list(subdirectory, new DisabledListProgressListener()).isEmpty());
            new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(subdirectory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        }
        {
            final AttributedList<Path> list = new DriveSearchListService(session, fileid, name).list(DriveHomeFinderService.MYDRIVE_FOLDER, new DisabledListProgressListener());
            assertEquals(1, list.size());
            assertEquals(file, list.get(0));
        }
        {
            assertTrue(new DriveSearchListService(session, fileid, name).list(DriveHomeFinderService.SHARED_FOLDER_NAME, new DisabledListProgressListener()).isEmpty());
        }
        {
            final AttributedList<Path> list = new DriveSearchListService(session, fileid, name).list(directory, new DisabledListProgressListener());
            assertEquals(1, list.size());
            assertEquals(file, list.get(0));
        }
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, directory), new DisabledPasswordCallback(), new Delete.DisabledCallback());
    }
}
