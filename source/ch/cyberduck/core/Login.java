package ch.cyberduck.core;

/*
 *  ch.cyberduck.core.Login.java
 *  Cyberduck
 *
 *  $Header$
 *  $Revision$
 *  $Date$
 *
 *  Copyright (c) 2003 David Kocher. All rights reserved.
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
    private String user = "anonymous"; //@todo use preferences
    private transient String pass = "anonymous@mail.tld";
    
    public Login(String user, String pass) {
	this.user = user;
	this.pass = pass;
    }

    public String getPassword() {
	return this.pass;
    }

    public String getUsername() {
	return this.user;
    }

    public void setUsername(String u) {
	this.user = u;
    }

    public void setPassword(String p) {
	this.pass = p;
    }

    /**
	* @return true If we whould try again with new login
     */
    public abstract boolean loginFailure();
}
