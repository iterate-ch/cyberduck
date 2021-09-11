package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.CachingAttributesFinderFeature;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.s3.AbstractS3Test;
import ch.cyberduck.core.s3.S3DefaultDeleteFeature;
import ch.cyberduck.core.s3.S3TouchFeature;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class CachingAttributesFinderFeatureTest extends AbstractS3Test {

    @Test
    public void testAttributes() throws Exception {
        final PathCache cache = new PathCache(1);
        final AttributesFinder f = new CachingAttributesFinderFeature(cache, new DefaultAttributesFinderFeature(session));
        final String name = new AlphanumericRandomStringService().random();
        final Path bucket = new Path("test-us-east-1-cyberduck", EnumSet.of(Path.Type.volume, Path.Type.directory));
        final Path file = new S3TouchFeature(session).touch(new Path(bucket, name, EnumSet.of(Path.Type.file)), new TransferStatus());
        assertNotSame(file.attributes(), f.find(file));
        assertEquals(0L, f.find(file).getSize());
        // Test cache
        assertEquals(0L, f.find(file).getSize());
        assertTrue(cache.containsKey(file.getParent()));
        // Test wrong type
        try {
            f.find(new Path(bucket, name, EnumSet.of(Path.Type.directory)));
            fail();
        }
        catch(NotfoundException e) {
            // Expected
        }
        new S3DefaultDeleteFeature(session).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }
}
