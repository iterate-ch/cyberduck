package ch.cyberduck.core.openstack;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.synchronization.ComparisonService;
import ch.cyberduck.core.synchronization.DefaultComparisonService;
import ch.cyberduck.core.synchronization.ETagComparisonService;
import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;

import java.util.Comparator;

public class SwiftProtocol extends AbstractProtocol {
    @Override
    public String getName() {
        return LocaleFactory.localizedString("Swift", "Mosso");
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("Swift (OpenStack Object Storage)", "Mosso");
    }

    @Override
    public String getIdentifier() {
        return "swift";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public String getContext() {
        return PreferencesFactory.get().getProperty("openstack.authentication.context");
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
    @SuppressWarnings("unchecked")
    public <T> T getFeature(final Class<T> type) {
        if(type == PathContainerService.class) {
            return (T) new DefaultPathContainerService();
        }
        if(type == ComparisonService.class) {
            return (T) new DefaultComparisonService(new ETagComparisonService(), ComparisonService.disabled);
        }
        return super.getFeature(type);
    }
}
