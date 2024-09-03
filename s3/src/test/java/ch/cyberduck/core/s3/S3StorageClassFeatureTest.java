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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.jets3t.service.model.S3Object;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3StorageClassFeatureTest extends AbstractS3Test {

    @Test
    public void testGetClasses() {
        assertArrayEquals(Collections.singletonList(S3Object.STORAGE_CLASS_STANDARD).toArray(),
                new S3StorageClassFeature(new S3Session(new Host(new S3Protocol())), new S3AccessControlListFeature(session)).getClasses().toArray());
        assertArrayEquals(Arrays.asList(S3Object.STORAGE_CLASS_STANDARD, "INTELLIGENT_TIERING",
                        S3Object.STORAGE_CLASS_INFREQUENT_ACCESS, "ONEZONE_IA", S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, S3Object.STORAGE_CLASS_GLACIER, "GLACIER_IR", "DEEP_ARCHIVE").toArray(),
                new S3StorageClassFeature(session, new S3AccessControlListFeature(session)).getClasses().toArray());
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        final S3StorageClassFeature feature = new S3StorageClassFeature(session, new S3AccessControlListFeature(session));
        feature.getClass(test);
    }

    @Test
    public void testSetClassFile() throws Exception {
        final S3StorageClassFeature feature = new S3StorageClassFeature(session, new S3AccessControlListFeature(session));
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        assertNull(S3Object.STORAGE_CLASS_STANDARD, feature.getClass(container));
        final Path test = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        assertEquals(S3Object.STORAGE_CLASS_STANDARD, feature.getClass(test));
        feature.setClass(test, S3Object.STORAGE_CLASS_INFREQUENT_ACCESS);
        assertEquals(S3Object.STORAGE_CLASS_INFREQUENT_ACCESS, feature.getClass(test));
        feature.setClass(test, S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, feature.getClass(test));
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, new S3AttributesFinderFeature(session, new S3AccessControlListFeature(session)).find(test).getStorageClass());
        new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = AccessDeniedException.class)
    public void testSetClassFileInGlacier() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final TransferStatus status = new TransferStatus();
        status.setStorageClass("GLACIER");
        final Path test = new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), status);
        final S3StorageClassFeature feature = new S3StorageClassFeature(session, new S3AccessControlListFeature(session));
        assertEquals(S3Object.STORAGE_CLASS_GLACIER, feature.getClass(test));
        try {
            feature.setClass(test, S3Object.STORAGE_CLASS_STANDARD);
        }
        finally {
            new S3DefaultDeleteFeature(session, new S3AccessControlListFeature(session)).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        }
    }

    @Test
    public void testSetClassPlaceholder() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        final S3StorageClassFeature feature = new S3StorageClassFeature(session, acl);
        assertEquals(S3Object.STORAGE_CLASS_STANDARD, feature.getClass(test));
        feature.setClass(test, S3Object.STORAGE_CLASS_INFREQUENT_ACCESS);
        assertEquals(S3Object.STORAGE_CLASS_INFREQUENT_ACCESS, feature.getClass(test));
        feature.setClass(test, S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        assertEquals(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, feature.getClass(test));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
