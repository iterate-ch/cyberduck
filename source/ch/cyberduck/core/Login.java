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

import java.io.File;

public class Login {
    private String user;
    private transient String pass;
    private LoginController controller;
	private String privateKeyFile;
    
    /**
		* New instance with default values. Anonymous login.
     */
    public Login() {
		this.user = Preferences.instance().getProperty("ftp.anonymous.name");
		this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
    }
	
//	public Login(String user, File privateKeyFile) {
//		this.user = user;
//		this.privateKeyFile = privateKeyFile;
//	}
	
    /**
		* New instance with default values. Anonymous login.
     * @param controller The controller to handle failed logins attempts
     */
    //public Login(LoginController controller) {
	//	this();
	//	this.controller = controller;
    //}
	
    /**
		* @param l the login credentials
     * @param controller The controller to handle failed logins attempts
     */
//    public Login(String l, LoginController controller) {
//		this(l);
//		this.controller = controller;
//    }
	
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
	
    /**
		* @param l the login credentials
     */
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
	
	public boolean usesPublicKeyAuthentication() {
		return this.privateKeyFile != null;
	}
	
	public boolean usesPasswordAuthentication() {
		return !this.usesPublicKeyAuthentication();
	}
	
	public void setPrivateKeyFile(String file) {
		this.privateKeyFile = file;
	}
	
	public String getPrivateKeyFile() {
		return this.privateKeyFile;
	}
	
    public boolean hasReasonableValues() {
		if(this.user != null && this.pass != null) {
			// anonymous login is ok
			if(this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name")) && this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass")))
				return  true;
			// if both name and pass are custom it is ok
			if(! (this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"))) && ! (this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass"))))
				return  true;
		}
		// all other cases we don't like
		return false;
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
