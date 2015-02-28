package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sftp.SFTPProtocol;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @version $Id$
 */
public class HostTest extends AbstractTestCase {

    @Test
    public void testWebURL() {
        Host host = new Host("test");
        host.setWebURL("http://localhost/~dkocher");
        assertEquals("http://localhost/~dkocher", host.getWebURL());
    }

    @Test
    public void testCreate1() {
        final Credentials credentials = new Credentials("u", "p");
        final Host bookmark = new Host(ProtocolFactory.SFTP, "h", 555, "/h", credentials);
        assertNotSame(credentials, bookmark.getCredentials());
        assertEquals(555, bookmark.getPort());
        assertEquals(Scheme.sftp, bookmark.getProtocol().getScheme());
        assertEquals("/h", bookmark.getDefaultPath());
        assertEquals("u", bookmark.getCredentials().getUsername());
        assertEquals("p", bookmark.getCredentials().getPassword());
    }

    @Test
    public void testCreate2() {
        final Credentials credentials = new Credentials("u", "p");
        final Host bookmark = new Host(ProtocolFactory.SFTP, "h", credentials);
        assertNotSame(credentials, bookmark.getCredentials());
        assertEquals(22, bookmark.getPort());
        assertEquals(Scheme.sftp, bookmark.getProtocol().getScheme());
        assertNull(bookmark.getDefaultPath());
        assertEquals("u", bookmark.getCredentials().getUsername());
        assertEquals("p", bookmark.getCredentials().getPassword());
    }

    @Test
    public void testConfigure() {
        final Credentials credentials = new Credentials("u", "p");
        final Host bookmark = new Host(ProtocolFactory.SFTP, "h", credentials);
        bookmark.configure(new HostnameConfigurator() {
                               @Override
                               public String getHostname(String alias) {
                                   return "c";
                               }

                               @Override
                               public int getPort(String alias) {
                                   return 444;
                               }
                           }, new CredentialsConfigurator() {
                               @Override
                               public Credentials configure(Host host) {
                                   final Credentials c = host.getCredentials();
                                   c.setUsername("uu");
                                   return c;
                               }
                           }
        );
        assertEquals(444, bookmark.getPort());
        // Hostname should not be changed
        assertEquals("h", bookmark.getHostname());
        assertEquals("uu", bookmark.getCredentials().getUsername());
    }

    @Test
    public void testTrim() {
        final Host bookmark = new Host(ProtocolFactory.SFTP);
        bookmark.setHostname("h ");
        assertEquals("h", bookmark.getHostname());
    }

    @Test
    public void testInvalidProtocol() {
        PreferencesFactory.get().setProperty("connection.protocol.default", "me");
        final Host bookmark = new Host("h");
        assertEquals(ProtocolFactory.FTP, bookmark.getProtocol());
        PreferencesFactory.get().deleteProperty("connection.protocol.default");
    }

    @Test
    public void testTrimDefaultPath() {
        Host host = new Host("localhost");
        host.setDefaultPath("p");
        assertEquals("p", host.getDefaultPath());
        host.setDefaultPath("/p");
        assertEquals("/p", host.getDefaultPath());
        host.setDefaultPath("/p\n");
        assertEquals("/p", host.getDefaultPath());
        host.setDefaultPath("/p\r");
        assertEquals("/p", host.getDefaultPath());
        host.setDefaultPath("/p\r\n");
        assertEquals("/p", host.getDefaultPath());
        host.setDefaultPath("/p f");
        assertEquals("/p f", host.getDefaultPath());
        host.setDefaultPath("/p ");
        assertEquals("/p", host.getDefaultPath());
    }

    @Test
    public void testCompare() {
        assertEquals(0, new Host("a", 33).compareTo(new Host("a", 33)));
        assertEquals(1, new Host("a", 22).compareTo(new Host("a", 33)));
        assertEquals(-1, new Host("a", 33).compareTo(new Host("a", 22)));

        assertEquals(1, new Host(new SFTPProtocol(), "a", 22, new Credentials("u", null))
                .compareTo(new Host(new SFTPProtocol(), "a", 22, new Credentials())));
        assertEquals(-1, new Host(new SFTPProtocol(), "a", 22, new Credentials())
                .compareTo(new Host(new SFTPProtocol(), "a", 22, new Credentials("u", null))));

        assertEquals(0, new Host("a").compareTo((new Host("a"))));
        assertEquals(-1, new Host("a").compareTo((new Host("b"))));
        assertEquals(1, new Host("b").compareTo((new Host("a"))));
    }
}
