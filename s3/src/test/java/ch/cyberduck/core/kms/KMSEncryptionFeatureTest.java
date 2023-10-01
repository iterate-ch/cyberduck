package ch.cyberduck.core.kms;

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
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3LocationFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.ssl.DefaultX509KeyManager;
import ch.cyberduck.core.ssl.DisabledX509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class KMSEncryptionFeatureTest extends AbstractS3Test {

    @Test
    public void testSetEncryptionKMSDefaultKeySignatureVersionV4() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3TouchFeature(session, acl).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final S3EncryptionFeature feature = new S3EncryptionFeature(session, acl);
        feature.setEncryption(test, KMSEncryptionFeature.SSE_KMS_DEFAULT);
        final Encryption.Algorithm value = feature.getEncryption(test);
        assertEquals("aws:kms", value.algorithm);
        assertNotNull(value.key);
        final PathAttributes attr = new S3AttributesFinderFeature(session, acl).find(test);
        assertNotEquals(Checksum.NONE, attr.getChecksum());
        assertNotNull(attr.getETag());
        assertNotEquals(Checksum.NONE, Checksum.parse(attr.getETag()));
        // The ETag will only be the MD5 of the object data when the object is stored as plaintext or encrypted using SSE-S3.
        // If the object is encrypted using another method (such as SSE-C or SSE-KMS) the ETag is not the MD5 of the object data.
        assertNotEquals("d41d8cd98f00b204e9800998ecf8427e", Checksum.parse(attr.getETag()).hash);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetEncryptionKMSCustomKeySignatureVersionV4() throws Exception {
        final Path container = new Path("test-eu-west-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final S3EncryptionFeature feature = new S3EncryptionFeature(session, new S3AccessControlListFeature(session));
        feature.setEncryption(test, new Encryption.Algorithm("aws:kms", "arn:aws:kms:eu-west-1:930717317329:key/015fa0af-f95e-483e-8fb6-abffb46fb783"));
        final Encryption.Algorithm value = feature.getEncryption(test);
        assertEquals("aws:kms", value.algorithm);
        assertEquals("arn:aws:kms:eu-west-1:930717317329:key/015fa0af-f95e-483e-8fb6-abffb46fb783", value.key);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testGetKeys_eu_west_1() throws Exception {
        final KMSEncryptionFeature kms = new KMSEncryptionFeature(session, new S3LocationFeature(session), new S3AccessControlListFeature(session), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        assertFalse(kms.getKeys(new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledLoginCallback()).isEmpty());
    }

    @Test
    public void testGetKeys_ap_southeast_2() throws Exception {
        final KMSEncryptionFeature kms = new KMSEncryptionFeature(session, new S3LocationFeature(session), new S3AccessControlListFeature(session), new DisabledX509TrustManager(), new DefaultX509KeyManager());
        final Set<Encryption.Algorithm> keys = kms.getKeys(new Path("test-ap-southeast-2-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory)), new DisabledLoginCallback());
        assertTrue(keys.contains(Encryption.Algorithm.NONE));
        assertTrue(keys.contains(S3EncryptionFeature.SSE_AES256));
        assertEquals(2, keys.size());
    }
}
