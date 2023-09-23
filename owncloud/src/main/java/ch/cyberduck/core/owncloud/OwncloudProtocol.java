package ch.cyberduck.core.owncloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.CredentialsConfigurator;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.WindowsIntegratedCredentialsConfigurator;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;

public class OwncloudProtocol extends AbstractProtocol {

    @Override
    public Type getType() {
        return Type.owncloud;
    }

    @Override
    public String getIdentifier() {
        return Type.owncloud.name();
    }

    @Override
    public String getDescription() {
        return "ownCloud";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public String disk() {
        return new DAVSSLProtocol().disk();
    }

    @Override
    public String icon() {
        return new DAVSSLProtocol().icon();
    }

    @Override
    public CredentialsConfigurator getCredentialsFinder() {
        return new WindowsIntegratedCredentialsConfigurator();
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.implicit;
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ETagComparisonService(), new ETagComparisonService());
        }
        return super.getFeature(type);
    }
}
