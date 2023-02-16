package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2DirectoryFeatureTest extends AbstractB2Test {

    @Test
    public void testCreateBucket() throws Exception {
        final Path bucket = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2DirectoryFeature feature = new B2DirectoryFeature(session, fileid);
        assertTrue(feature.isSupported(bucket.getParent(), bucket.getName()));
        feature.mkdir(bucket, new TransferStatus());
        assertThrows(ConflictException.class, () -> feature.mkdir(bucket, new TransferStatus()));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = ConflictException.class)
    public void testBucketExists() throws Exception {
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        try {
            new B2DirectoryFeature(session, new B2VersionIdProvider(session)).mkdir(bucket, new TransferStatus());
        }
        catch(ConflictException e) {
            assertEquals("Bucket name is already in use. Please contact your web hosting service provider for assistance.", e.getDetail());
            assertEquals("Cannot create folder test-cyberduck.", e.getMessage());
            throw e;
        }
    }

    @Test(expected = InteroperabilityException.class)
    public void testBucketInvalidCharacter() throws Exception {
        final Path bucket = new Path("untitled folder", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        assertFalse(new B2DirectoryFeature(session, fileid).isSupported(bucket.getParent(), bucket.getName()));
        try {
            new B2DirectoryFeature(session, fileid).mkdir(bucket, new TransferStatus());
        }
        catch(InteroperabilityException e) {
            assertEquals("Invalid characters in bucketName: must be alphanumeric or '-'. Please contact your web hosting service provider for assistance.", e.getDetail());
            assertEquals("Cannot create folder untitled folder.", e.getMessage());
            throw e;
        }
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path directory = new B2DirectoryFeature(session, fileid, new B2WriteFeature(session, fileid)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path file = new B2TouchFeature(session, fileid).touch(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(directory.getType().contains(Path.Type.placeholder));
        assertTrue(new B2FindFeature(session, fileid).find(directory));
        assertTrue(new DefaultFindFeature(session).find(directory));
        // Mark as hidden
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(new Path(directory).withAttributes(PathAttributes.EMPTY)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new B2FindFeature(session, fileid).find(directory));
        assertTrue(new DefaultFindFeature(session).find(directory));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new DefaultFindFeature(session).find(directory));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testModificationDate() throws Exception {
        final Path bucket = new Path("/test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        final long timestamp = 1509959502930L;
        status.setTimestamp(timestamp);
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path directory = new B2DirectoryFeature(session, fileid, new B2WriteFeature(session, fileid)).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), status);
        final Path test = new B2DirectoryFeature(session, fileid, new B2WriteFeature(session, fileid)).mkdir(new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), status);
        assertEquals(timestamp, new B2AttributesFinderFeature(session, fileid).find(test).getModificationDate());
        assertEquals(timestamp, new B2AttributesFinderFeature(session, fileid).find(directory).getModificationDate());
        // Timestamp for placeholder is unknown. Only set on /.bzEmpty
        assertNotEquals(timestamp, new B2ObjectListService(session, fileid).list(directory, new DisabledListProgressListener()).get(test).attributes().getModificationDate());
        new B2DeleteFeature(session, fileid).delete(Arrays.asList(test, directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
