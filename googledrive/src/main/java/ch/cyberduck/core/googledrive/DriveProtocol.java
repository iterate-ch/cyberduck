package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

public class DriveProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return "googledrive";
    }

    @Override
    public String getDescription() {
        return "Google Drive";
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", DriveProtocol.class.getPackage().getName(), "Drive");
    }

    @Override
    public String getDefaultHostname() {
        return "www.googleapis.com";
    }

    @Override
    public boolean isHostnameConfigurable() {
        return false;
    }

    @Override
    public boolean isPasswordConfigurable() {
        // Only provide account email
        return false;
    }

    @Override
    public boolean isPortConfigurable() {
        return false;
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public String getUsernamePlaceholder() {
        return "Google Account Email";
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Authorization code", "Credentials");
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }
}
