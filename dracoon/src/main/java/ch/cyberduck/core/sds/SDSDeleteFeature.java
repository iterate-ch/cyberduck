package ch.cyberduck.core.sds;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SDSDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(SDSDeleteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSDeleteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            try {
                new NodesApi(session.getClient()).deleteNode(
                    Long.parseLong(nodeid.getFileid(file, new DisabledListProgressListener())), StringUtils.EMPTY);
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path node) {
        if(containerService.isContainer(node)) {
            // you need the manage permission on the parent data room to delete it
            final Path parent = containerService.getContainer(node.getParent());
            if(parent.equals(node)) {
                // top-level data room
                return this.getRoles(node).contains(SDSPermissionsFeature.MANAGE_ROLE);
            }
            // sub data room
            return this.getRoles(parent).contains(SDSPermissionsFeature.MANAGE_ROLE);
        }
        return this.getRoles(node).contains(SDSPermissionsFeature.DELETE_ROLE);
    }

    private Set<Acl.Role> getRoles(final Path file) {
        final Acl acl = file.attributes().getAcl();
        final Set<Acl.Role> roles;
        try {
            roles = acl.get(new Acl.CanonicalUser(String.valueOf(session.userAccount().getId())));
        }
        catch(BackgroundException e) {
            log.warn(String.format("Unable to retrieve user account information. %s", e.getDetail()));
            return Collections.emptySet();
        }
        return roles != null ? roles : Collections.emptySet();
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
