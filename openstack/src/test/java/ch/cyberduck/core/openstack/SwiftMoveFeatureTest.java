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
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.InteroperabilityException;
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
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(test);
        assertTrue(new SwiftFindFeature(session).find(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("DFW");
        new SwiftMoveFeature(session).move(test, target, false, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test(expected = InteroperabilityException.class)
    public void testMoveBetweenRegions() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path sourceContainer = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        sourceContainer.attributes().setRegion("IAD");
        final String name = UUID.randomUUID().toString();
        final Path test = new Path(sourceContainer, name, EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(test);
        assertTrue(new SwiftFindFeature(session).find(test));
        final Path targetContainer = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        targetContainer.attributes().setRegion("DFW");
        final Path target = new Path(targetContainer, name, EnumSet.of(Path.Type.file));
        new SwiftMoveFeature(session).move(test, target, false, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test
    public void testMoveOverride() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(test);
        final Path target = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        target.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(target);
        new SwiftMoveFeature(session).move(test, target, false, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        assertFalse(new SwiftFindFeature(session).find(test));
        assertTrue(new SwiftFindFeature(session).find(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginCallback(), new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                System.getProperties().getProperty("rackspace.key"), System.getProperties().getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file));
        test.attributes().setRegion("DFW");
        new SwiftMoveFeature(session).move(test, new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.file)), false, new Delete.Callback() {
            @Override
            public void delete(final Path file) {
            }
        });
    }

    @Test
    public void testSupport() throws Exception {
        assertFalse(new SwiftMoveFeature(null).isSupported(new Path("/c", EnumSet.of(Path.Type.directory))));
        assertTrue(new SwiftMoveFeature(null).isSupported(new Path("/c/f", EnumSet.of(Path.Type.directory))));
    }
}
