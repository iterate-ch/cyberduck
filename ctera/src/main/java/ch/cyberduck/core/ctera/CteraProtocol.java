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
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.comparison.DefaultAttributesComparison;
import ch.cyberduck.core.comparison.DisabledAttributesComparison;
import ch.cyberduck.core.comparison.ETagAttributesComparison;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.features.AttributesComparison;
import ch.cyberduck.core.preferences.PreferencesFactory;

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
        return super.validate(credentials, new LoginOptions(options).token(false).password(false).user(false));
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public String getTokenPlaceholder() {
        return "CTERA Token";
    }

    @Override
    public <T> T getFeature(final Class<T> type) {
        if(type == AttributesComparison.class) {
            return (T) new DefaultAttributesComparison(new ETagAttributesComparison(), new DisabledAttributesComparison());
        }
        return super.getFeature(type);
    }
}
