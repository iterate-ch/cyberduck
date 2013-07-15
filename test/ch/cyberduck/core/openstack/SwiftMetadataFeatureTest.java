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
import ch.cyberduck.core.Protocol;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftMetadataFeatureTest extends AbstractTestCase {

    @Test
    public void testGetObjectMetadata() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(new Path(container, "test.txt", Path.FILE_TYPE));
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Content-Type"));
        assertEquals("text/plain", metadata.get("Content-Type"));
        assertTrue(metadata.containsKey("X-Object-Meta-Test"));
        assertEquals("Cyberduck", metadata.get("X-Object-Meta-Test"));
        session.close();
    }

    @Test
    public void testGetContainerMetadata() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(container);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("X-Container-Meta-Web-Index"));
        assertEquals("index.html", metadata.get("X-Container-Meta-Web-Index"));
        session.close();
    }

    @Test
    public void testSetMetadata() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(Protocol.SWIFT, "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final Path test = new Path(container, UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.touch(test);
        final String v = UUID.randomUUID().toString();
        new SwiftMetadataFeature(session).setMetadata(test, Collections.<String, String>singletonMap("Test", v));
        final Map<String, String> metadata = new SwiftMetadataFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("X-Object-Meta-Test"));
        assertEquals(v, metadata.get("X-Object-Meta-Test"));
        session.close();
    }
}
