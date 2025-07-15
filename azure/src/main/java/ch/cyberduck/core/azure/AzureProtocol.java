package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;

import org.apache.commons.codec.binary.Base64;

import java.util.Comparator;

public class AzureProtocol extends AbstractProtocol {

    @Override
    public String getName() {
        return "Azure";
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
        return "Windows Azure Blob Storage";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean validate(final Credentials credentials, final LoginOptions options) {
        if(super.validate(credentials, options)) {
            if(options.password) {
                if(credentials.getPassword().length() % 4 != 0) {
                    return false;
                }
                return Base64.isBase64(credentials.getPassword());
            }
            return true;
        }
        return false;
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Comparator<String> getListComparator() {
        return new DefaultLexicographicOrderComparator();
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == PathContainerService.class) {
            return (T) new DirectoryDelimiterPathContainerService();
        }
        return super.getFeature(type);
    }
}
