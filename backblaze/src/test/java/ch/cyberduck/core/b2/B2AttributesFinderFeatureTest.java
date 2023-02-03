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
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import synapticloop.b2.response.B2StartLargeFileResponse;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class B2AttributesFinderFeatureTest extends AbstractB2Test {

    @Test
    public void testFindRoot() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, fileid);
        assertEquals(PathAttributes.EMPTY, f.find(new Path("/", EnumSet.of(Path.Type.directory))));
    }

    @Test
    public void testFind() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final long timestamp = System.currentTimeMillis();
        new B2TouchFeature(session, fileid).touch(test, new TransferStatus().withTimestamp(timestamp));
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(test);
        assertEquals(0L, attributes.getSize());
        assertEquals(timestamp, attributes.getModificationDate());
        assertNotNull(attributes.getVersionId());
        assertNull(attributes.getFileId());
        assertNotEquals(Checksum.NONE, attributes.getChecksum());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testHideMarker() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final Path test = new Path(directory, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final long timestamp = System.currentTimeMillis();
        final TransferStatus status = new TransferStatus().withTimestamp(timestamp);
        new B2TouchFeature(session, fileid).touch(test, status);
        assertNotNull(status.getResponse().getVersionId());
        assertNotNull(test.attributes().getVersionId());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(new Path(test).withAttributes(PathAttributes.EMPTY)), new DisabledLoginCallback(), new Delete.DisabledCallback());
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, fileid);
        try {
            f.find(new Path(test).withAttributes(PathAttributes.EMPTY));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new B2DeleteFeature(session, fileid).delete(new B2ObjectListService(session, fileid).list(directory, new DisabledListProgressListener()).toList(), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindDirectory() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path directory = new B2DirectoryFeature(session, fileid).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(directory);
        assertNotNull(attributes);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        // Test wrong type
        try {
            f.find(new Path(directory.getAbsolute(), EnumSet.of(Path.Type.file)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(directory), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindBucket() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, fileid);
        final PathAttributes attributes = f.find(bucket);
        assertNotNull(attributes);
        assertNotEquals(PathAttributes.EMPTY, attributes);
        assertEquals(bucket.attributes().getVersionId(), attributes.getVersionId());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindLargeUpload() throws Exception {
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path file = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file, Path.Type.upload));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final B2StartLargeFileResponse startResponse = session.getClient().startLargeFileUpload(
                fileid.getVersionId(bucket),
                file.getName(), null, Collections.emptyMap());
        final PathAttributes attributes = new B2AttributesFinderFeature(session, fileid).find(file);
        assertNotSame(PathAttributes.EMPTY, attributes);
        assertEquals(0L, attributes.getSize());
        new B2ReadFeature(session, fileid).read(file, new TransferStatus(), new DisabledConnectionCallback()).close();
        final Path found = new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).find(
                new SimplePathPredicate(file));
        assertTrue(found.getType().contains(Path.Type.upload));
        new B2ReadFeature(session, fileid).read(found, new TransferStatus(), new DisabledConnectionCallback()).close();
        session.getClient().cancelLargeFileUpload(startResponse.getFileId());
    }

    @Test
    public void testChangedFileId() throws Exception {
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final String latestnodeid = test.attributes().getVersionId();
        assertNotNull(latestnodeid);
        // Assume previously seen but changed on server
        final String invalidId = String.valueOf(RandomUtils.nextLong());
        test.attributes().setVersionId(invalidId);
        fileid.cache(test, invalidId);
        final B2AttributesFinderFeature f = new B2AttributesFinderFeature(session, fileid);
        try {
            f.find(test).getVersionId();
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new B2DeleteFeature(session, fileid).delete(new B2ObjectListService(session, fileid).list(bucket, new DisabledListProgressListener()).toList(), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
