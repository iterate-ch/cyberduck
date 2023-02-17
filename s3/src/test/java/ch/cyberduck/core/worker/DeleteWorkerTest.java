package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledPasswordCallback;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3AccessControlListFeature;
import ch.cyberduck.core.s3.S3AttributesFinderFeature;
import ch.cyberduck.core.s3.S3DirectoryFeature;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3MultipleDeleteFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DeleteWorkerTest extends AbstractS3Test {

    @Test
    public void testDelete() throws Exception {
        final Path home = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path folder = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(folder));
        final Path file = new S3TouchFeature(session, acl).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertNull(file.attributes().getVersionId());
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(folder), new DisabledProgressListener()).run(session);
        assertFalse(new S3FindFeature(session, acl).find(file));
    }

    @Test
    public void testDeleteVersioning() throws Exception {
        final Path home = new Path("versioning-test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path folder = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(
                new Path(home, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus().withLength(0L));
        assertTrue(new S3FindFeature(session, acl).find(folder));
        final Path file = new S3TouchFeature(session, acl).touch(
                new Path(folder, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus().withLength(0L));
        assertNotNull(file.attributes().getVersionId());
        assertTrue(new S3FindFeature(session, acl).find(file));
        new DeleteWorker(new DisabledLoginCallback(), Collections.singletonList(folder), new DisabledProgressListener()).run(session);
        // Find delete marker
        assertTrue(new S3FindFeature(session, acl).find(file));
        assertTrue(new S3AttributesFinderFeature(session, acl).find(file).isDuplicate());
        assertFalse(new S3FindFeature(session, acl).find(new Path(file).withAttributes(PathAttributes.EMPTY)));
        assertTrue(new DefaultFindFeature(session).find(file));
        assertFalse(new DefaultFindFeature(session).find(new Path(file).withAttributes(PathAttributes.EMPTY)));
        new S3MultipleDeleteFeature(session, acl).delete(Arrays.asList(file, folder), new DisabledPasswordCallback(), new Delete.DisabledCallback());
        assertFalse(new S3FindFeature(session, acl).find(folder));
    }
}
