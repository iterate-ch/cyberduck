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

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class Login {
    private static Logger log = Logger.getLogger(Login.class);

    private String serviceName;
    private String protocol;
    private String user;
    private transient String pass;
    private String privateKeyFile;
    private boolean shouldBeAddedToKeychain;

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
     *                                successfull login
     */
    public void setUseKeychain(boolean shouldBeAddedToKeychain) {
        this.shouldBeAddedToKeychain = shouldBeAddedToKeychain;
    }

    /**
     *
     * @return
     */
    public boolean usesKeychain() {
        return this.shouldBeAddedToKeychain;
    }

    /**
     *
     * @return
     */
    public String getInternetPasswordFromKeychain() {
        int pool = NSAutoreleasePool.push();
        log.info("Fetching password from Keychain for:" + this.getUsername());
        String password = Keychain.instance().getInternetPasswordFromKeychain(this.protocol,
                this.serviceName, this.getUsername());
        NSAutoreleasePool.pop(pool);
        return password;
    }

    /**
     *
     */
    public void addInternetPasswordToKeychain() {
        if (this.shouldBeAddedToKeychain && !this.isAnonymousLogin()) {
            int pool = NSAutoreleasePool.push();
            Keychain.instance().addInternetPasswordToKeychain(this.protocol,
                    this.serviceName, this.getUsername(), this.getPassword());
            NSAutoreleasePool.pop(pool);
        }
    }

    /**
     *
     * @return
     */
    public String getPasswordFromKeychain() {
        int pool = NSAutoreleasePool.push();
        String pass = Keychain.instance().getPasswordFromKeychain(this.serviceName, this.getUsername());
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
     * @param hostname                The serviceName to use when looking up the password in the keychain
     * @param user                    Login with this username
     * @param pass                    Passphrase
     * @param shouldBeAddedToKeychain if the credential should be added to the keychain uppon successful login
     */
    public Login(String hostname, String protocol, String user, String pass, boolean shouldBeAddedToKeychain) {
        this.serviceName = hostname;
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
            this.user = Preferences.instance().getProperty("ftp.anonymous.name");
        }
        else {
            if (u.indexOf(':') != -1) {
                this.user = u.substring(0, u.indexOf(':'));
                this.pass = u.substring(u.indexOf(':') + 1, u.length());
            }
            else {
                this.user = u;
            }
        }
        if (null == p || p.equals("")) {
            if (this.isAnonymousLogin()) {
                this.pass = Preferences.instance().getProperty("ftp.anonymous.pass");
            }
            else {
                this.pass = null;
            }
        }
        else {
            this.pass = p;
        }
    }

    /**
     *
     * @return
     */
    public boolean isAnonymousLogin() {
        return this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"));
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
     * @return
     */
    public boolean hasReasonableValues() {
        boolean reasonable = false;
        if (this.usesPublicKeyAuthentication()) {
            reasonable = true;
        }
        if (this.user != null && this.pass != null) {
            // anonymous login is ok
            if (this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name")) &&
                    this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass"))) {
                reasonable = true;
            }
            // if both name and pass are custom it is ok
            if (!(this.user.equals(Preferences.instance().getProperty("ftp.anonymous.name"))) &&
                    !(this.pass.equals(Preferences.instance().getProperty("ftp.anonymous.pass")))) {
                reasonable = true;
            }
        }
        log.debug("hasReasonableValues:" + reasonable);
        return reasonable;
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
                    return this.promptUser("The username or password does not seem reasonable.",
                            controller).tryAgain();
                }
                else {
                    this.pass = passFromKeychain;
                    return true;
                }
            }
            else {
                return this.promptUser("The username or password does not seem reasonable.", controller).tryAgain();
            }
        }
        return true;
    }

    private boolean tryAgain;

    /**
     *
     * @return
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

    /**
     *
     * @param message
     * @param controller
     * @return true if the user hasn't canceled the login process. If false is returned,
     * no more attempts should be made and the connection closed.
     */
    public Login promptUser(String message, LoginController controller) {
        if (null == controller) {
            log.error("No login controller; returning default credentials!");
            this.setTryAgain(false);
            return this;
        }
        return controller.promptUser(this, message);
    }
}
