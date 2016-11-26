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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Category(IntegrationTest.class)
public class SwiftMoveFeatureTest {

    @Test
    public void testMove() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(test);
        assertTrue(new SwiftFindFeature(session).find(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("DFW");
        new SwiftMoveFeature(session).move(test, target, false, new Delete.DisabledCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test
    public void testMoveOverride() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(test);
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(target);
        new SwiftMoveFeature(session).move(test, target, false, new Delete.DisabledCallback());
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host).withAccountPreload(false).withCdnPreload(false).withContainerPreload(false);
        session.open(new DisabledHostKeyCallback());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("DFW");
        new SwiftMoveFeature(session).move(test, new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), false, new Delete.DisabledCallback());
    }

    @Test
    public void testSupport() throws Exception {
        assertFalse(new SwiftMoveFeature(null).isSupported(new Path("/c", EnumSet.of(Path.Type.directory))));
        assertTrue(new SwiftMoveFeature(null).isSupported(new Path("/c/f", EnumSet.of(Path.Type.directory))));
    }
}
