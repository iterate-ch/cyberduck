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
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.b2.AbstractB2Test;
import ch.cyberduck.core.b2.B2DeleteFeature;
import ch.cyberduck.core.b2.B2VersionIdProvider;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CachingAttributesFinderFeatureTest extends AbstractB2Test {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final PathCache cache = new PathCache(1);
        final CachingAttributesFinderFeature f = new CachingAttributesFinderFeature(session, cache, new DefaultAttributesFinderFeature(session));
        final Path test = new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        f.find(test);
        // Test cache
        new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return PathAttributes.EMPTY;
            }
        }).find(test);
    }

    @Test
    public void testAttributes() throws Exception {
        final PathCache cache = new PathCache(1);
        final B2VersionIdProvider fileid = new B2VersionIdProvider(session);
        final AttributesFinder f = new CachingAttributesFinderFeature(session, cache, new DefaultAttributesFinderFeature(session));
        final Path bucket = new Path("test-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final String name = new AlphanumericRandomStringService().random();
        final Path file = new Path(bucket, name, EnumSet.of(Path.Type.file));
        session.getFeature(Touch.class).touch(file, new TransferStatus());
        final Attributes lookup = f.find(file);
        assertEquals(0L, lookup.getSize());
        // Test cache
        assertSame(lookup, new CachingAttributesFinderFeature(session, cache, new AttributesFinder() {
            @Override
            public PathAttributes find(final Path file, final ListProgressListener listener) {
                fail("Expected cache hit");
                return PathAttributes.EMPTY;
            }
        }).find(file));
        assertEquals(0L, f.find(file).getSize());
        assertTrue(cache.containsKey(file.getParent()));
        // Test wrong type
        try {
            f.find(new Path(new DefaultHomeFinderService(session).find(), name, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new B2DeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
