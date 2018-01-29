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
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;

import org.junit.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OneDriveTimestampFeatureTest extends AbstractOneDriveTest {
    @Test
    public void testSetTimestamp() throws Exception {
        final Touch touch = new OneDriveTouchFeature(session);
        final Delete delete = new OneDriveDeleteFeature(session);
        final AttributesFinder attributesFinder = new OneDriveAttributesFinderFeature(session);
        final Timestamp timestamp = new OneDriveTimestampFeature(session);

        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final Path file = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        touch.touch(file, new TransferStatus().withMime("x-application/cyberduck"));
        assertNotNull(attributesFinder.find(file));

        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).toEpochMilli();
        timestamp.setTimestamp(file, modified);
        assertEquals(modified, attributesFinder.find(file).getModificationDate());

        delete.delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetTimestampDirectory() throws Exception {
        final Directory directory = new OneDriveDirectoryFeature(session);
        final Delete delete = new OneDriveDeleteFeature(session);
        final AttributesFinder attributesFinder = new OneDriveAttributesFinderFeature(session);
        final Timestamp timestamp = new OneDriveTimestampFeature(session);

        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        directory.mkdir(test, null, null);
        assertNotNull(attributesFinder.find(test));

        final long modified = Instant.now().minusSeconds(5 * 24 * 60 * 60).toEpochMilli();
        timestamp.setTimestamp(test, modified);
        assertEquals(modified, attributesFinder.find(test).getModificationDate());

        delete.delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
