package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.CachingFindFeature;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2DirectoryFeature;
import ch.cyberduck.core.b2.B2TouchFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CachingFindFeatureTest extends AbstractB2Test {

    @Test
    public void testFindDefault() throws Exception {
        final PathCache cache = new PathCache(1);
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final String name = new AlphanumericRandomStringService().random();
        final CachingFindFeature f = new CachingFindFeature(cache, new DefaultFindFeature(session));
        assertFalse(f.find(new Path(bucket, name, EnumSet.of(Path.Type.file))));
        final Path test = new B2TouchFeature(session, fileid).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertFalse(f.find(test));
        cache.clear();
        assertTrue(f.find(test));
        // Find without version id set in attributes
        assertTrue(f.find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFind() throws Exception {
        final PathCache cache = new PathCache(1);
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final Path bucket = new B2DirectoryFeature(session, fileid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final String name = new AlphanumericRandomStringService().random();
        final Path test = new B2TouchFeature(session, fileid).touch(
                new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        final CachingFindFeature f = new CachingFindFeature(cache, new DefaultFindFeature(session));
        // Find without version id set in attributes
        assertTrue(f.find(test));
        assertTrue(f.find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        assertEquals(test.attributes(), new CachingAttributesFinderFeature(cache, new DefaultAttributesFinderFeature(session)).find(test));
        assertTrue(new CachingFindFeature(cache, new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return false;
            }
        }).find(test));
        assertTrue(new CachingFindFeature(cache, new Find() {
            @Override
            public boolean find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return false;
            }
        }).find(new Path(test).withAttributes(PathAttributes.EMPTY)));
        // Test wrong type
        assertFalse(f.find(new Path(bucket, test.getName(), EnumSet.of(Path.Type.directory))));
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(test), new DisabledLoginCallback(), new Delete.DisabledCallback());
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(bucket), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
