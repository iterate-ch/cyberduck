package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageFindFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testFindFileNotFound() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final GoogleStorageFindFeature f = new GoogleStorageFindFeature(session);
        assertFalse(f.find(new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))));
    }

    @Test
    public void testFindDirectory() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path folder = new GoogleStorageDirectoryFeature(session).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(folder));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(folder.getAbsolute(), EnumSet.of(Path.Type.file))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindFile() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(file, new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(file));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDeleted() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new GoogleStorageFindFeature(session).find(test));
    }

    @Test
    public void testFindCommonPrefix() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        assertTrue(new GoogleStorageFindFeature(session).find(container));
        final String prefix = new AlphanumericRandomStringService().random();
        final Path test = new GoogleStorageTouchFeature(session).touch(
                new Path(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                        new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(test.getAbsolute(), EnumSet.of(Path.Type.directory))));
        assertTrue(new GoogleStorageFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        assertTrue(new GoogleStorageFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
        assertTrue(new GoogleStorageObjectListService(session).list(new Path(container, prefix, EnumSet.of(Path.Type.directory)),
                new DisabledListProgressListener()).contains(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new GoogleStorageFindFeature(session).find(test));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory))));
        final PathCache cache = new PathCache(1);
        final Path directory = new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertFalse(new CachingFindFeature(session, cache, new GoogleStorageFindFeature(session)).find(directory));
        assertFalse(cache.isCached(directory));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(container, prefix, EnumSet.of(Path.Type.directory, Path.Type.placeholder))));
    }
}
