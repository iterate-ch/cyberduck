package ch.cyberduck.core.ctera;

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

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public class CteraProtocol extends AbstractProtocol {

    public static final String CTERA_REDIRECT_URI = String.format("%s:websso",
            PreferencesFactory.get().getProperty("oauth.handler.scheme"));

    @Override
    public Type getType() {
        return Type.ctera;
    }

    @Override
    public String getIdentifier() {
        return Type.ctera.name();
    }

    @Override
    public String getName() {
        return "CTERA";
    }

    @Override
    public String getDescription() {
        return "CTERA Portal";
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
    public boolean isTokenConfigurable() {
        return true;
    }

    @Override
    public boolean validate(final Credentials credentials, final LoginOptions options) {
        if(options.user && options.password && options.token) {
            // No prompt before login when it is determined if login is via SSO or username
            return true;
        }
        return super.validate(credentials, options);
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.storage;
    }

    @Override
    public String getTokenPlaceholder() {
        return "CTERA Token";
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ETagComparisonService(), ComparisonService.disabled);
        }
        return super.getFeature(type);
    }
}
