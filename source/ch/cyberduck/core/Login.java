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

import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

public class Login {
	private static Logger log = Logger.getLogger(Login.class);
	
	private String service;
	private String user;
	private transient String pass;
	private String privateKeyFile;
	private LoginController controller;
	private boolean addToKeychain;

	static {
		// Ensure native keychain library is loaded
		try {
			NSBundle bundle = NSBundle.mainBundle();
			String lib = bundle.resourcePath() + "/Java/" + "libKeychain.jnilib";
			log.debug("Locating libKeychain.jnilib at '"+lib+"'");
			System.load(lib);
		}
		catch (UnsatisfiedLinkError e) {
			log.error("Could not load the Keychain library:" + e.getMessage());
		}
	}
	
	public void setUseKeychain(boolean addToKeychain) {
		this.addToKeychain = addToKeychain;
	}
	
	public boolean usesKeychain() {
		return this.addToKeychain;
	}

	public native String getPasswordFromKeychain(String service, String account);

	public String getPasswordFromKeychain() {
		int pool = NSAutoreleasePool.push();
		String pass = this.getPasswordFromKeychain(this.service, this.user);
		NSAutoreleasePool.pop(pool);
		return pass;
	}

	public native void addPasswordToKeychain(String service, String account, String password);

	public void addPasswordToKeychain() {
		if(this.addToKeychain) {
			int pool = NSAutoreleasePool.push();
			this.addPasswordToKeychain(this.service, this.user, this.pass);
			NSAutoreleasePool.pop(pool);
		}
	}

	/**
	 * @param service The service to use when looking up the password in the keychain
	 * @param user Login with this username
	 * @param pass Passphrase
	 */
	public Login(String service, String user, String pass) {
		this(service, user, pass, false);
	}

	/**
		* @param service The service to use when looking up the password in the keychain
	 * @param user Login with this username
	 * @param pass Passphrase
	 * @param addToKeychain if the credential should be added to the keychain uppon successful login
	 */
	public Login(String service, String user, String pass, boolean addToKeychain) {
		this.service = service;
		this.addToKeychain = addToKeychain;
		this.init(user, pass);
	}

	private void init(String user, String pass) {
		if (null == user || user.equals("")) {
			this.user = Preferences.instance().getProperty("connection.login.name");
			if(this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"))) {
				this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
			}
		}
		else {
			if(user.indexOf(':') != -1) { //catch username/pass from java.net.URL.getUserInfo()
				this.user = user.substring(0, user.indexOf(':'));
				this.pass = user.substring(user.indexOf(':')+1, user.length());
			}
			else
				this.user = user;
		}
		if (null == pass || pass.equals("")) {
			if(this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"))) {
				this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
			}
		}
		else 
			this.pass = pass;
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
		boolean reasonable = false;
		if (this.usesPublicKeyAuthentication())
			reasonable = true;
		if (this.user != null && this.pass != null) {
			// anonymous login is ok
			if (this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name")) && 
				this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass")))
				reasonable = true;
			// if both name and pass are custom it is ok
			if (!(this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"))) && 
				!(this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass"))))
				reasonable = true;
		}
		log.debug("hasReasonableValues:"+reasonable);
		return reasonable;
	}
	
	public boolean check() {
		if (!this.hasReasonableValues()) {
			if (Preferences.instance().getProperty("connection.login.useKeychain").equals("true")) {
				log.info("Searching keychain for password...");
				String passFromKeychain = this.getPasswordFromKeychain();
				if (null == passFromKeychain || passFromKeychain.equals("")) {
					return controller.promptUser(this, "The username or password does not seem reasonable.");
				}
				else {
					this.pass = passFromKeychain;
					return true;
				}
			}
			else
				return controller.promptUser(this, "The username or password does not seem reasonable.");
		}
		return true;
	}

	/**
	 * @pre controller != null
	 */
	public boolean promptUser(String message) {
		return this.controller.promptUser(this, message);
	}

	public String getUsername() {
		return this.user;
	}

	public void setUsername(String user) {
		this.init(user, this.getPassword());
	}

	public String getPassword() {
		return this.pass;
	}

	public void setPassword(String pass) {
		this.init(this.getUsername(), pass);
	}

	public void setController(LoginController lc) {
		this.controller = lc;
	}
}
