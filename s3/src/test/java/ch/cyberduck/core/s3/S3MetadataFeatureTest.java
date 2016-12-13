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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
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
public class S3MetadataFeatureTest {

    @Test
    public void testGetMetadataBucket() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(container);
        assertTrue(metadata.isEmpty());
        session.close();
    }

    @Test
    public void testGetMetadataFile() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString() + ".txt", EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3WriteFeature(session)).touch(test, new TransferStatus());
        final Map<String, String> metadata = new S3MetadataFeature(session).getMetadata(test);
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertFalse(metadata.containsKey(Constants.KEY_FOR_USER_METADATA));
        assertFalse(metadata.containsKey(Constants.KEY_FOR_SERVICE_METADATA));
        assertFalse(metadata.containsKey(Constants.KEY_FOR_COMPLETE_METADATA));
        session.close();
    }

    @Test
    public void testSetMetadataFileLeaveOtherFeatures() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3WriteFeature(session)).touch(test, new TransferStatus());
        final String v = UUID.randomUUID().toString();
        final S3StorageClassFeature storage = new S3StorageClassFeature(session);
        storage.setClass(test, S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, storage.getClass(test));

        final S3EncryptionFeature encryption = new S3EncryptionFeature(session);
        encryption.setEncryption(test, S3EncryptionFeature.SSE_AES256);
        assertEquals("AES256", encryption.getEncryption(test).algorithm);

        final S3MetadataFeature feature = new S3MetadataFeature(session);
        feature.setMetadata(test, Collections.singletonMap("Test", v));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("test"));
        assertEquals(v, metadata.get("test"));

        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, storage.getClass(test));
        assertEquals("AES256", encryption.getEncryption(test).algorithm);

        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testSetDuplicateHeaderDifferentCapitalization() throws Exception {
        final S3Session session = new S3Session(
                new Host(new S3Protocol(), new S3Protocol().getDefaultHostname(),
                        new Credentials(
                                System.getProperties().getProperty("s3.key"), System.getProperties().getProperty("s3.secret")
                        )));
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3WriteFeature(session)).touch(test, new TransferStatus());
        final S3MetadataFeature feature = new S3MetadataFeature(session);
        assertTrue(feature.getMetadata(test).containsKey("Content-Type"));
        feature.setMetadata(test, Collections.singletonMap("Content-type", "text/plain"));
        final Map<String, String> metadata = feature.getMetadata(test);
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("text/plain", metadata.get("Content-Type"));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
