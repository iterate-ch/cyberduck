package ch.cyberduck.core;
/*
 *  Copyright (c) 2002 David Kocher. All rights reserved.
 *  http://icu.unizh.ch/~dkocher/
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

public abstract class Login {
    private String user = Preferences.instance().getProperty("connection.login.anonymous.name");
    private transient String pass = Preferences.instance().getProperty("connection.login.anonymous.pass");

    /**
	* New instance with default values. (anonymous login)
     */
    public Login() {

    }

    /**
	* 
     */
    public Login(String user, String pass) {
	this.user = user;
	this.pass = pass;
    }

    public String getUsername() {
	return this.user;
    }

    public void setUsername(String u) {
	this.user = u;
    }

    public String getPassword() {
	return this.pass;
    }
    
    public void setPassword(String p) {
	this.pass = p;
    }

    /**
	* @return true If we whould try again with new login
     */
    public abstract boolean loginFailure();
}
