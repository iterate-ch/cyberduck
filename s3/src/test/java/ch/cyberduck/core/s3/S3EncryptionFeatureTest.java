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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@Category(IntegrationTest.class)
public class S3EncryptionFeatureTest extends AbstractS3Test {

    @Test
    public void testGetAlgorithms() throws Exception {
        assertEquals(2, new S3EncryptionFeature(session, new S3AccessControlListFeature(session)).getKeys(
                new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledLoginCallback()).size());
    }

    @Test
    public void testSetEncryptionAES256() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        new S3TouchFeature(session, acl).touch(test, new TransferStatus());
        final S3EncryptionFeature feature = new S3EncryptionFeature(session, acl);
        feature.setEncryption(test, S3EncryptionFeature.SSE_AES256);
        final Encryption.Algorithm value = feature.getEncryption(test);
        assertEquals("AES256", value.algorithm);
        assertNull(value.key);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetEncryptionAES256Placeholder() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final S3EncryptionFeature feature = new S3EncryptionFeature(session, acl);
        feature.setEncryption(test, S3EncryptionFeature.SSE_AES256);
        final Encryption.Algorithm value = feature.getEncryption(test);
        assertEquals("AES256", value.algorithm);
        assertNull(value.key);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
