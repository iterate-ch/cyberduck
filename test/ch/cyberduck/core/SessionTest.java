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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SessionTest extends TestCase {
    public SessionTest(String name) {
        super(name);
    }

    private Session session;

    public void setUp() throws Exception {
        super.setUp();
        this.session = SessionFactory.createSession(new Host("localhost"));
    }

    public void tearDown() throws Exception {
        super.tearDown();
        this.session = null;
    }

    public void testGetHost() throws Exception {
        assertEquals(session.getHost(), session.getHost());
        assertEquals(session.getHost().getCredentials(), session.getHost().getCredentials());
    }

    public static Test suite() {
        return new TestSuite(SessionTest.class);
    }
}
