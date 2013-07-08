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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.NotfoundException;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DAVListServiceTest extends AbstractTestCase {

    @Test(expected = NotfoundException.class)
    public void testListNotfound() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        new DAVListService(session).list(new Path("/notfound", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE));
        session.close();
    }

    @Test
    public void testList() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        assertNotNull(session.open(new DefaultHostKeyController()));
        assertTrue(session.isConnected());
        assertNotNull(session.getClient());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final AttributedList<Path> list = new DAVListService(session).list(new Path("/trunk", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE));
        assertFalse(list.isEmpty());
        for(Path p : list) {
            assertEquals(new Path("/trunk", Path.DIRECTORY_TYPE | Path.VOLUME_TYPE), p.getParent());
            assertNotNull(p.attributes().getModificationDate());
            assertNotNull(p.attributes().getCreationDate());
            assertNotNull(p.attributes().getSize());
            assertNotNull(p.attributes().getChecksum());
            assertNotNull(p.attributes().getETag());
        }
        session.close();
    }
}
