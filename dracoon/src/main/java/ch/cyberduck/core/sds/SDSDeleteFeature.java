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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SDSDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(SDSDeleteFeature.class);

    private final SDSSession session;

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSDeleteFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files) {
            try {
                new NodesApi(session.getClient()).deleteNode(StringUtils.EMPTY,
                    Long.parseLong(new SDSNodeIdProvider(session).getFileid(file, new DisabledListProgressListener())));
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path node) {
        try {
            if(containerService.isContainer(node)) {
                // you need the manage permission on the parent data room to delete it
                final Path parent = containerService.getContainer(node.getParent());
                if(parent.equals(node)) {
                    // top-level data room
                    return this.getRoles(node).contains(SDSAttributesFinderFeature.MANAGE_ROLE);
                }
                // sub data room
                return this.getRoles(parent).contains(SDSAttributesFinderFeature.MANAGE_ROLE);
            }
            return this.getRoles(node).contains(SDSAttributesFinderFeature.DELETE_ROLE);
        }
        catch(BackgroundException e) {
            log.warn(String.format("Unable to retrieve user account information. %s", e.getDetail()));
            return true;
        }
    }

    private Set<Acl.Role> getRoles(final Path node) throws BackgroundException {
        final Set<Acl.Role> roles = containerService.getContainer(node).attributes().getAcl().get(new Acl.CanonicalUser(String.valueOf(session.userAccount().getId())));
        return roles != null ? roles : new HashSet<Acl.Role>();
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
