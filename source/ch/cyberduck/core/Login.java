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

import ch.cyberduck.ui.LoginController;

public class Login {
    private String user;
    private transient String pass;
    private LoginController controller;
    
    /**
	* New instance with default values. Anonymous login.
     */
    public Login() {
	this.user = Preferences.instance().getProperty("ftp.anonymous.name");
	this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
    }

    public Login(String l) {
	if(l != null) {
	    if(l.indexOf(':') != -1) {
		this.user = l.substring(0, l.indexOf(':'));
		this.pass = l.substring(l.indexOf(':')+1, l.length());
	    }
	    else {
		this.user = l;
		this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
	    }
	}
	else {
	    this.user = Preferences.instance().getProperty("ftp.anonymous.name");
	    this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
	}
    }

    /**
	* @param user Login with this username
	* @param pass Passphrase
     */
    public Login(String user, String pass) {
	if(null == user || user.equals(""))
	    this.user = Preferences.instance().getProperty("ftp.anonymous.name");
	else
	    this.user = user;
	if(null == pass || pass.equals(""))
	    this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
	else
	    this.pass = pass;
    }

//    public abstract boolean loginFailure(String explanation);

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

    public void setController(LoginController c) {
	this.controller = c;
    }

    public LoginController getController() {
	return this.controller;
    }
    
    public String toString() {
	return this.getUsername()+":"+this.getPassword();
    }
}
