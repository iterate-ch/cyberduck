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

import junit.framework.TestCase;

/**
 * @version $Id$
 */
public class LoginTest extends TestCase {
	
	static {
		org.apache.log4j.BasicConfigurator.configure();
	}
	
	public LoginTest(String name) {
		super(name);
	}
	
	public void testLoginReasonable() {
		try {
			Login login = new Login("example.net", "ftp",
									"guest",
									"changeme");
			assertTrue(login.hasReasonableValues());
		}
		catch(java.lang.UnsatisfiedLinkError e) {}
	}
	
	public void testLoginWithoutUsername() {
		try {
            Login login = new Login("example.net", "ftp",
									null,
									Preferences.instance().getProperty("ftp.anonymous.pass"));
			assertTrue(login.hasReasonableValues());
		}
		catch(java.lang.UnsatisfiedLinkError e) {}
	}
	
	public void testLoginWithoutPass() {
		try {
            Login login = new Login("example.net", "ftp",
									"guest",
									null);
			assertFalse(login.hasReasonableValues());
		}
		catch(java.lang.UnsatisfiedLinkError e) {}
	}

    public void testLoginWithoutEmptyPass() {
        try {
            Login login = new Login("example.net", "ftp",
                                    "guest",
                                    "");
            assertTrue(login.hasReasonableValues());
        }
        catch(java.lang.UnsatisfiedLinkError e) {}
    }

    public void testLoginAnonymous1() {
		try {
            Login login = new Login("example.net", "ftp",
									Preferences.instance().getProperty("ftp.anonymous.name"),
									Preferences.instance().getProperty("ftp.anonymous.pass"));
			assertTrue(login.hasReasonableValues());
		}
		catch(java.lang.UnsatisfiedLinkError e) {}
	}
	
	public void testLoginAnonymous2() {
		try {
            Login login = new Login("example.net", "ftp",
									Preferences.instance().getProperty("ftp.anonymous.name"),
									null);
			assertTrue(login.hasReasonableValues());
		}
		catch(java.lang.UnsatisfiedLinkError e) {}
	}
}