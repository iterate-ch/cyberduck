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
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class DriveTouchFeatureTest extends AbstractDriveTest {

    @Test
    public void testTouch() throws Exception {
        final DriveFileidProvider fileid = new DriveFileidProvider(session).withCache(cache);
        final Path test = new DriveTouchFeature(session, fileid).touch(
            new Path(DriveHomeFinderService.MYDRIVE_FOLDER, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(test.attributes().getVersionId());
        assertEquals(test.attributes().getVersionId(), new DriveAttributesFinderFeature(session, fileid).find(test, new DisabledListProgressListener()).getVersionId());
        new DriveDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
