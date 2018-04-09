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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class DriveMetadataFeatureTest extends AbstractDriveTest {

    @Test
    public void setMetadata() throws Exception {
        final Path home = DriveHomeFinderService.MYDRIVE_FOLDER;
        final Path test = new Path(home, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        new DriveTouchFeature(session, fileid).touch(test, new TransferStatus());
        assertEquals(Collections.emptyMap(), new DriveMetadataFeature(session, fileid).getMetadata(test));
        new DriveMetadataFeature(session, fileid).setMetadata(test, Collections.singletonMap("test", "t"));
        assertEquals(Collections.singletonMap("test", "t"), new DriveMetadataFeature(session, fileid).getMetadata(test));
        new DriveDeleteFeature(session, fileid).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
