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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AclPermission;

import org.jets3t.service.acl.Permission;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import synapticloop.b2.BucketType;
import synapticloop.b2.exception.B2ApiException;

public class B2BucketTypeFeature implements AclPermission {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2BucketTypeFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public Acl getPermission(final Path file) throws BackgroundException {
        for(Path bucket : new B2BucketListService(session).list(new DisabledListProgressListener())) {
            if(bucket.equals(containerService.getContainer(file))) {
                return bucket.attributes().getAcl();
            }
        }
        throw new NotfoundException(file.getAbsolute());
    }

    @Override
    public void setPermission(final Path file, final Acl acl) throws BackgroundException {
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
            throw new B2ExceptionMappingService().map("Cannot change permissions of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public List<Acl.User> getAvailableAclUsers() {
        return new ArrayList<Acl.User>(Arrays.asList(
                new Acl.GroupUser(Acl.GroupUser.EVERYONE, false))
        );
    }

    @Override
    public List<Acl.Role> getAvailableAclRoles(final List<Path> files) {
        return Arrays.asList(
                new Acl.Role(Permission.PERMISSION_READ.toString()));
    }
}
