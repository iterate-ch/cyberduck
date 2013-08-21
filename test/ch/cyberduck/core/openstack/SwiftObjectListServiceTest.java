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
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class SwiftObjectListServiceTest extends AbstractTestCase {

    @Test
    public void testList() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final List<Path> list = new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            assertEquals("DFW", p.attributes().getRegion());
            if(p.attributes().isFile()) {
                assertNotNull(p.attributes().getModificationDate());
                assertNotNull(p.attributes().getSize());
                assertNotNull(p.attributes().getChecksum());
                assertNotNull(p.attributes().getETag());
            }
            else if(p.attributes().isDirectory()) {
                assertTrue(p.attributes().isPlaceholder());
            }
            else {
                fail();
            }
        }
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("notfound.cyberduck.ch", Path.VOLUME_TYPE);
        new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test
    public void testPlaceholderSameObject() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path container = new Path("test.cyberduck.ch", Path.VOLUME_TYPE);
        container.attributes().setRegion("DFW");
        final String basename = UUID.randomUUID().toString();
        final String childname = String.format("%s/%s", basename, UUID.randomUUID().toString());
        final Path base = new Path(container, basename, Path.FILE_TYPE);
        base.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(base);
        final Path child = new Path(container, childname, Path.FILE_TYPE);
        child.attributes().setRegion("DFW");
        new SwiftTouchFeature(session).touch(child);
        final AttributedList<Path> list = new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
        assertTrue(list.contains(base));
        assertEquals(Path.FILE_TYPE, list.get(base.getReference()).attributes().getType());
        final Path placeholder = new Path(container, basename, Path.DIRECTORY_TYPE);
        placeholder.attributes().setRegion("DFW");
        assertTrue(list.contains(placeholder));
        assertEquals(Path.DIRECTORY_TYPE, list.get(placeholder.getReference()).attributes().getType());
        new SwiftDeleteFeature(session).delete(Arrays.asList(base, child), new DisabledLoginController());
    }
}
