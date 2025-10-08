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
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.synchronization.ChainedComparisonService;
import ch.cyberduck.core.synchronization.ChecksumComparisonService;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;
import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;

import java.util.Comparator;

import com.microsoft.azure.storage.core.Base64;
import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
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
                return Base64.validateIsBase64String(credentials.getPassword());
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
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ChainedComparisonService(new ChecksumComparisonService(), new ETagComparisonService()), ComparisonService.disabled);
        }
        return super.getFeature(type);
    }
}
