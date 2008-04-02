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

import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import sun.misc.BASE64Encoder;

/**
 * Stores the login credentials
 *
 * @version $Id$
 */
public class Credentials {
    private static Logger log = Logger.getLogger(Credentials.class);

    /**
     * The login name
     */
    private String user;
    /**
     * The login password
     */
    private transient String pass;
    /**
     * If not null, use public key authentication if SSH is the protocol
     */
    private String privateKeyFile;
    /**
     * If the credentials should be stored in the Keychain upon successful login
     */
    private boolean shouldBeAddedToKeychain;

    public String getUsername() {
        return this.user;
    }

    public void setUsername(String user) {
        this.init(user, this.getPassword());
    }

    public String getPassword() {
        return this.getPassword(false);
    }

    /**
     * @param encrypted
     * @return
     */
    public String getPassword(boolean encrypted) {
        if(!encrypted) {
            return this.pass;
        }
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
        }
        catch(NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            return null;
        }
        try {
            md.update(this.pass.getBytes("UTF-8"));
        }
        catch(UnsupportedEncodingException e) {
            log.error(e.getMessage());
            return null;
        }
        byte raw[] = md.digest();
        return (new BASE64Encoder()).encode(raw);
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
     * @return true if the password will be added to the system keychain when logged in successfully
     */
    public boolean usesKeychain() {
        return this.shouldBeAddedToKeychain;
    }

    /**
     * @return the password fetched from the keychain or null if it was not found
     */
    public String getInternetPasswordFromKeychain(Protocol protocol, String hostname) {
        if(log.isInfoEnabled()) {
            log.info("Fetching password from Keychain for:" + protocol + "," + hostname + "," + this.getUsername());
        }
        final String p = Keychain.instance().getInternetPasswordFromKeychain(protocol.getScheme(),
                hostname, this.getUsername());
        if(null == p) {
            log.info("Password for " + protocol + "," + hostname + "," + this.getUsername() + " not found in Keychain");
        }
        return p;
    }

    /**
     * Adds the password to the system keychain
     */
    public void addInternetPasswordToKeychain(Protocol protocol, String hostname, int port) {
        if(this.usesKeychain() && !this.isAnonymousLogin()) {
            if(StringUtils.hasLength(this.getUsername()) && StringUtils.hasLength(this.getPassword())) {
                if(log.isInfoEnabled()) {
                    log.debug("addInternetPasswordToKeychain:" + protocol + "," + hostname + "," + this.getUsername());
                }
                Keychain.instance().addInternetPasswordToKeychain(protocol.getScheme(), port,
                        hostname, this.getUsername(), this.getPassword());
            }
        }
    }

    public Credentials() {
        this(Preferences.instance().getProperty("connection.login.name"), null,
                Preferences.instance().getBoolean("connection.login.useKeychain"));
    }

    /**
     * @param user Login with this username
     * @param pass Passphrase
     */
    public Credentials(String user, String pass) {
        this(user, pass, Preferences.instance().getBoolean("connection.login.useKeychain"));
    }

    /**
     * @param user                    Login with this username
     * @param pass                    Passphrase
     * @param shouldBeAddedToKeychain if the credential should be added to the keychain uppon successful login
     */
    public Credentials(String user, String pass, boolean shouldBeAddedToKeychain) {
        this.shouldBeAddedToKeychain = shouldBeAddedToKeychain;
        this.init(user, pass);
    }

    /**
     * @param username The username to use or null if anonymous
     * @param password The password to use or null if anonymous
     */
    private void init(String username, String password) {
        this.user = username;
        this.pass = password;
        if(!StringUtils.hasLength(password)) {
            this.pass = this.isAnonymousLogin() ? Preferences.instance().getProperty("connection.login.anon.pass") : password;
        }
    }

    /**
     * @return true if the username is anononymous
     */
    public boolean isAnonymousLogin() {
        return Preferences.instance().getProperty("connection.login.anon.name").equals(this.getUsername());
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

    /**
     * The path for the private key file to use for public key authentication; e.g. ~/.ssh/id_rsa
     *
     * @param file
     */
    public void setPrivateKeyFile(String file) {
        this.privateKeyFile = file;
    }

    /**
     * @return The path to the private key file to use for public key authentication
     */
    public String getPrivateKeyFile() {
        return this.privateKeyFile;
    }

    public boolean isValid() {
        if(this.usesPublicKeyAuthentication()) {
            return true;
        }
        return StringUtils.hasLength(this.getUsername()) && this.getPassword() != null;
    }
}
