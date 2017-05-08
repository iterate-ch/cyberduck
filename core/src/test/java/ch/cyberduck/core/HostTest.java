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

import org.junit.Test;

import static org.junit.Assert.*;

public class HostTest {

    @Test
    public void testWebURL() {
        Host host = new Host(new TestProtocol(), "test");
        host.setWebURL("http://localhost/~dkocher");
        assertEquals("http://localhost/~dkocher", host.getWebURL());
    }

    @Test
    public void testCreate1() {
        final Credentials credentials = new Credentials("u", "p");
        final Host bookmark = new Host(new TestProtocol(), "h", 555, "/h", credentials);
        assertNotSame(credentials, bookmark.getCredentials());
        assertEquals(555, bookmark.getPort());
        assertEquals(Scheme.http, bookmark.getProtocol().getScheme());
        assertEquals("/h", bookmark.getDefaultPath());
        assertEquals("u", bookmark.getCredentials().getUsername());
        assertEquals("p", bookmark.getCredentials().getPassword());
    }

    @Test
    public void testCreate2() {
        final Credentials credentials = new Credentials("u", "p");
        final Host bookmark = new Host(new TestProtocol(Scheme.sftp), "h", credentials);
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
        final Host bookmark = new Host(new TestProtocol(Scheme.sftp), "h", credentials);
        bookmark.configure(new HostnameConfigurator() {
                               @Override
                               public String getHostname(String alias) {
                                   return "c";
                               }

                               @Override
                               public int getPort(String alias) {
                                   return 444;
                               }

            @Override
            public void reload() {
                //
            }
                           }, new CredentialsConfigurator() {
                               @Override
                               public Credentials configure(Host host) {
                                   final Credentials c = host.getCredentials();
                                   c.setUsername("uu");
                                   return c;
                               }

            @Override
            public void reload() {
                //
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
        final Host bookmark = new Host(new TestProtocol(Scheme.sftp));
        bookmark.setHostname("h ");
        assertEquals("h", bookmark.getHostname());
    }

    @Test
    public void testInvalidProtocol() {
        PreferencesFactory.get().setProperty("connection.protocol.default", "me");
        final Host bookmark = new Host(new TestProtocol(Scheme.ftp), "h");
        assertEquals(new TestProtocol(Scheme.ftp), bookmark.getProtocol());
        PreferencesFactory.get().deleteProperty("connection.protocol.default");
    }

    @Test
    public void testTrimDefaultPath() {
        Host host = new Host(new TestProtocol(Scheme.ftp), "localhost");
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
        assertEquals(0, new Host(new TestProtocol(Scheme.ftp), "a", 33).compareTo(new Host(new TestProtocol(Scheme.ftp), "a", 33)));
        assertEquals(-1, new Host(new TestProtocol(Scheme.ftp), "a", 22).compareTo(new Host(new TestProtocol(Scheme.ftp), "a", 33)));
        assertEquals(1, new Host(new TestProtocol(Scheme.ftp), "a", 33).compareTo(new Host(new TestProtocol(Scheme.ftp), "a", 22)));

        assertEquals(1, new Host(new TestProtocol(Scheme.sftp), "a", 22, new Credentials("u", null))
                .compareTo(new Host(new TestProtocol(Scheme.sftp), "a", 22, new Credentials())));
        assertEquals(-1, new Host(new TestProtocol(Scheme.sftp), "a", 22, new Credentials())
                .compareTo(new Host(new TestProtocol(Scheme.sftp), "a", 22, new Credentials("u", null))));

        assertEquals(0, new Host(new TestProtocol(Scheme.ftp), "a").compareTo((new Host(new TestProtocol(Scheme.ftp), "a"))));
        assertEquals(-1, new Host(new TestProtocol(Scheme.ftp), "a").compareTo((new Host(new TestProtocol(Scheme.ftp), "b"))));
        assertEquals(1, new Host(new TestProtocol(Scheme.ftp), "b").compareTo((new Host(new TestProtocol(Scheme.ftp), "a"))));
    }
}
