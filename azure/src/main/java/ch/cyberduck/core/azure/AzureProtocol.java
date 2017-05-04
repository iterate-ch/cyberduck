package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

public class AzureProtocol extends AbstractProtocol {

    @Override
    public String getName() {
        return "Azure";
    }

    /**
     * URL format: Blobs are addressable using the following URL format:
     * http://<storage account>.blob.core.windows.net/<container>/<blob>
     */
    @Override
    public String getDefaultHostname() {
        return "<storageaccount>.blob.core.windows.net";
    }

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Storage Account Name", "Azure");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Primary Access Key", "Azure");
    }

    @Override
    public boolean isAnonymousConfigurable() {
        return false;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public boolean isPortConfigurable() {
        return false;
    }

    @Override
    public String getIdentifier() {
        return "azure";
    }

    @Override
    public String getDescription() {
        return "Windows Azure Storage";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String[] getSchemes() {
        return new String[]{this.getScheme().name(), "azure"};
    }
}
