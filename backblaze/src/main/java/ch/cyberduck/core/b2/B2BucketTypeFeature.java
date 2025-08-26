package ch.cyberduck.core.b2;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import java.util.LinkedHashSet;
import java.util.Set;

import synapticloop.b2.BucketType;

public class B2BucketTypeFeature implements Location {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2BucketTypeFeature(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    protected BucketType toBucketType(final Acl acl) {
        return acl.asList().stream()
                .filter(userAndRole -> userAndRole.getUser() instanceof Acl.GroupUser)
                .anyMatch(userAndRole -> userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.EVERYONE)) ? BucketType.allPublic : BucketType.allPrivate;
    }

    @Override
    public Name getDefault(final Path file) {
        return new B2BucketTypeName(BucketType.valueOf(HostPreferencesFactory.get(session.getHost()).getProperty("b2.bucket.acl.default")));
    }

    @Override
    public Set<Name> getLocations(final Path file) {
        final Set<Name> types = new LinkedHashSet<>();
        types.add(new B2BucketTypeName(BucketType.allPrivate));
        types.add(new B2BucketTypeName(BucketType.allPublic));
        return types;
    }

    @Override
    public Name getLocation(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(container.isRoot()) {
            return unknown;
        }
        return new B2BucketTypeName(BucketType.valueOf(new B2AttributesFinderFeature(session, fileid).find(container).getRegion()));
    }

    public static final class B2BucketTypeName extends Name {

        private final String description;

        public B2BucketTypeName(final BucketType type) {
            super(type.name());
            switch(type) {
                case allPublic:
                    description = LocaleFactory.localizedString("Public", "B2");
                    break;
                case allPrivate:
                    description = LocaleFactory.localizedString("Private", "B2");
                    break;
                default:
                    description = LocaleFactory.localizedString("Unknown");
            }
        }

        @Override
        public String toString() {
            return description;
        }
    }
}
