package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class AzureFindFeatureTest extends AbstractAzureTest {

    @Test
    public void testFindNotFound() throws Exception {
        assertFalse(new AzureFindFeature(session, null).find(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindHome() throws Exception {
        assertTrue(new AzureFindFeature(session, null).find(new DefaultHomeFinderService(session).find()));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new AzureDirectoryFeature(session, null).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(folder));
        assertFalse(new AzureFindFeature(session, null).find(new Path(folder.getAbsolute(), EnumSet.of(Path.Type.file))));
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindFile() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new AzureTouchFeature(session, null).touch(file, new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(file));
        assertFalse(new AzureFindFeature(session, null).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new AzureFindFeature(session, null).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path intermediate = new Path(container, prefix, EnumSet.of(Path.Type.directory));
        final Path test = new AzureTouchFeature(session, null).touch(new Path(intermediate, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new AzureFindFeature(session, null).find(test));
        assertFalse(new AzureFindFeature(session, null).find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory))));
        assertTrue(new AzureFindFeature(session, null).find(intermediate));
        // Ignore 404 for placeholder and search for common prefix
        assertTrue(new AzureFindFeature(session, null).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(new AzureObjectListService(session, null).list(intermediate,
                new DisabledListProgressListener()).contains(test));
        new AzureDeleteFeature(session, null).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new AzureFindFeature(session, null).find(test));
        assertFalse(new AzureFindFeature(session, null).find(intermediate));
        final PathCache cache = new PathCache(1);
        final Path directory = new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertFalse(new CachingFindFeature(session, cache, new AzureFindFeature(session, null)).find(directory));
        assertFalse(cache.isCached(directory));
        assertFalse(new AzureFindFeature(session, null).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }
}