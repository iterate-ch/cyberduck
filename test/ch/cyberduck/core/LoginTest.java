package ch.cyberduck.core;

/*
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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

public class LoginTest extends TestCase {

	static {
		org.apache.log4j.BasicConfigurator.configure();
	}

	public LoginTest(String name) {
		super(name);
	}

	public void testLoginReasonable() {
		Login login = new Login("example.net", "dkocher", "changeme", true);
		assertTrue(login.hasReasonableValues());
		login.setUsername(null);
		assertFalse(login.hasReasonableValues());
		login.setPassword(null);
		assertFalse(login.hasReasonableValues());
	}
		
	public void testLoginWithoutPass() {
		Login login = new Login("example.net", "dkocher", null, false);
		assertFalse(login.hasReasonableValues());
	}
	
	public void testLoginAnonymous1() {
		Login login = new Login("example.net", 
		Preferences.instance().getProperty("ftp.anonymous.name"), 
		Preferences.instance().getProperty("ftp.anonymous.pass"), 
		false);
		assertTrue(login.hasReasonableValues());
	}

	public void testLoginAnonymous2() {
		Login login = new Login("example.net", null, null, false);
		assertTrue(login.hasReasonableValues());
	}
	
	/*
	public void testKeychain() {
		Login login = new Login("example.net", "dkocher", "changeme", true);
		login.addPasswordToKeychain();
		assertEquals(login.getPassword(), login.getPasswordFromKeychain());
	}
	*/
}