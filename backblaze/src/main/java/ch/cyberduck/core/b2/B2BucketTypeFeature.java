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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.shared.DefaultAclFeature;

import org.jets3t.service.acl.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import synapticloop.b2.BucketType;
import synapticloop.b2.exception.B2ApiException;

public class B2BucketTypeFeature extends DefaultAclFeature implements AclPermission, Location {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2BucketTypeFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        if(containerService.isContainer(file)) {
            return containerService.getContainer(file).attributes().getAcl();
        }
        return Acl.EMPTY;
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
        if(containerService.isContainer(file)) {
            try {
                for(Acl.UserAndRole userAndRole : acl.asList()) {
                    if(userAndRole.getUser() instanceof Acl.GroupUser) {
                        if(userAndRole.getUser().getIdentifier().equals(Acl.GroupUser.EVERYONE)) {
                            session.getClient().updateBucket(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                                    BucketType.allPublic);
                            return;
                        }
                    }
                }
                session.getClient().updateBucket(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                        BucketType.allPrivate);
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService(session).map("Cannot change permissions of {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return new ArrayList<Acl.User>(Collections.singletonList(
                new Acl.GroupUser(Acl.GroupUser.EVERYONE, false))
        );
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Collections.singletonList(
                new Acl.Role(Permission.PERMISSION_READ.toString()));
    }

    @Override
    public Set<Name> getLocations() {
        final Set<Name> types = new LinkedHashSet<Name>();
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
        for(Acl.UserAndRole role : container.attributes().getAcl().asList()) {
            if(role.getUser().equals(new Acl.GroupUser(Acl.GroupUser.EVERYONE))) {
                if(role.getRole().equals(new Acl.Role(Acl.Role.READ))) {
                    return new B2BucketTypeName(BucketType.allPublic);
                }
            }
        }
        return new B2BucketTypeName(BucketType.allPrivate);
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
