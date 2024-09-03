package ch.cyberduck.core.s3;

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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;
import ch.cyberduck.ui.browser.SearchFilter;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class S3SearchFeatureTest extends AbstractS3Test {

    @Test
    public void testSearchInBucket() throws Exception {
        final String name = new AlphanumericRandomStringService().random();
        final Path root = new Path("/", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final Path bucket = new Path(root, "test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final Path file = new S3TouchFeature(session, acl).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final S3SearchFeature feature = new S3SearchFeature(session, acl);
        assertNotNull(feature.search(bucket, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        assertNotNull(feature.search(bucket, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        assertNotNull(feature.search(bucket, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        assertNotNull(feature.search(root, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        final Path subdir = new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory));
        assertNull(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).find(new SimplePathPredicate(file)));
        new S3DefaultDeleteFeature(session, acl).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSearchInDirectory() throws Exception {
        final Path bucket = new Path("test-eu-central-1-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final S3AccessControlListFeature acl = new S3AccessControlListFeature(session);
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(bucket, name, EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(file, new TransferStatus());
        final S3SearchFeature feature = new S3SearchFeature(session, acl);
        assertTrue(feature.search(bucket, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(bucket, new SearchFilter(StringUtils.upperCase(name)), new DisabledListProgressListener()).contains(file));
        assertTrue(feature.search(bucket, new SearchFilter(StringUtils.substring(name, 2)), new DisabledListProgressListener()).contains(file));
        // Glob pattern
        assertTrue(feature.search(bucket, new SearchFilter(String.format("*%s", StringUtils.substring(name, 2))), new DisabledListProgressListener()).contains(file));
        {
            final AttributedList<Path> result = feature.search(bucket, new SearchFilter(StringUtils.substring(name, 0, name.length() - 2)), new DisabledListProgressListener());
            assertTrue(result.contains(file));
        }
        assertFalse(feature.search(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        final Path subdir = new S3DirectoryFeature(session, new S3WriteFeature(session, acl), acl).mkdir(new Path(bucket, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(feature.search(bucket, new SearchFilter(new AlphanumericRandomStringService().random()), new DisabledListProgressListener()).isEmpty());
        assertFalse(feature.search(subdir, new SearchFilter(name), new DisabledListProgressListener()).contains(file));
        final Path filesubdir = new S3TouchFeature(session, acl).touch(new Path(subdir, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        {
            final AttributedList<Path> result = feature.search(bucket, new SearchFilter(filesubdir.getName()), new DisabledListProgressListener());
            assertNotNull(result.find(new SimplePathPredicate(filesubdir)));
            assertEquals(subdir, result.find(new SimplePathPredicate(filesubdir)).getParent());
        }
        {
            final AttributedList<Path> result = feature.search(subdir, new SearchFilter(filesubdir.getName()), new DisabledListProgressListener());
            assertNotNull(result.find(new SimplePathPredicate(filesubdir)));
            assertEquals(subdir, result.find(new SimplePathPredicate(filesubdir)).getParent());
        }
        new S3DefaultDeleteFeature(session, acl).delete(Arrays.asList(file, filesubdir, subdir), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

}
