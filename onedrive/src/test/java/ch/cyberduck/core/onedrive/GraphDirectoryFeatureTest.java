package ch.cyberduck.core.onedrive;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.RandomStringService;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.features.GraphAttributesFinderFeature;
import ch.cyberduck.core.onedrive.features.GraphDeleteFeature;
import ch.cyberduck.core.onedrive.features.GraphDirectoryFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GraphDirectoryFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testMkdir() throws Exception {
        final TransferStatus status = new TransferStatus();
        final Path target = new GraphDirectoryFeature(session, fileid).mkdir(new Path(new OneDriveHomeFinderService().find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), status);
        final PathAttributes attributes = new GraphAttributesFinderFeature(session, fileid).find(target);
        assertNotNull(attributes.getETag());
        assertEquals(target.attributes().getFileId(), attributes.getFileId());
        // Can create again regardless if exists
        new GraphDirectoryFeature(session, fileid).mkdir(target, new TransferStatus());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testWhitespaceMkdir() throws Exception {
        final RandomStringService randomStringService = new AlphanumericRandomStringService();
        final String name = String.format("%s %s", randomStringService.random(), randomStringService.random());
        final Path target = new GraphDirectoryFeature(session, fileid).mkdir(new Path(new OneDriveHomeFinderService().find(), name, EnumSet.of(Path.Type.directory)), null);
        assertEquals(name, target.getName());
        final AttributedList<Path> list = new GraphItemListService(session, fileid).list(new OneDriveHomeFinderService().find(), new DisabledListProgressListener());
        assertTrue(list.contains(target));
        assertNotNull(new GraphAttributesFinderFeature(session, fileid).find(target).getETag());
        new GraphDeleteFeature(session, fileid).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
