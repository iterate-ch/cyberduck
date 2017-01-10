package ch.cyberduck.core.dav;

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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.test.IntegrationTest;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.EnumSet;

import static org.junit.Assert.*;

@Category(IntegrationTest.class)
public class DAVListServiceTest {

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        new DAVListService(session).list(new Path("/notfound", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener());
        session.close();
    }

    @Test
    public void testList() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final Path directory = new Path("/trunk", EnumSet.of(Path.Type.directory, Path.Type.volume));
        final AttributedList<Path> list = new DAVListService(session).list(directory,
                new DisabledListProgressListener());
        assertFalse(list.isEmpty());
        assertFalse(list.contains(new Path(directory, "trunk", EnumSet.of(Path.Type.directory))));
        for(Path p : list) {
            assertEquals(directory, p.getParent());
            assertNotNull(p.attributes().getModificationDate());
            assertNotNull(p.attributes().getCreationDate());
            assertNotNull(p.attributes().getSize());
            assertEquals(Checksum.NONE, p.attributes().getChecksum());
            assertNotNull(p.attributes().getETag());
        }
        session.close();
    }

    @Test
    public void testListEmpty() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final AttributedList<Path> list = new DAVListService(session).list(new Path("/trunk/bookmarks", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener());
        assertTrue(list.isEmpty());
        session.close();
    }

    @Test(expected = NotfoundException.class)
    public void testListFileException() throws Exception {
        final Host host = new Host(new DAVSSLProtocol(), "svn.cyberduck.ch", new Credentials(
                PreferencesFactory.get().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DisabledHostKeyCallback()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginCallback(), new DisabledCancelCallback(), PathCache.empty());
        final AttributedList<Path> list = new DAVListService(session).list(new Path("/trunk/LICENSE.txt", EnumSet.of(Path.Type.directory, Path.Type.volume)),
                new DisabledListProgressListener());
        session.close();
    }
}
