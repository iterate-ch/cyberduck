package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphTimestampFeature;
import ch.cyberduck.core.onedrive.features.GraphTouchFeature;
import ch.cyberduck.core.onedrive.features.GraphWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.EnumSet;

@Category(IntegrationTest.class)
public class GraphTimestampFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testSetTimestamp() throws Exception {
        final Path drive = new OneDriveHomeFinderService().find();
        final Path test = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GraphTouchFeature(session, fileid).touch(new GraphWriteFeature(session, fileid), test, new TransferStatus());
        final TransferStatus status = new TransferStatus();
        new GraphTimestampFeature(session, fileid).setTimestamp(test, status.setModified(1671187993791L));
        assertEquals(1671187993000L, status.getResponse().getModificationDate());
        final PathAttributes attr = new GraphAttributesFinderFeature(session, fileid).find(test);
        assertEquals(1671187993000L, attr.getModificationDate());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}