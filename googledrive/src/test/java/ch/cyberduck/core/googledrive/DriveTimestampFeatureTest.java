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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class DriveTimestampFeatureTest extends AbstractDriveTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path test = new DriveTouchFeature(session, fileid).touch(new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        new DriveMetadataFeature(session, fileid).setMetadata(test, Collections.singletonMap("test", "t"));
        final long modified = System.currentTimeMillis();
        final TransferStatus status = new TransferStatus().withModified(modified);
        new DriveTimestampFeature(session, fileid).setTimestamp(test, status);
        assertEquals(modified, new DefaultAttributesFinderFeature(session).find(test).getModificationDate());
        final PathAttributes attr = new DriveAttributesFinderFeature(session, fileid).find(test);
        assertEquals(modified, attr.getModificationDate());
        assertEquals(attr, status.getResponse());
        assertEquals(Collections.singletonMap("test", "t"), new DriveMetadataFeature(session, fileid).getMetadata(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path test = new DriveDirectoryFeature(session, fileid).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final long modified = System.currentTimeMillis();
        new DriveTimestampFeature(session, fileid).setTimestamp(test, modified);
        assertEquals(modified, new DefaultAttributesFinderFeature(session).find(test).getModificationDate());
        assertEquals(modified, new DriveAttributesFinderFeature(session, fileid).find(test).getModificationDate());
        new DriveDeleteFeature(session, fileid).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
