package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3DisabledMultipartService;
import ch.cyberduck.core.s3.S3FindFeature;
import ch.cyberduck.core.s3.S3HomeFinderService;
import ch.cyberduck.core.s3.S3WriteFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class GoogleStorageDirectoryFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testMakeBucket() throws Exception {
        final Path test = new Path(new S3HomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new GoogleStorageDirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(test, null, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMakeDirectory() throws Exception {
        final Path bucket = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path test = new GoogleStorageDirectoryFeature(session, new S3WriteFeature(session, new S3DisabledMultipartService())).mkdir(new Path(bucket,
                UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)), null, new TransferStatus());
        assertTrue(new S3FindFeature(session).find(test));
        assertTrue(new DefaultFindFeature(session).find(test));
        new S3DefaultDeleteFeature(session).delete(Collections.<Path>singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
