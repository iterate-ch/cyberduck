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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Stores the login credentials
 *
 * @version $Id$
 */
public abstract class Credentials {
    private static Logger log = Logger.getLogger(Credentials.class);

    /**
     * The login name
     */
    private String user;

    /**
     * The login password
     */
    private transient String password;

    /**
     * If not null, use public key authentication if SSH is the protocol
     */
    private Local identity;

    /**
     * If the credentials should be stored in the Keychain upon successful login
     */
    private boolean shouldBeAddedToKeychain;

    /**
     * Default credentials from Preferences
     */
    public Credentials() {
        this(Preferences.instance().getProperty("connection.login.name"), null,
                Preferences.instance().getBoolean("connection.login.useKeychain"));
    }

    /**
     * @param user     Login with this username
     * @param password Passphrase
     */
    public Credentials(String user, String password) {
        this(user, password, Preferences.instance().getBoolean("connection.login.useKeychain"));
    }

    /**
     * @param user                    Login with this username
     * @param password                Passphrase
     * @param shouldBeAddedToKeychain if the credential should be added to the keychain uppon successful login
     */
    public Credentials(String user, String password, boolean shouldBeAddedToKeychain) {
        this.shouldBeAddedToKeychain = shouldBeAddedToKeychain;
        this.init(user, password);
    }

    /**
     * @param username The username to use or null if anonymous
     * @param password The password to use or null if anonymous
     */
    private void init(String username, String password) {
        this.user = username;
        this.password = password;
    }

    /**
     * @return The login identification
     */
    public String getUsername() {
        return this.user;
    }

    public void setUsername(String user) {
        this.user = user;
    }

    /**
     * @return The login secret
     */
    public String getPassword() {
        if(StringUtils.isEmpty(password)) {
            if(this.isAnonymousLogin()) {
                return Preferences.instance().getProperty("connection.login.anon.pass");
            }
        }
        return password;
    }

    public void setPassword(String pass) {
        this.password = pass;
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
     * @return true if the password will be added to the system keychain when logged in successfully
     */
    public boolean usesKeychain() {
        return this.shouldBeAddedToKeychain;
    }

    /**
     * @return true if the username is anononymous
     */
    public boolean isAnonymousLogin() {
        final String user = this.getUsername();
        if(StringUtils.isEmpty(user)) {
            return false;
        }
        return Preferences.instance().getProperty("connection.login.anon.name").equals(user);
    }

    /**
     * SSH specific
     *
     * @return true if public key authentication should be used. This is the case, if a
     *         private key file has been specified
     * @see #setIdentity
     */
    public boolean isPublicKeyAuthentication() {
        if(null == this.getIdentity()) {
            return false;
        }
        return this.getIdentity().exists();
    }

    /**
     * The path for the private key file to use for public key authentication; e.g. ~/.ssh/id_rsa
     *
     * @param file
     */
    public void setIdentity(Local file) {
        this.identity = file;
    }

    /**
     * @return The path to the private key file to use for public key authentication
     */
    public Local getIdentity() {
        return identity;
    }

    /**
     * @return
     */
    public boolean isValid() {
        return StringUtils.isNotEmpty(this.getUsername())
                && this.getPassword() != null;
    }

    /**
     * Reset credentials.
     */
    public void clear() {
        user = null;
        password = null;
    }

    public abstract String getUsernamePlaceholder();

    public abstract String getPasswordPlaceholder();
}
