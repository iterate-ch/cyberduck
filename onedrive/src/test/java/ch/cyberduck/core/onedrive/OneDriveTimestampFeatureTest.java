package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@Category(IntegrationTest.class)
public class OneDriveTimestampFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final Path file = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(new OneDriveAttributesFinderFeature(session).find(file));

        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new OneDriveTimestampFeature(session).setTimestamp(file, modified);
        assertEquals(modified, new OneDriveAttributesFinderFeature(session).find(file).getModificationDate());

        new OneDriveDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new OneDriveDirectoryFeature(session).mkdir(test, null, null);
        assertNotNull(new OneDriveAttributesFinderFeature(session).find(test));

        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).getEpochSecond() * 1000;
        new OneDriveTimestampFeature(session).setTimestamp(test, modified);
        assertEquals(modified, new OneDriveAttributesFinderFeature(session).find(test).getModificationDate());

        new OneDriveDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
