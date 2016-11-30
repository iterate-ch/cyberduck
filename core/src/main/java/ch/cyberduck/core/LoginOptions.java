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

public final class LoginOptions {

    public boolean user = true;
    public boolean password = true;
    public boolean keychain = true;
    public boolean publickey = false;
    public boolean anonymous = false;
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

    public boolean isKeychain() {
        return keychain;
    }

    public boolean isPublickey() {
        return publickey;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Defer login options from protocol
     */
    public LoginOptions(final Protocol protocol) {
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
        if(anonymous != that.anonymous) {
            return false;
        }
        if(keychain != that.keychain) {
            return false;
        }
        if(publickey != that.publickey) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = (keychain ? 1 : 0);
        result = 31 * result + (publickey ? 1 : 0);
        result = 31 * result + (anonymous ? 1 : 0);
        return result;
    }
}
