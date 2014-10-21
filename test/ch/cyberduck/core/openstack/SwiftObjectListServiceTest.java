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
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import java.util.Arrays;
import java.util.EnumSet;
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
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("DFW");
        final List<Path> list = new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
        for(Path p : list) {
            assertEquals(container, p.getParent());
            if(p.isFile()) {
                assertNotNull(p.attributes().getModificationDate());
                assertNotNull(p.attributes().getSize());
                assertNotNull(p.attributes().getChecksum());
                assertNotNull(p.attributes().getETag());
            }
            else if(p.isDirectory()) {
//                assertTrue(p.isPlaceholder());
            }
            else {
                fail();
            }
        }
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("notfound.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
    }

    @Test
    public void testListPlaceholder() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("SYD");
        final Path placeholder = new Path(container, UUID.randomUUID().toString(), EnumSet.of(Path.Type.directory));
        new SwiftDirectoryFeature(session).mkdir(placeholder);
        final AttributedList<Path> list = new SwiftObjectListService(session).list(placeholder, new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        new SwiftDeleteFeature(session).delete(Arrays.asList(placeholder), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }

    @Test
    public void testPlaceholderAndObjectSameName() throws Exception {
        final SwiftSession session = new SwiftSession(
                new Host(new SwiftProtocol(), "identity.api.rackspacecloud.com",
                        new Credentials(
                                properties.getProperty("rackspace.key"), properties.getProperty("rackspace.secret")
                        )));
        session.open(new DisabledHostKeyCallback(), new DisabledTranscriptListener());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback());
        final Path container = new Path("test.cyberduck.ch", EnumSet.of(Path.Type.directory, Path.Type.volume));
        container.attributes().setRegion("SYD");
        final String basename = UUID.randomUUID().toString();
        final String childname = String.format("%s/%s", basename, UUID.randomUUID().toString());
        final Path base = new Path(container, basename, EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(base);
        final Path child = new Path(container, childname, EnumSet.of(Path.Type.file));
        new SwiftTouchFeature(session).touch(child);
        final AttributedList<Path> list = new SwiftObjectListService(session).list(container, new DisabledListProgressListener());
        assertTrue(list.contains(base));
        assertEquals(EnumSet.of(Path.Type.file), list.get(base.getReference()).getType());
        final Path placeholder = new Path(container, basename, EnumSet.of(Path.Type.directory, Path.Type.placeholder));
        assertTrue(list.contains(placeholder));
        assertEquals(EnumSet.of(Path.Type.directory, Path.Type.placeholder), list.get(placeholder.getReference()).getType());
        new SwiftDeleteFeature(session).delete(Arrays.asList(base, child), new DisabledLoginCallback(), new DisabledProgressListener());
        session.close();
    }
}
