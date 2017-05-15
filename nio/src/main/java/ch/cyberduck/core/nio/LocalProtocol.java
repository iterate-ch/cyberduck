package ch.cyberduck.core.nio;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class LocalProtocol extends AbstractProtocol {

    private static String LOCAL_HOSTNAME;

    static {
        try {
            LOCAL_HOSTNAME = InetAddress.getLocalHost().getHostName();
        }
        catch(UnknownHostException e) {
            LOCAL_HOSTNAME = LocaleFactory.localizedString("Disk");
        }
    }

    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public String getName() {
        return LOCAL_HOSTNAME;
    }

    @Override
    public String getDescription() {
        return this.getName();
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", LocalProtocol.class.getPackage().getName(), "Local");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.file;
    }

    @Override
    public boolean isEncodingConfigurable() {
        return true;
    }

    @Override
    public Type getType() {
        return Type.file;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return false;
    }

    @Override
    public boolean isPortConfigurable() {
        return false;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return false;
    }

    @Override
    public boolean isPasswordConfigurable() {
        return false;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public String getDefaultHostname() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
