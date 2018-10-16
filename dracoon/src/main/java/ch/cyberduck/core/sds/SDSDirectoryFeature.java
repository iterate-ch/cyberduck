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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class SDSDirectoryFeature implements Directory<VersionId> {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSDirectoryFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                final CreateRoomRequest roomRequest = new CreateRoomRequest();
                final UserAccountWrapper user = session.userAccount();
                roomRequest.addAdminIdsItem(user.getId());
                roomRequest.setAdminGroupIds(null);
                if(!folder.getParent().isRoot()) {
                    roomRequest.setParentId(Long.parseLong(nodeid.getFileid(folder.getParent(), new DisabledListProgressListener())));
                }
                roomRequest.setName(folder.getName());
                final Node r = new NodesApi(session.getClient()).createRoom(roomRequest, StringUtils.EMPTY, null);
                return new Path(folder.getParent(), folder.getName(), EnumSet.of(Path.Type.directory, Path.Type.volume),
                    new SDSAttributesFinderFeature(session, nodeid).toAttributes(r));
            }
            else {
                final CreateFolderRequest folderRequest = new CreateFolderRequest();
                folderRequest.setParentId(Long.parseLong(nodeid.getFileid(folder.getParent(), new DisabledListProgressListener())));
                folderRequest.setName(folder.getName());
                final Node f = new NodesApi(session.getClient()).createFolder(folderRequest, StringUtils.EMPTY, null);
                return new Path(folder.getParent(), folder.getName(), folder.getType(),
                    new SDSAttributesFinderFeature(session, nodeid).toAttributes(f));
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public Directory<VersionId> withWriter(final Write<VersionId> writer) {
        return this;
    }
}
