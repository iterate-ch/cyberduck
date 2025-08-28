package ch.cyberduck.core.googledrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static ch.cyberduck.core.googledrive.DriveHomeFinderService.MYDRIVE_FOLDER;
import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveMetadataFeatureTest extends AbstractDriveTest {

    @Test
    public void setMetadata() throws Exception {
        final Path test = new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final TransferStatus status = new TransferStatus();
        new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid), test, status);
        final DriveMetadataFeature feature = new DriveMetadataFeature(session, fileid);
        assertEquals(Collections.emptyMap(), feature.getMetadata(test));
        feature.setMetadata(test, Collections.singletonMap("test", "t"));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertEquals(Collections.singletonMap("test", "t"), metadata);
        test.attributes().setMetadata(metadata);
        feature.setMetadata(test, status.setMetadata(Collections.emptyMap()));
        assertFalse(status.getResponse().getMetadata().containsKey("test"));
        assertFalse(feature.getMetadata(test).containsKey("test"));
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testChangedFileId() throws Exception {
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path room = new DriveDirectoryFeature(session, fileid).mkdir(
                new DriveWriteFeature(session, fileid), new Path(MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new DriveTouchFeature(session, fileid).touch(new DriveWriteFeature(session, fileid), new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestfileid = test.attributes().getFileId();
        assertNotNull(latestfileid);
        // Assume previously seen but changed on server
        fileid.cache(test, String.valueOf(RandomUtils.nextLong()));
        final DriveMetadataFeature f = new DriveMetadataFeature(session, fileid);
        try {
            f.getMetadata(test);
        }
        catch(NotfoundException e) {
            assertNull(test.attributes().getFileId());
        }
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
