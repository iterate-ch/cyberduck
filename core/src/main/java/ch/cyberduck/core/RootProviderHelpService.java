package ch.cyberduck.core;

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

import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

public class RootProviderHelpService implements ProviderHelpService {

    public final String base;

    public RootProviderHelpService() {
        this(PreferencesFactory.get().getProperty("website.help"));
    }

    public RootProviderHelpService(final String base) {
        this.base = base;
    }

    public String help() {
        return this.help(StringUtils.EMPTY);
    }

    @Override
    public String help(final Protocol provider) {
        return this.help(StringUtils.EMPTY);
    }

    @Override
    public String help(final Scheme scheme) {
        return this.help(StringUtils.EMPTY);
    }

    protected String help(final String page) {
        final StringBuilder site = new StringBuilder(base);
        if(StringUtils.isNotBlank(page)) {
            site.append("/").append(page);
        }
        return site.toString();
    }
}
