package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.AsciiRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@Category(IntegrationTest.class)
public class GoogleStorageStorageClassFeatureTest extends AbstractGoogleStorageTest {

    @Test
    public void testGetClasses() {
        assertArrayEquals(Arrays.asList(
                        "STANDARD",
                        "MULTI_REGIONAL",
                        "REGIONAL",
                        "NEARLINE",
                        "COLDLINE",
                        "ARCHIVE").toArray(),
                new GoogleStorageStorageClassFeature(session).getClasses().toArray());
    }

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path container = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path test = new Path(container, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        final GoogleStorageStorageClassFeature feature = new GoogleStorageStorageClassFeature(session);
        feature.getClass(test);
    }

    @Test
    public void testSetClassBucket() throws Exception {
        final TransferStatus status = new TransferStatus();
        status.setStorageClass("MULTI_REGIONAL");
        final Path test = new GoogleStorageDirectoryFeature(session).mkdir(new Path(new AsciiRandomStringService().random().toLowerCase(Locale.ROOT), EnumSet.of(Path.Type.directory)),
                status);
        final GoogleStorageStorageClassFeature feature = new GoogleStorageStorageClassFeature(session);
        assertEquals("MULTI_REGIONAL", feature.getClass(test));
        feature.setClass(test, "MULTI_REGIONAL");
        assertEquals("MULTI_REGIONAL", feature.getClass(test));
        feature.setClass(test, "NEARLINE");
        assertEquals("NEARLINE", feature.getClass(test));
        feature.setClass(test, "COLDLINE");
        assertEquals("COLDLINE", feature.getClass(test));
        assertEquals("COLDLINE", new GoogleStorageAttributesFinderFeature(session).find(test).getStorageClass());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testSetClassObject() throws Exception {
        final Path bucket = new Path("cyberduck-test-eu", EnumSet.of(Path.Type.directory));
        final Path test = new GoogleStorageTouchFeature(session).touch(new Path(bucket,
                new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)), new TransferStatus());
        final GoogleStorageStorageClassFeature feature = new GoogleStorageStorageClassFeature(session);
        assertEquals("STANDARD", feature.getClass(test));
        feature.setClass(test, "MULTI_REGIONAL");
        assertEquals("MULTI_REGIONAL", feature.getClass(test));
        feature.setClass(test, "NEARLINE");
        assertEquals("NEARLINE", feature.getClass(test));
        feature.setClass(test, "COLDLINE");
        assertEquals("COLDLINE", feature.getClass(test));
        assertEquals("COLDLINE", new GoogleStorageAttributesFinderFeature(session).find(test).getStorageClass());
        new GoogleStorageDeleteFeature(session).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
