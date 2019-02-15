package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

public class SDSHomeFinderService extends DefaultHomeFinderService {

    private final SDSSession session;

    public SDSHomeFinderService(final SDSSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path path = super.find();
        if(path.isRoot()) {
            // We need to map user roles to ACLs in order to decide if creating a top-level room is allowed
            final Acl acl = new Acl();
            if(session.userAccount().isUserInRole(SDSPermissionsFeature.ROOM_MANAGER_ROLE)) {
                final Acl.User user = new Acl.CanonicalUser();
                acl.addAll(user, SDSPermissionsFeature.CREATE_ROLE);
            }
            path.attributes().setAcl(acl);
        }
        return path;
    }
}
