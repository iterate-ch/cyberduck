package ch.cyberduck.core.hubic;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.openstack.SwiftProtocol;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class HubicProtocol extends SwiftProtocol {

    @Override
    public String getIdentifier() {
        return "hubic";
    }

    @Override
    public String getDescription() {
        return "hubiC";
    }

    @Override
    public Type getType() {
        return Type.swift;
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", HubicProtocol.class.getPackage().getName(), "Hubic");
    }

    @Override
    public String getDefaultHostname() {
        return "api.hubic.com";
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
    public String getUsernamePlaceholder() {
        return "Email";
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Authorization code", "Credentials");
    }

    @Override
    public boolean isUsernameConfigurable() {
        return false;
    }

    @Override
    public boolean isPasswordConfigurable() {
        // Only provide account email
        return false;
    }

    @Override
    public String disk() {
        return new SwiftProtocol().disk();
    }

    @Override
    public String icon() {
        return new SwiftProtocol().icon();
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.none;
    }
}
