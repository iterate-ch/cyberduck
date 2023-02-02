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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveTouchFeatureTest extends AbstractDriveTest {

    @Test
    public void testTouch() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path folder = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new DriveTouchFeature(session, fileid).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("x-application/cyberduck"));
        final String id = test.attributes().getFileId();
        assertNotNull(id);
        assertNull(test.attributes().getVersionId());
        assertEquals(test.attributes().getFileId(), new DriveAttributesFinderFeature(session, fileid).find(test).getFileId());
        try {
            new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
            fail();
        }
        catch(ConflictException e) {
            // Expected
        }
        new DriveTrashFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertNull(test.attributes().getFileId());
        // Trashed
        assertFalse(new DriveFindFeature(session, fileid).find(test));
        assertFalse(new DefaultFindFeature(session).find(test));
        test.attributes().setFileId(id);
        assertTrue(new DriveFindFeature(session, fileid).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
