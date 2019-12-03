package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
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
public class SwiftMoveFeatureTest extends AbstractSwiftTest {

    @Test
    public void testMove() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(test, new TransferStatus());
        assertTrue(new SwiftFindFeature(session).find(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("IAD");
        new SwiftMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(test, new TransferStatus());
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("IAD");
        new SwiftTouchFeature(session, new SwiftRegionService(session)).touch(target, new TransferStatus());
        new SwiftMoveFeature(session).move(test, target, new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Path container = new Path("test-iad-cyberduck", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("IAD");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("IAD");
        new SwiftMoveFeature(session).move(test, new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), new TransferStatus(), new Delete.DisabledCallback(), new DisabledConnectionCallback());
    }

    @Test
    public void testSupport() {
        final Path c = new Path("/c", EnumSet.of(Path.Type.directory));
        assertFalse(new SwiftMoveFeature(null).isSupported(c, c));
        final Path cf = new Path("/c/f", EnumSet.of(Path.Type.directory));
        assertTrue(new SwiftMoveFeature(null).isSupported(cf, cf));
    }
}
