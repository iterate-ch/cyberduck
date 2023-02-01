package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageDirectoryFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testMakeBucket() throws Exception {
        final Path test = new Path(new DefaultHomeFinderService(session).find(),
                new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageDirectoryFeature(session).mkdir(test, new TransferStatus().withRegion("us"));
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        try {
            new GoogleStorageDirectoryFeature(session).mkdir(test, new TransferStatus());
            fail();
        }
        catch(ConflictException e) {
            // Expected
        }
        new GoogleStorageDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testDirectoryDeleteWithVersioning() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path parent = new GoogleStorageDirectoryFeature(session).mkdir(new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new GoogleStorageDirectoryFeature(session).mkdir(new Path(parent,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertNotNull(test.attributes().getVersionId());
        // Only placeholder is found in list output with no version id set
        assertTrue(test.isPlaceholder());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        // This will only cause a delete marker being added
        new GoogleStorageDeleteFeature(session).delete(Arrays.asList(new Path(test).withAttributes(PathAttributes.EMPTY), parent), new DisabledLoginCallback(), new Delete.DisabledCallback());
        // Specific version is still found
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertFalse(new DefaultFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertTrue(new GoogleStorageAttributesFinderFeature(session).find(new Path(test)).isDuplicate());
        assertTrue(new DefaultAttributesFinderFeature(session).find(new Path(test)).isDuplicate());
    }

    @Test
    public void testDirectoryWhitespace() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageDirectoryFeature(session).mkdir(new Path(bucket,
                String.format("%s %s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCreatePlaceholderVersioningDelete() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageDirectoryFeature(session).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new GoogleStorageObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new GoogleStorageObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(test));
        assertFalse(new DefaultFindFeature(session).find(test));
        assertFalse(new GoogleStorageFindFeature(session).find(test));
    }

    @Test
    public void testCreatePlaceholderVersioningDeleteWithMarker() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageDirectoryFeature(session).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(test.getType().contains(Path.Type.placeholder));
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new GoogleStorageObjectListService(session).list(bucket, new DisabledListProgressListener()).contains(test));
        // Add delete marker
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(new Path(test).withAttributes(PathAttributes.EMPTY)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertFalse(new GoogleStorageFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertTrue(new DefaultFindFeature(session).find(test));
        assertTrue(new GoogleStorageFindFeature(session).find(test));
    }
}
