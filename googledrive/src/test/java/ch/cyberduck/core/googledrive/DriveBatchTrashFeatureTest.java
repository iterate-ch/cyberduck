package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class DriveBatchTrashFeatureTest extends AbstractDriveTest {

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFound() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new DriveBatchTrashFeature(session, new DriveFileIdProvider(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleteFromTrash() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path file = new DriveTouchFeature(session, fileid).touch(
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String fileId = file.attributes().getFileId();
        new DriveBatchTrashFeature(session, fileid).delete(Collections.singletonList(file), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DriveFindFeature(session, fileid).find(file));
        assertTrue(new DriveFindFeature(session, fileid).find(file.withAttributes(new PathAttributes().withFileId(fileId))));
        final PathAttributes attributesInTrash = new DriveAttributesFinderFeature(session, fileid).find(file.withAttributes(new PathAttributes().withFileId(fileId)));
        assertTrue(attributesInTrash.isHidden());
        new DriveBatchTrashFeature(session, fileid).delete(Collections.singletonList(file.withAttributes(attributesInTrash)), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new DriveFindFeature(session, fileid).find(file.withAttributes(attributesInTrash)));
    }
}
