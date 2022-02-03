package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageMetadataFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testGetMetadataBucket() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Map<String, String> metadata = new GoogleStorageMetadataFeature(session).getMetadata(bucket);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void testGetMetadataFile() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(test, new TransferStatus().withMime("text/plain"));
        final GoogleStorageMetadataFeature feature = new GoogleStorageMetadataFeature(session);
        assertTrue(feature.getMetadata(test).isEmpty());
        feature.setMetadata(test, Collections.singletonMap("k", "v"));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertTrue(metadata.containsKey("k"));
        assertEquals("v", metadata.get("k"));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetMetadataFileLeaveOtherFeatures() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String v = UUID.randomUUID().toString();
        final GoogleStorageStorageClassFeature storage = new GoogleStorageStorageClassFeature(session);
        storage.setClass(test, "NEARLINE");
        assertEquals("NEARLINE", storage.getClass(test));
        final GoogleStorageMetadataFeature feature = new GoogleStorageMetadataFeature(session);
        feature.setMetadata(test, Collections.singletonMap("Test", v));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        assertEquals("NEARLINE", storage.getClass(test));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
