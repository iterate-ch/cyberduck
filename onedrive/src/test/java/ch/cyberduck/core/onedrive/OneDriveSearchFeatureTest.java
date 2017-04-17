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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class OneDriveSearchFeatureTest extends AbstractOneDriveTest {

    @Test
    public void testSearch() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final Path drive = new OneDriveHomeFinderFeature(session).find();
        final Path directory = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new OneDriveDirectoryFeature(session).mkdir(directory, null, new TransferStatus());
        final Path file = new Path(directory, name, EnumSet.of(Path.Type.file));
        new OneDriveTouchFeature(session).touch(file, new TransferStatus());
        final OneDriveSearchFeature feature = new OneDriveSearchFeature(session);
        assertTrue(feature.search(drive, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(drive, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(drive, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(directory, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        final Path subdir = new Path(drive, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        new OneDriveDeleteFeature(session).delete(Arrays.asList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
