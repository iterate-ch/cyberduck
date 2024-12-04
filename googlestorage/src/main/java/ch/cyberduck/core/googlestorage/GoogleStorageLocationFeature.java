package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.HostPreferences;

import java.util.Set;

public class GoogleStorageLocationFeature implements Location {

    private final GoogleStorageSession session;
    private final PathContainerService containerService;

    public GoogleStorageLocationFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = new GoogleStoragePathContainerService();
    }

    @Override
    public Name getDefault() {
        return new GoogleStorageRegion(new HostPreferences(session.getHost()).getProperty("googlestorage.location"));
    }

    @Override
    public Set<Name> getLocations() {
        return session.getHost().getProtocol().getRegions();
    }

    @Override
    public Name getLocation(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(container.isRoot()) {
            return unknown;
        }
        return new GoogleStorageRegion(new GoogleStorageAttributesFinderFeature(session).find(container).getRegion());
    }

    public static final class GoogleStorageRegion extends Name {
        public GoogleStorageRegion(final String identifier) {
            super(identifier);
        }

        @Override
        public String toString() {
            final String identifier = getIdentifier();
            if(null == identifier) {
                return LocaleFactory.localizedString("Unknown");
            }
            return LocaleFactory.localizedString(identifier, "S3");
        }
    }
}
