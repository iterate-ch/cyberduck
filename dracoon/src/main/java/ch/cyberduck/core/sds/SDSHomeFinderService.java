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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.apache.commons.lang3.StringUtils;

public class SDSHomeFinderService extends DefaultHomeFinderService {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSHomeFinderService(final SDSSession session, final SDSNodeIdProvider nodeid) {
        super(session);
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path find() throws BackgroundException {
        final Path directory = super.find();
        if(directory.isRoot()) {
            // We need to map user roles to ACLs in order to decide if creating a top-level room is allowed
            final Acl acl = new Acl();
            if(session.userAccount().isUserInRole(SDSPermissionsFeature.ROOM_MANAGER_ROLE)) {
                acl.addAll(new Acl.CanonicalUser(), SDSPermissionsFeature.CREATE_ROLE);
            }
            directory.attributes().setAcl(acl);
        }
        else {
            final SDSAttributesFinderFeature feature = new SDSAttributesFinderFeature(session, nodeid);
            try {
                final Node node = new NodesApi(session.getClient()).requestNode(
                    Long.parseLong(nodeid.getFileid(directory, new DisabledListProgressListener())), StringUtils.EMPTY, null);
                directory.setAttributes(feature.toAttributes(node));
                directory.setType(feature.toType(node));
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map("Failure to read attributes of {0}", e, directory);
            }
        }
        return directory;
    }
}
