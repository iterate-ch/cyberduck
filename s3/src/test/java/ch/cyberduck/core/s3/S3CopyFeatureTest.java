package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3CopyFeatureTest extends AbstractS3Test {

    @Test
    public void testCopyFileZeroLength() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        test.attributes().setSize(0L);
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3TouchFeature(session, acl).touch(test, new TransferStatus());
        final Path copy = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3CopyFeature(session, acl).copy(test, copy, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertNull(copy.attributes().getVersionId());
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(copy));
        ;
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFile() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        status.setMetadata(Collections.singletonMap("cyberduck", "m"));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final Path copy = new S3CopyFeature(session, acl).copy(test,
                new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertNull(copy.attributes().getVersionId());
        assertEquals("m", new S3MetadataFeature(session, acl).getMetadata(copy).get("cyberduck"));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(copy));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileToBucketWithOwnerEnforced() throws Exception {
        final Path bucket = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final TransferStatus status = new TransferStatus();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(bucket, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final Path copy = new S3CopyFeature(session, acl).copy(test,
                new Path(new Path("test-eu-central-1-cyberduck-ownerenforced", EnumSet.of(Path.Type.directory, Path.Type.volume)), new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(session, acl).find(test));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(copy));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileVersionedBucket() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final TransferStatus status = new TransferStatus();
        status.setMetadata(Collections.singletonMap("cyberduck", "m"));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        assertNotNull(test.attributes().getVersionId());
        final Path copy = new S3CopyFeature(session, acl).copy(test,
                new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertNotNull(copy.attributes().getVersionId());
        assertNotEquals("", copy.attributes().getVersionId());
        assertNotEquals(test.attributes().getVersionId(), copy.attributes().getVersionId());
        assertEquals("m", new S3MetadataFeature(session, acl).getMetadata(copy).get("cyberduck"));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(session, acl).find(copy));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testCopyFileVirtualHostBucket() throws Exception {
        final TransferStatus status = new TransferStatus();
        status.setMetadata(Collections.singletonMap("cyberduck", "m"));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        final Path test = new S3TouchFeature(virtualhost, acl).touch(new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final Path copy = new S3CopyFeature(virtualhost, acl).copy(test,
                new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
        assertTrue(new S3FindFeature(virtualhost, acl).find(test));
        assertEquals("m", new S3MetadataFeature(virtualhost, acl).getMetadata(copy).get("cyberduck"));
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertTrue(new S3FindFeature(virtualhost, acl).find(copy));
        new S3DefaultDeleteFeature(virtualhost, acl).delete(Collections.singletonList(copy), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
