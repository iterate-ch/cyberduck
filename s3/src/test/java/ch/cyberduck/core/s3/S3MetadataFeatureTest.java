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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.Constants;
import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3MetadataFeatureTest extends AbstractS3Test {

    @Test
    public void testGetMetadataBucket() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Map<String, String> metadata = new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(container);
        assertTrue(metadata.isEmpty());
    }

    @Test
    public void testGetMetadataFile() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus()
                .withMetadata(Collections.singletonMap("app", "cyberduck"))
                .withMime("text/plain"));
        final Map<String, String> metadata = new S3MetadataFeature(session, new S3AccessControlListFeature(session)).getMetadata(test);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("app"));
        assertEquals("cyberduck", metadata.get("app"));
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertFalse(metadata.containsKey(Constants.KEY_FOR_USER_METADATA));
        assertFalse(metadata.containsKey(Constants.KEY_FOR_SERVICE_METADATA));
        assertFalse(metadata.containsKey(Constants.KEY_FOR_COMPLETE_METADATA));
    }

    @Test
    public void testSetMetadataFileLeaveOtherFeatures() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final S3MetadataFeature feature = new S3MetadataFeature(session, new S3AccessControlListFeature(session));
        final Map<String, String> reference = feature.getMetadata(test);

        final String v = UUID.randomUUID().toString();

        final S3StorageClassFeature storage = new S3StorageClassFeature(session, new S3AccessControlListFeature(session));
        storage.setClass(test, S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, storage.getClass(test));
        assertEquals(reference, feature.getMetadata(test));

        final S3EncryptionFeature encryption = new S3EncryptionFeature(session, new S3AccessControlListFeature(session));
        encryption.setEncryption(test, S3EncryptionFeature.SSE_AES256);
        assertEquals("AES256", encryption.getEncryption(test).algorithm);
        assertEquals(reference, feature.getMetadata(test));

        feature.setMetadata(test, Collections.singletonMap("Test", v));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("test"));
        assertEquals(v, metadata.get("test"));
        assertEquals(reference.size() + 1, metadata.size());

        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, storage.getClass(test));
        assertEquals("AES256", encryption.getEncryption(test).algorithm);

        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetDuplicateHeaderDifferentCapitalization() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus());
        final S3MetadataFeature feature = new S3MetadataFeature(session, new S3AccessControlListFeature(session));
        assertTrue(feature.getMetadata(test).containsKey("Content-Type"));
        feature.setMetadata(test, Collections.singletonMap("Content-type", "text/plain"));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
