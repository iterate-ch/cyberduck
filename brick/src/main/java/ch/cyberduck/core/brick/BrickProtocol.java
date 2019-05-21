package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.dav.DAVSSLProtocol;

import org.apache.commons.lang3.StringUtils;

public class BrickProtocol extends DAVSSLProtocol {

    @Override
    public Type getType() {
        return Type.brick;
    }

    @Override
    public String getIdentifier() {
        return Type.brick.name();
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", DAVSSLProtocol.class.getPackage().getName(), StringUtils.upperCase(Type.dav.name()));
    }

    @Override
    public CredentialsConfigurator getCredentialsFinder() {
        return new BrickCredentialsConfigurator();
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isPasswordConfigurable() {
        return true;
    }

    @Override
    public boolean isTokenConfigurable() {
        return true;
    }

    @Override
    public boolean validate(final Credentials credentials, final LoginOptions options) {
        // Will get new pairing key if missing credentials
        return true;
    }
}
