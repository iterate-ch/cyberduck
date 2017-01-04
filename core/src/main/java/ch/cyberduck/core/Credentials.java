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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * Stores the login credentials
 */
public class Credentials implements Comparable<Credentials> {

    /**
     * The login name
     */
    private String user;

    /**
     * The login password
     */
    private String password;

    /**
     * Private key identity for SSH public key authentication.
     */
    private Local identity;

    /**
     * Client certificate alias for TLS
     */
    private String certificate;

    /**
     * If the credentials should be stored in the Keychain upon successful login
     */
    private boolean persist = PreferencesFactory.get().getBoolean("connection.login.keychain");

    /**
     * Passed authentication successfully
     */
    private boolean passed;

    /**
     * Default credentials
     */
    public Credentials() {
        //
    }

    public Credentials(final String user) {
        this.user = user;
    }

    /**
     * @param user     Login with this username
     * @param password Passphrase
     */
    public Credentials(final String user, final String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * @return The login identification
     */
    public String getUsername() {
        return user;
    }

    public void setUsername(final String user) {
        this.user = user;
        this.passed = false;
    }

    public Credentials withUsername(final String user) {
        this.user = user;
        this.passed = false;
        return this;
    }

    /**
     * @return The login secret
     */
    public String getPassword() {
        if(StringUtils.isEmpty(password)) {
            if(this.isAnonymousLogin()) {
                return PreferencesFactory.get().getProperty("connection.login.anon.pass");
            }
        }
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
        this.passed = false;
    }

    public Credentials withPassword(final String password) {
        this.password = password;
        this.passed = false;
        return this;
    }

    /**
     * @return true if the password will be added to the system keychain when logged in successfully
     */
    public boolean isSaved() {
        return persist;
    }

    /**
     * Use this to define if passwords should be added to the keychain
     *
     * @param saved If true, the password of the login is added to the keychain upon
     *              successful login
     */
    public void setSaved(final boolean saved) {
        this.persist = saved;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(final boolean passed) {
        this.passed = passed;
    }

    /**
     * @return true if the username is anonymous.
     */
    public boolean isAnonymousLogin() {
        if(StringUtils.isEmpty(user)) {
            return false;
        }
        return PreferencesFactory.get().getProperty("connection.login.anon.name").equals(user);
    }

    /**
     * SSH specific
     *
     * @return true if public key authentication should be used. This is the case, if a
     * private key file has been specified
     * @see #setIdentity
     */
    public boolean isPublicKeyAuthentication() {
        if(null == identity) {
            return false;
        }
        return identity.exists();
    }

    public Credentials withIdentity(final Local file) {
        this.identity = file;
        this.passed = false;
        return this;
    }

    /**
     * @return The path to the private key file to use for public key authentication
     */
    public Local getIdentity() {
        return identity;
    }

    /**
     * The path for the private key file to use for public key authentication; e.g. ~/.ssh/id_rsa
     *
     * @param file Private key file
     */
    public void setIdentity(final Local file) {
        this.identity = file;
        this.passed = false;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(final String certificate) {
        this.certificate = certificate;
    }

    public boolean isCertificateAuthentication() {
        if(null == certificate) {
            return false;
        }
        return true;
    }

    /**
     * @param protocol The protocol to verify against.
     * @param options  Options
     * @return True if the login credential are valid for the given protocol.
     */
    public boolean validate(final Protocol protocol, final LoginOptions options) {
        return protocol.validate(this, options);
    }

    public String getUsernamePlaceholder() {
        return StringUtils.EMPTY;
    }

    public String getPasswordPlaceholder() {
        return StringUtils.EMPTY;
    }

    @Override
    public int compareTo(final Credentials o) {
        if(null == user && null == o.user) {
            return 0;
        }
        if(null == user) {
            return -1;
        }
        if(null == o.user) {
            return 1;
        }
        return user.compareTo(o.user);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof Credentials)) {
            return false;
        }
        final Credentials that = (Credentials) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(password, that.password) &&
                Objects.equals(identity, that.identity) &&
                Objects.equals(certificate, that.certificate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, password, identity, certificate);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Credentials{");
        sb.append("user='").append(user).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
