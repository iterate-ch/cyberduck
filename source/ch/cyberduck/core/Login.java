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

import ch.cyberduck.ui.LoginController;

import com.apple.cocoa.foundation.NSAutoreleasePool;
import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Login {
    private static Logger log = Logger.getLogger(Login.class);

    private String hostname;
    private String protocol;
    private String user;
    private transient String pass;
    private String privateKeyFile;
    private boolean shouldBeAddedToKeychain;

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
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

    /**
     * Use this to define if passwords should be added to the keychain
     *
     * @param shouldBeAddedToKeychain If true, the password of the login is added to the keychain uppon
     * successfull login
     */
    public void setUseKeychain(boolean shouldBeAddedToKeychain) {
        this.shouldBeAddedToKeychain = shouldBeAddedToKeychain;
    }

    /**
     *
     * @return true if the password will be added to the system keychain when logged in successfully
     */
    public boolean usesKeychain() {
        return this.shouldBeAddedToKeychain;
    }

    /**
     *
     * @return the password fetched from the keychain or null if it was not found
     */
    public String getInternetPasswordFromKeychain() {
        int pool = NSAutoreleasePool.push();
        log.info("Fetching password from Keychain for:" + this.getUsername());
        String password = Keychain.instance().getInternetPasswordFromKeychain(this.protocol,
                this.hostname, this.getUsername());
        NSAutoreleasePool.pop(pool);
        return password;
    }

    /**
     * Adds the password to the system keychain
     */
    public void addInternetPasswordToKeychain() {
        if (this.shouldBeAddedToKeychain && !this.isAnonymousLogin() && this.hasReasonableValues()) {
            int pool = NSAutoreleasePool.push();
            Keychain.instance().addInternetPasswordToKeychain(this.protocol,
                    this.hostname, this.getUsername(), this.getPassword());
            NSAutoreleasePool.pop(pool);
        }
    }

    /**
     *
     * @return the password fetched from the system keychain or null if it was not found
     */
    public String getPasswordFromKeychain() {
        int pool = NSAutoreleasePool.push();
        String pass = Keychain.instance().getPasswordFromKeychain(this.hostname, this.getUsername());
        NSAutoreleasePool.pop(pool);
        return pass;
    }

    /**
     * @param hostname The service to use when looking up the password in the keychain
     * @param user     Login with this username
     * @param pass     Passphrase
     */
    public Login(String hostname, String protocol, String user, String pass) {
        this(hostname, protocol, user, pass, false);
    }

    /**
     * @param hostname                The hostname to use when looking up the password in the keychain
     * @param user                    Login with this username
     * @param pass                    Passphrase
     * @param shouldBeAddedToKeychain if the credential should be added to the keychain uppon successful login
     */
    public Login(String hostname, String protocol, String user, String pass, boolean shouldBeAddedToKeychain) {
        this.hostname = hostname;
        this.protocol = protocol;
        this.shouldBeAddedToKeychain = shouldBeAddedToKeychain;
        this.init(user, pass);
    }

    /**
     * @param u The username to use or null if anonymous
     * @param p The password to use or null if anonymous
     */
    private void init(String u, String p) {
        if (null == u || u.equals("")) {
			if(this.protocol.equals(Session.FTP)) {
            	this.user = Preferences.instance().getProperty("ftp.anonymous.name");
			}
			else {
            	this.user = Preferences.instance().getProperty("connection.login.name");
			}
        }
        else {
			this.user = u;
        }
        if (null == p || p.equals("")) {
            if (this.isAnonymousLogin()) {
                this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
            }
            else {
                this.pass = p;
            }
        }
        else {
            this.pass = p;
        }
    }

    /**
     *
     * @return true if the username is anononymous
     */
    public boolean isAnonymousLogin() {
        return Preferences.instance().getProperty("ftp.anonymous.name").equals(this.getUsername());
    }

    /**
     * SSH specific
     *
     * @return true if public key authentication should be used. This is the case, if a
     *         private key file has been specified
     * @see #setPrivateKeyFile
     */
    public boolean usesPublicKeyAuthentication() {
        return this.privateKeyFile != null && this.protocol.equals(Session.SFTP);
    }

    public void setPrivateKeyFile(String file) {
        this.privateKeyFile = file;
    }

    public String getPrivateKeyFile() {
        return this.privateKeyFile;
    }

    /**
     * Checks if both username and password qualify for a possible reasonable login attempt
     * @return true if both username and password could be valid
     */
    public boolean hasReasonableValues() {
        if (this.usesPublicKeyAuthentication()) {
            return true;
        }
        if (this.getUsername() != null && this.getPassword() != null) {
            // anonymous login is ok
            if (Preferences.instance().getProperty("ftp.anonymous.name").equals(this.getUsername()) &&
                    Preferences.instance().getProperty("ftp.anonymous.pass").equals(this.getPassword())) {
                return true;
            }
            // if both name and pass are custom it is ok
            if (!(Preferences.instance().getProperty("ftp.anonymous.name").equals(this.getUsername())) &&
                    !(Preferences.instance().getProperty("ftp.anonymous.pass").equals(this.getPassword()))) {
                return true;
            }
        }
		return false;
    }

    /**
     *
     * @param controller
     * @return true if reasonable values have been found localy or in the keychain or the user
     * was prompted to for the credentials and new values got entered.
     */
    public boolean check(LoginController controller) {
        if (!this.hasReasonableValues()) {
            if (Preferences.instance().getBoolean("connection.login.useKeychain")) {
                log.info("Searching keychain for password...");
                String passFromKeychain = this.getInternetPasswordFromKeychain();
                if (null == passFromKeychain || passFromKeychain.equals("")) {
                    passFromKeychain = this.getPasswordFromKeychain(); //legacy support
                }
                if (null == passFromKeychain || passFromKeychain.equals("")) {
					if(null == controller) {
						throw new IllegalArgumentException("No login controller given");
					}
                    controller.promptUser(this,
                            NSBundle.localizedString("Login with username and password", "Credentials", ""),
                            NSBundle.localizedString("No login credentials could be found in the Keychain", "Credentials", ""));
                    return this.tryAgain();
                }
                else {
                    this.pass = passFromKeychain;
                    return true;
                }
            }
            else {
				if(null == controller) {
					throw new IllegalArgumentException("No login controller given");
				}
                controller.promptUser(this,
                        NSBundle.localizedString("Login with username and password", "Credentials", ""),
                        NSBundle.localizedString("The use of the Keychain is disabled in the Preferences", "Credentials", ""));
                return this.tryAgain();
            }
        }
        return true;
    }

    private boolean tryAgain;

    /**
     *
     * @return true if the user decided to try login again with new credentials
     */
    public boolean tryAgain() {
        return this.tryAgain;
    }

    /**
     *
     * @param v
     */
    public void setTryAgain(boolean v) {
        this.tryAgain = v;
    }
}
