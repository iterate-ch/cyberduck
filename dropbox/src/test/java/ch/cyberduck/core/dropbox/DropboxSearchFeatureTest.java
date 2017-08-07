package ch.cyberduck.core.dropbox;

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

import ch.cyberduck.core.AbstractDropboxTest;
import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
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
public class DropboxSearchFeatureTest extends AbstractDropboxTest {

    @Test
    public void testSearch() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final Path workdir = new DefaultHomeFinderService(session).find();
        final Path file = new Path(workdir, name, EnumSet.of(Path.Type.file));
        new DropboxTouchFeature(session).touch(file, new TransferStatus());
        final DropboxSearchFeature feature = new DropboxSearchFeature(session);
        assertTrue(feature.search(workdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        // Supports prefix matching only
        assertFalse(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(workdir, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).contains(file));
        try {
            assertFalse(feature.search(new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new SearchFilter(name), new DisabledListProgressListener()).contains(file));
            fail();
        }
        catch(NotfoundException e) {
            //
        }
        final Path subdir = new Path(workdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new DropboxDirectoryFeature(session).mkdir(subdir, null, new TransferStatus());
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        final Path filesubdir = new DropboxTouchFeature(session).touch(new Path(subdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        {
            final AttributedList<Path> result = feature.search(workdir, new SearchFilter(filesubdir.getName()), new DisabledListProgressListener());
            assertTrue(result.contains(filesubdir));
            assertEquals(subdir, result.find(new SimplePathPredicate(filesubdir)).getParent());
        }
        new DropboxDeleteFeature(session).delete(Arrays.asList(file, filesubdir, subdir), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}