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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.shared.DefaultHomeFinderService;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SDSFindFeatureTest extends AbstractSDSTest {

    @Test
    public void testFindDirectory() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path folder = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(new DefaultHomeFinderService(session).find(), new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory)), new TransferStatus());
        assertTrue(new SDSFindFeature(session, nodeid).find(folder));
        assertFalse(new SDSFindFeature(session, nodeid).find(new Path(folder.getAbsolute(), EnumSet.of(Path.Type.file))));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(folder), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFindFile() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        final Path room = new SDSDirectoryFeature(session, nodeid).mkdir(
                new Path(new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.directory, Path.Type.volume)), new TransferStatus());
        final Path file = new Path(room, new AlphanumericRandomStringService().random(), EnumSet.of(Path.Type.file));
        new SDSTouchFeature(session, nodeid).touch(file, new TransferStatus());
        assertTrue(new SDSFindFeature(session, nodeid).find(file));
        assertFalse(new SDSFindFeature(session, nodeid).find(new Path(file.getAbsolute(), EnumSet.of(Path.Type.directory))));
        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(room), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testFind() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        assertTrue(new SDSFindFeature(session, nodeid).find(new DefaultHomeFinderService(session).find()));
        assertFalse(new SDSFindFeature(session, nodeid).find(
            new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory))
        ));
        assertFalse(new SDSFindFeature(session, nodeid).find(
            new Path(new DefaultHomeFinderService(session).find(), UUID.randomUUID().toString(), EnumSet.of(Path.Type.file))
        ));
    }

    @Test
    public void testFindRoot() throws Exception {
        final SDSNodeIdProvider nodeid = new SDSNodeIdProvider(session);
        assertTrue(new SDSFindFeature(session, nodeid).find(new Path("/", EnumSet.of(Path.Type.directory))));
    }
}
