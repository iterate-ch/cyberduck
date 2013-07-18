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

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DefaultHostKeyController;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.features.Touch;

import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class DAVHeadersFeatureTest extends AbstractTestCase {

    @Test
    public void testGetMetadataFolder() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Map<String, String> metadata = new DAVHeadersFeature(session).getMetadata(new Path("/trunk", Path.DIRECTORY_TYPE));
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("repository-uuid"));
        assertEquals("9e2dff1d-8f06-0410-b5b1-4d70b6340adc", metadata.get("repository-uuid"));
        session.close();
    }

    @Test
    public void testGetMetadataFile() throws Exception {
        final Host host = new Host(Protocol.WEBDAV_SSL, "svn.cyberduck.ch", new Credentials(
                Preferences.instance().getProperty("connection.login.anon.name"), null
        ));
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Map<String, String> metadata = new DAVHeadersFeature(session).getMetadata(new Path("/trunk/README.txt", Path.FILE_TYPE));
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("repository-uuid"));
        assertEquals("9e2dff1d-8f06-0410-b5b1-4d70b6340adc", metadata.get("repository-uuid"));
        assertTrue(metadata.containsKey("eol-style"));
        assertEquals("native", metadata.get("eol-style"));
        session.close();
    }

    @Test
    public void testSetMetadataFile() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.FILE_TYPE);
        session.getFeature(Touch.class, new DisabledLoginController()).touch(test);
        final String v = UUID.randomUUID().toString();
        new DAVHeadersFeature(session).setMetadata(test, Collections.<String, String>singletonMap("Test", v));
        final Map<String, String> metadata = new DAVHeadersFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        session.delete(test, new DisabledLoginController());
        session.close();

    }

    @Test
    public void testSetMetadataFolder() throws Exception {
        final Host host = new Host(Protocol.WEBDAV, "test.cyberduck.ch", new Credentials(
                properties.getProperty("webdav.user"), properties.getProperty("webdav.password")
        ));
        host.setDefaultPath("/dav/basic");
        final DAVSession session = new DAVSession(host);
        session.open(new DefaultHostKeyController());
        session.login(new DisabledPasswordStore(), new DisabledLoginController());
        final Path test = new Path(session.home(), UUID.randomUUID().toString(), Path.DIRECTORY_TYPE);
        session.mkdir(test, null);
        final String v = UUID.randomUUID().toString();
        new DAVHeadersFeature(session).setMetadata(test, Collections.<String, String>singletonMap("Test", v));
        final Map<String, String> metadata = new DAVHeadersFeature(session).getMetadata(test);
        assertFalse(metadata.isEmpty());
        assertTrue(metadata.containsKey("Test"));
        assertEquals(v, metadata.get("Test"));
        session.delete(test, new DisabledLoginController());
        session.close();

    }
}
