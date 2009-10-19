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

/**
 * @version $Id$
 */
public class LoginTest extends AbstractTestCase {

    static {
        org.apache.log4j.BasicConfigurator.configure();
    }

    public LoginTest(String name) {
        super(name);
    }

    public void testLoginReasonable() {
        Credentials credentials = new Credentials("guest", "changeme");
        assertTrue(credentials.isValid());
    }

    public void testLoginWithoutUsername() {
        Credentials credentials = new Credentials(null,
                Preferences.instance().getProperty("connection.login.anon.pass"));
        assertFalse(credentials.isValid());
    }

    public void testLoginWithoutPass() {
        Credentials credentials = new Credentials("guest", null);
        assertFalse(credentials.isValid());
    }

    public void testLoginWithoutEmptyPass() {
        Credentials credentials = new Credentials("guest", "");
        assertTrue(credentials.isValid());
    }

    public void testLoginAnonymous1() {
        Credentials credentials = new Credentials(Preferences.instance().getProperty("connection.login.anon.name"),
                Preferences.instance().getProperty("connection.login.anon.pass"));
        assertTrue(credentials.isValid());
    }

    public void testLoginAnonymous2() {
        Credentials credentials = new Credentials(Preferences.instance().getProperty("connection.login.anon.name"),
                null);
        assertTrue(credentials.isValid());
    }

    /**
     * http://trac.cyberduck.ch/ticket/1204
     */
    public void testLogin1204() {
        Credentials credentials = new Credentials("cyberduck.login",
                "1seCret");
        assertTrue(credentials.isValid());
        assertEquals("cyberduck.login", credentials.getUsername());
        assertEquals("1seCret", credentials.getPassword());
    }
}