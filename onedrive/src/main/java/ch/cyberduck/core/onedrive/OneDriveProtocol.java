package ch.cyberduck.core.onedrive;

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

public class OneDriveProtocol extends AbstractProtocol {
    @Override
    public String getIdentifier() {
        return "onedrive";
    }

    @Override
    public String getDescription() {
        return "OneDrive";
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", OneDriveProtocol.class.getPackage().getName(), "OneDrive");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public boolean isUsernameConfigurable() {
        return true;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public String getDefaultHostname() {
        return "api.onedrive.com";
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Authorization code", "Credentials");
    }

    @Override
    public boolean isPasswordConfigurable() {
        // Only provide account email
        return false;
    }
}
