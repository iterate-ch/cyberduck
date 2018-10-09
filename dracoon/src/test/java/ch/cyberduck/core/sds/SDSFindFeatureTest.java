package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SDSFindFeatureTest extends AbstractSDSTest {

    @Test
    public void testFind() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        assertTrue(new SDSFindFeature(nodeid).find(new DefaultHomeFinderService(session).find(), new DisabledListProgressListener()));
        assertFalse(new SDSFindFeature(nodeid).find(
            new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory)),
            new DisabledListProgressListener()));
        assertFalse(new SDSFindFeature(nodeid).find(
            new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)),
            new DisabledListProgressListener()));
    }

    @Test
    public void testFindRoot() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session).withCache(cache);
        assertTrue(new SDSFindFeature(nodeid).find(new Path("/", EnumSet.of(Path.Type.directory)), new DisabledListProgressListener()));
    }
}
