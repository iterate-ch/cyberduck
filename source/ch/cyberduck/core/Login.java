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

import org.apache.log4j.Logger;

public class Login {
	private static Logger log = Logger.getLogger(Login.class);

	static {
        // Ensure native keychain library is loaded
        System.loadLibrary("Keychain");
    }
	
	//char *getpwdfromkeychain( const char *service, const char *account, OSStatus *error );
    native String getPasswordFromKeychain(String service, String account);
	
	//void addpwdtokeychain( const char *service, const char *account, const char *password );
	native void addPasswordToKeychain(String service, String account, String password);
	
	private String service;
	private String user;
	private transient String pass;
	private LoginController controller;
	private String privateKeyFile;

	/**
	 * New instance with default values. Anonymous login.
	 * @param service The service to use when looking up the password in the keychain
	 */
	public Login(String service) {
		this.service = service;
		this.user = Preferences.instance().getProperty("ftp.anonymous.name");
		this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
	}

	/**
		* @param service The service to use when looking up the password in the keychain
	 * @param user Login with this username
	 * @param pass Passphrase
	 */
	public Login(String service, String user, String pass) {
		this.service = service;
		if (null == user || user.equals(""))
			this.user = Preferences.instance().getProperty("ftp.anonymous.name");
		else
			this.user = user;
		if (null == pass || pass.equals(""))
			this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
		else
			this.pass = pass;
	}

	/**
		* @param service The service to use when looking up the password in the keychain
	 * @param l the login credentials
	 */
	public Login(String service, String l) {
		this.service = service;
		if (l != null) {
			if (l.indexOf(':') != -1) {
				this.user = l.substring(0, l.indexOf(':'));
				this.pass = l.substring(l.indexOf(':') + 1, l.length());
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
		if (this.usesPublicKeyAuthentication())
			return true;
		if (this.user != null && this.pass != null) {
			// anonymous login is ok
			if (this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name")) && this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass")))
				return true;
			// if both name and pass are custom it is ok
			if (!(this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"))) && !(this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass"))))
				return true;
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
		if(!this.hasReasonableValues()) {
			if(Preferences.instance().getProperty("connection.login.useKeychain").equals("true")) {
				log.info("Searching keychain for password...");
				this.pass = this.getPasswordFromKeychain(this.service, this.getUsername());
				if(null == this.pass) {
					//				if(this.controller != null)
					//					this.controller.loginFailure("The username or password is not reasonable.");
					//				else
					// still no reasonable values, what a pitty
					this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
				}
			}
		}
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
		return this.getUsername() + ":" + this.getPassword();
	}
}
