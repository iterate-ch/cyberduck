package ch.cyberduck.core;

/*
 *  Copyright (c) 2004 David Kocher. All rights reserved.
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

import com.apple.cocoa.foundation.NSBundle;

import org.apache.log4j.Logger;

import ch.cyberduck.ui.LoginController;

public class Login {
    private static Logger log = Logger.getLogger(Login.class);

    private String serviceName;
    private String user;
    private transient String pass;
    private String privateKeyFile;
    private LoginController controller;
    private boolean shouldBeAddedToKeychain;

    static {
        // Ensure native keychain library is loaded
        try {
            NSBundle bundle = NSBundle.mainBundle();
            String lib = bundle.resourcePath() + "/Java/" + "libKeychain.jnilib";
            log.debug("Locating libKeychain.jnilib at '" + lib + "'");
            System.load(lib);
        }
        catch (UnsatisfiedLinkError e) {
            log.error("Could not load the Keychain library:" + e.getMessage());
        }
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

    public boolean usesKeychain() {
        return this.shouldBeAddedToKeychain;
    }

    /**
     * @see #getPasswordFromKeychain
     */
    public native String getPasswordFromKeychain(String serviceName, String account);

    public String getPasswordFromKeychain() {
        return this.getPasswordFromKeychain(this.getServiceName(), this.getUsername());
    }

    /**
     * @see #addPasswordToKeychain
     */
    public native void addPasswordToKeychain(String serviceName, String account, String password);

    public void addPasswordToKeychain() {
        if (this.shouldBeAddedToKeychain && !this.isAnonymousLogin()) {
            this.addPasswordToKeychain(this.getServiceName(), this.getUsername(), this.getPassword());
        }
    }

    /**
     * @param serviceName The service to use when looking up the password in the keychain
     * @param user        Login with this username
     * @param pass        Passphrase
     */
    public Login(String serviceName, String user, String pass) {
        this(serviceName, user, pass, false);
    }

    /**
     * @param serviceName             The serviceName to use when looking up the password in the keychain
     * @param user                    Login with this username
     * @param pass                    Passphrase
     * @param shouldBeAddedToKeychain if the credential should be added to the keychain uppon successful login
     */
    public Login(String serviceName, String user, String pass, boolean shouldBeAddedToKeychain) {
        this.serviceName = serviceName;
        this.shouldBeAddedToKeychain = shouldBeAddedToKeychain;
        this.init(user, pass);
    }

    /**
     * @param u    The username to use or null if anonymous
     * @param pass The password to use or null if anonymous
     */
    private void init(String u, String p) {
        if (null == u || u.equals("")) {
            this.user = Preferences.instance().getProperty("ftp.anonymous.name");
        }
        else {
            if (u.indexOf(':') != -1) { //catch username/pass from java.net.URL.getUserInfo()
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
        }
        else {
            this.pass = p;
        }
    }

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
        return this.privateKeyFile != null;
    }

    public void setPrivateKeyFile(String file) {
        this.privateKeyFile = file;
    }

    public String getPrivateKeyFile() {
        return this.privateKeyFile;
    }

    /**
     * Checks if both username and password qualify for a possible reasonable login attempt
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
     * @return true if reasonable values have been found localy or
     *         in the keychain or the user was prompted to
     *         for the credentials and new values got entered.
     */
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
            else {
                return controller.promptUser(this, "The username or password does not seem reasonable.");
            }
        }
        return true;
    }

    /**
     * @return true if the user hasn't canceled the login process. If false is returned,
     *         no more attempts should be made and the connection closed.
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

    public String getServiceName() {
        return this.serviceName;
    }

    public void setController(LoginController lc) {
        this.controller = lc;
    }
}
