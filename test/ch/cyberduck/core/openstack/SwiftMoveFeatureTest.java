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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftMoveFeatureTest extends AbstractTestCase {

    @Test
    public void testMove() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SwiftTouchFeature(session).touch(test);
        assertTrue(session.exists(test));
        final Path target = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        new SwiftMoveFeature(session).move(test, target);
        assertFalse(session.exists(test));
        assertTrue(session.exists(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginController());
    }

    @Test
    public void testMoveOverride() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(test);
        final Path target = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        target.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(target);
        new SwiftMoveFeature(session).move(test, target);
        assertFalse(session.exists(test));
        assertTrue(session.exists(target));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(target), new DisabledLoginController());
    }

    @Test(expected = NotfoundException.class)
    public void testMoveNotFound() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        test.attributes().setRegion("DFW");
        new SwiftMoveFeature(session).move(test, new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE));
    }
}
