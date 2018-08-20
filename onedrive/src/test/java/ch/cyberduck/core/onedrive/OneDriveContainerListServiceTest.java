package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.onedrive.features.OneDriveAttributesFinderFeature;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class OneDriveContainerListServiceTest extends AbstractOneDriveTest {

    @Test
    public void testFindDrive() throws Exception {
        final AttributedList<Path> drives = new GraphDrivesListService(session).list(new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume)), new DisabledListProgressListener());
        assertFalse(drives.isEmpty());
        for(Path drive : drives) {
            final PathAttributes attributes = new OneDriveAttributesFinderFeature(session).find(drive);
            assertNotNull(attributes);
            assertNotEquals(-1L, attributes.getSize());
            assertNotEquals(-1L, attributes.getCreationDate());
            assertNotEquals(-1L, attributes.getModificationDate());
            assertNotNull(attributes.getVersionId());
            assertNotNull(attributes.getLink());
        }
    }

}
