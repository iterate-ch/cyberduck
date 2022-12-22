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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DriveSearchFeatureTest extends AbstractDriveTest {

    @Test
    public void testSearchRoot() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final Path workdir = DriveHomeFinderService.MYDRIVE_FOLDER;
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(workdir, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final DriveSearchFeature feature = new DriveSearchFeature(session, fileid);
        assertTrue(feature.search(workdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        // Supports prefix matching only
        assertFalse(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        final Path subdir = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSearchFolder() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path workdir = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(workdir, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final DriveSearchFeature feature = new DriveSearchFeature(session, fileid);
        assertTrue(feature.search(workdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        final AttributedList<Path> result = feature.search(workdir, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener());
        assertTrue(result.contains(file));
        assertEquals(workdir, result.get(result.indexOf(file)).getParent());
        final Path subdir = new DriveDirectoryFeature(session, fileid).mkdir(new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, subdir, workdir), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSearchFolderRecursively() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final DriveFileIdProvider fileid = new DriveFileIdProvider(session);
        final Path workdir = new DriveDirectoryFeature(session, fileid).mkdir(new Path(DriveHomeFinderService.MYDRIVE_FOLDER, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new DriveTouchFeature(session, fileid).touch(new Path(workdir, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final DriveSearchFeature feature = new DriveSearchFeature(session, fileid);
        assertTrue(feature.search(DriveHomeFinderService.MYDRIVE_FOLDER, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        new DriveDeleteFeature(session, fileid).delete(Arrays.asList(file, workdir), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
