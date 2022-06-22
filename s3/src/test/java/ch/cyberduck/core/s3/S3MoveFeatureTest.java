package ch.cyberduck.core.s3;

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

import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.apache.commons.lang3.RandomUtils;
import org.jets3t.service.model.StorageObject;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MoveFeatureTest extends AbstractS3Test {

    @Test
    public void testMove() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        assertNull(new S3TouchFeature(session, acl).touch(test, new TransferStatus().withMime("text/plain")).attributes().getVersionId());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final Path renamed = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertTrue(new S3FindFeature(session, acl).find(renamed));
        final Map<String, String> metadata = new S3MetadataFeature(session, acl).getMetadata(renamed);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveVirtualHost() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        final Path test = new S3TouchFeature(virtualhost, acl).touch(new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertTrue(new S3FindFeature(virtualhost, acl).find(test));
        final Path renamed = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(virtualhost, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(virtualhost, acl).find(test));
        assertTrue(new S3FindFeature(virtualhost, acl).find(renamed));
        new S3DefaultDeleteFeature(virtualhost).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMovePlaceholderVirtualHost() throws Exception {
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        final Path test = new S3DirectoryFeature(virtualhost, new S3WriteFeature(virtualhost, acl), acl).mkdir(new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new S3FindFeature(virtualhost, acl).find(test));
        final Path renamed = new Path(new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory));
        new S3MoveFeature(virtualhost, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new DefaultFindFeature(virtualhost).find(test));
        assertTrue(new S3FindFeature(virtualhost, acl).find(renamed));
        new S3DefaultDeleteFeature(virtualhost).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveVersioned() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        assertNotNull(new S3TouchFeature(session, acl).touch(test, new TransferStatus().withMime("text/plain")).attributes().getVersionId());
        assertTrue(new S3FindFeature(session, acl).find(test));
        // Write some data to add a new version
        final S3WriteFeature feature = new S3WriteFeature(session, acl);
        final byte[] content = RandomUtils.nextBytes(10);
        final TransferStatus status = new TransferStatus().withMime("text/plain");
        status.setLength(content.length);
        status.setChecksum(new SHA256ChecksumCompute().compute(new ByteArrayInputStream(content), status));
        final HttpResponseOutputStream<StorageObject> out = feature.write(test, status, new DisabledConnectionCallback());
        new StreamCopier(status, status).transfer(new ByteArrayInputStream(content), out);
        out.close();
        // Get new path with updated version id
        final AttributedList<Path> list = new S3ListService(session, acl).list(container, new DisabledListProgressListener());
        for(Path path : list) {
            if(new SimplePathPredicate(test).test(path)) {
                test = path;
                break;
            }
        }
        final Path renamed = new Path(container, String.format("%s-renamed", test.getName()), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertTrue(new S3FindFeature(session, acl).find(renamed));
        // Ensure that the latest version of the source file is a delete marker
        for(Path path : new S3ListService(session, acl).list(container, new DisabledListProgressListener())) {
            if(new SimplePathPredicate(test).test(path)) {
                assertTrue(path.attributes().isDuplicate());
                assertTrue(new S3AttributesFinderFeature(session, acl).find(path).isDuplicate());
                assertTrue(new S3AttributesFinderFeature(session, acl).find(path).isDuplicate());
                break;
            }
        }
        final Map<String, String> metadata = new S3MetadataFeature(session, acl).getMetadata(renamed);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveWithDelimiter() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(placeholder, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3TouchFeature(session, acl).touch(test, new TransferStatus());
        final Path renamed = new Path(placeholder, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertTrue(new S3FindFeature(session, acl).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSupport() {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new S3MoveFeature(session, new S3AccessControlListFeature(session)).isSupported(c, c));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new S3MoveFeature(session, new S3AccessControlListFeature(session)).isSupported(cf, cf));
    }

    @Test
    public void testMoveWithServerSideEncryptionBucketPolicy() throws Exception {
        final Path container = new Path("sse-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3TouchFeature touch = new S3TouchFeature(session, acl);
        final TransferStatus status = new TransferStatus();
        status.setEncryption(S3EncryptionFeature.SSE_AES256);
        touch.touch(test, status);
        assertTrue(new S3FindFeature(session, acl).find(test));
        final Path renamed = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertTrue(new S3FindFeature(session, acl).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveBucketDisabledAcl() throws Exception {
        final Path container = new Path("test-eu-central-1-acl-disabled", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final S3TouchFeature touch = new S3TouchFeature(session, acl);
        touch.touch(test, new TransferStatus());
        assertTrue(new S3FindFeature(session, acl).find(test));
        final Path renamed = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session, acl).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertTrue(new S3FindFeature(session, acl).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
