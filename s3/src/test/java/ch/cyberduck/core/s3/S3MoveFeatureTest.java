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
import ch.cyberduck.core.DisabledConnectionCallback;
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

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MoveFeatureTest extends AbstractS3Test {

    @Test
    public void testMove() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        assertNull(new S3TouchFeature(session).touch(test, new TransferStatus().withMime("text/plain")).attributes().getVersionId());
        assertTrue(new S3FindFeature(session).find(test));
        final Path renamed = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session).find(test));
        assertTrue(new S3FindFeature(session).find(renamed));
        final Map<String, String> metadata = new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(renamed);
        assertFalse(metadata.isEmpty());
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveWithDelimiter() throws Exception {
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path placeholder = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.directory));
        final Path test = new Path(placeholder, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session).touch(test, new TransferStatus());
        final Path renamed = new Path(placeholder, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session).find(test));
        assertTrue(new S3FindFeature(session).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSupport() throws Exception {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new S3MoveFeature(null).isSupported(c, c));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new S3MoveFeature(null).isSupported(cf, cf));
    }

    @Test
    public void testMoveWithServerSideEncryptionBucketPolicy() throws Exception {
        final Path container = new Path("sse-test-us-east-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3TouchFeature touch = new S3TouchFeature(session);
        final TransferStatus status = new TransferStatus();
        status.setEncryption(S3EncryptionFeature.SSE_AES256);
        touch.touch(test, status);
        assertTrue(new S3FindFeature(session).find(test));
        final Path renamed = new Path(container, new AsciiRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3MoveFeature(session).move(test, renamed, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new S3FindFeature(session).find(test));
        assertTrue(new S3FindFeature(session).find(renamed));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(renamed), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
