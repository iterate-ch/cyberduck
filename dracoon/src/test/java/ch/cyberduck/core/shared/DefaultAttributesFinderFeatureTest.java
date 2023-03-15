package ch.cyberduck.core.shared;

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
import ch.cyberduck.core.Attributes;
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.AbstractSDSTest;
import ch.cyberduck.core.sds.SDSDeleteFeature;
import ch.cyberduck.core.sds.SDSDirectoryFeature;
import ch.cyberduck.core.sds.SDSNodeIdProvider;
import ch.cyberduck.core.sds.SDSTouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DefaultAttributesFinderFeatureTest extends AbstractSDSTest {

    @Test(expected = NotfoundException.class)
    public void testNotFound() throws Exception {
        final Path room = new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume));
        new DefaultAttributesFinderFeature(session).find(new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file)));
    }

    @Test
    public void testAttributes() throws Exception {
        final PathCache cache = new PathCache(1);
        final AttributesFinder f = new CachingAttributesFinderFeature(cache, new DefaultAttributesFinderFeature(session));
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(file, new TransferStatus());
        final Attributes attributes = f.find(file);
        assertEquals(0L, attributes.getSize());
        // Test cache
        assertEquals(0L, f.find(file).getSize());
        assertTrue(cache.containsKey(file.getParent()));
        // Test wrong type
        try {
            f.find(new Path(room, "test", EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
