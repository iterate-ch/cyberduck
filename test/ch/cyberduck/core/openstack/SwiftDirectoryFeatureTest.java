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

import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @version $Id$
 */
public class SwiftDirectoryFeatureTest extends AbstractTestCase {

    @Test
    public void testCreateContainer() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path(UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        new SwiftDirectoryFeature(session).mkdir(container, null);
        assertTrue(new SwiftFindFeature(session).find(container));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(container), new DisabledLoginController());
        assertFalse(new SwiftFindFeature(session).find(container));
    }

    @Test
    public void testCreatePlaceholder() throws Exception {
        final Host host = new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com", new Credentials(
                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
        ));
        final SwiftSession session = new SwiftSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("/test.cyberduck.ch", Path.VOLUME_TYPE | Path.DIRECTORY_TYPE);
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        new SwiftDirectoryFeature(session).mkdir(placeholder, null);
        Thread.sleep(2000L);
        assertTrue(new SwiftFindFeature(session).find(placeholder));
        new SwiftDeleteFeature(session).delete(Collections.<Path>singletonList(placeholder), new DisabledLoginController());
        assertFalse(new SwiftFindFeature(session).find(placeholder));
    }
}
