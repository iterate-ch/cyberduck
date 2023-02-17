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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3DefaultDeleteFeatureTest extends AbstractS3Test {

    @Test
    public void testDeleteFile() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
    }

    @Test
    public void testDeleteFileBackslash() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, String.format("%s\\%s", new AlphanumericRandomStringService().random(),
                new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.file));
        new S3TouchFeature(session, new S3AccessControlListFeature(session)).touch(test, new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, new S3AccessControlListFeature(session)).find(test));
    }

    @Test
    public void testDeleteFileVirtualHost() throws Exception {
        final Path test = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(virtualhost);
        new S3TouchFeature(virtualhost, acl).touch(test, new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(virtualhost, acl).find(test));
        new S3DefaultDeleteFeature(virtualhost).delete(Arrays.asList(test, test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(virtualhost, acl).find(test));
    }

    @Test
    public void testDeletePlaceholder() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(container,
                String.format("%s %s", new AlphanumericRandomStringService().random(), new AlphanumericRandomStringService().random()), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertFalse(new DefaultFindFeature(session).find(test));
        assertNull(new S3VersionedObjectListService(session, acl).list(container, new DisabledListProgressListener()).find(new SimplePathPredicate(test)));
    }

    @Test
    public void testDeleteVersionedPlaceholder() throws Exception {
        final Path container = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final String name = new AlphanumericRandomStringService().random();
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path test = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(container, name, EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, acl).find(test));
        assertFalse(new DefaultFindFeature(session).find(test));
        assertFalse(new S3FindFeature(session, acl).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertFalse(new DefaultFindFeature(session).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
    }

    @Ignore
    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundKey() throws Exception {
        final Path container = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucket() throws Exception {
        final Path container = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testDeleteNotFoundBucketDnsNameCompatible() throws Exception {
        final Path container = new Path(new AlphanumericRandomStringService().random().toLowerCase(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(container), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
