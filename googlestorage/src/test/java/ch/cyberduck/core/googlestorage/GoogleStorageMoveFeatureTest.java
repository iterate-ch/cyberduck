package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class GoogleStorageMoveFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testMove() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageTouchFeature(session).touch(
                new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withMetadata(Collections.singletonMap("cyberduck", "set")));
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        assertFalse(new GoogleStorageMetadataFeature(session).getMetadata(test).isEmpty());
        final Path renamed = new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final Path moved = new GoogleStorageMoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new GoogleStorageFindFeature(session).find(renamed));
        final PathAttributes targetAttr = new GoogleStorageAttributesFinderFeature(session).find(renamed);
        assertEquals(moved.attributes(), targetAttr);
        assertEquals(Comparison.equal, session.getHost().getProtocol().getFeature(ComparisonService.class).compare(Path.Type.file, moved.attributes(), targetAttr));
        assertEquals(1, new GoogleStorageObjectListService(session).list(bucket, new DisabledListProgressListener())
                .filter(new SearchFilter(renamed.getName())).size());
        final Map<String, String> metadata = new GoogleStorageMetadataFeature(session).getMetadata(renamed);
        assertFalse(metadata.isEmpty());
        assertEquals("set", metadata.get("cyberduck"));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveWithDelimiter() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(placeholder, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageTouchFeature(session).touch(test, new TransferStatus());
        final Path renamed = new Path(placeholder, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageMoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new GoogleStorageFindFeature(session).find(renamed));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSupport() {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new GoogleStorageMoveFeature(session).isSupported(c, Optional.of(new Path(new Path("/d", EnumSet.of(Path.Type.directory))))));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new GoogleStorageMoveFeature(session).isSupported(cf, Optional.of(new Path("/c/f2", EnumSet.of(Path.Type.directory)))));
    }

    @Test
    public void testMoveWithServerSideEncryptionBucketPolicy() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final GoogleStorageTouchFeature touch = new GoogleStorageTouchFeature(session);
        final TransferStatus status = new TransferStatus();
        status.setEncryption(new Encryption.Algorithm("AES256", null));
        touch.touch(test, status);
        assertTrue(new GoogleStorageFindFeature(session).find(test));
        final Path renamed = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new GoogleStorageMoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new GoogleStorageFindFeature(session).find(test));
        assertTrue(new GoogleStorageFindFeature(session).find(renamed));
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
