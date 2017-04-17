package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import java.util.Objects;

public final class LoginOptions {

    /**
     * Allow username input
     */
    public boolean user = true;
    /**
     * Allow password input
     */
    public boolean password = true;
    /**
     * Enable option to save password in keychain
     */
    public boolean keychain = true;
    /**
     * Enable option to select public key
     */
    public boolean publickey = false;
    /**
     * Enable option to login as anonymous user
     */
    public boolean anonymous = false;
    /**
     * Set custom icon in login prompt
     */
    public String icon;

    public LoginOptions() {
        //
    }

    public LoginOptions user(boolean e) {
        user = e;
        return this;
    }

    public LoginOptions password(boolean e) {
        password = e;
        return this;
    }

    public LoginOptions keychain(boolean e) {
        keychain = e;
        return this;
    }

    public LoginOptions publickey(boolean e) {
        publickey = e;
        return this;
    }

    public LoginOptions anonymous(boolean e) {
        anonymous = e;
        return this;
    }

    public LoginOptions icon(String icon) {
        this.icon = icon;
        return this;
    }

    public boolean user() {
        return user;
    }

    public boolean password() {
        return password;
    }

    public boolean keychain() {
        return keychain;
    }

    public boolean publickey() {
        return publickey;
    }

    public boolean anonymous() {
        return anonymous;
    }

    public String icon() {
        return icon;
    }

    /**
     * Defer login options from protocol
     */
    public LoginOptions(final Protocol protocol) {
        this.configure(protocol);
    }

    public void configure(final Protocol protocol) {
        publickey = protocol.getType() == Protocol.Type.sftp;
        anonymous = protocol.isAnonymousConfigurable();
        user = protocol.isUsernameConfigurable();
        password = protocol.isPasswordConfigurable();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final LoginOptions that = (LoginOptions) o;
        return user == that.user &&
                password == that.password &&
                keychain == that.keychain &&
                publickey == that.publickey &&
                anonymous == that.anonymous &&
                Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, password, keychain, publickey, anonymous, icon);
    }
}
