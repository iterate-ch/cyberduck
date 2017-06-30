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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

public class SDSDirectoryFeature implements Directory {

    private final SDSSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    private final SDSNodeIdProvider idProvider;

    public SDSDirectoryFeature(final SDSSession session) {
        this.session = session;
        this.idProvider = new SDSNodeIdProvider(session);
    }

    @Override
    public Path mkdir(final Path folder, final String region, final TransferStatus status) throws BackgroundException {
        if(containerService.isContainer(folder)) {
            final CreateRoomRequest roomRequest = new CreateRoomRequest();
            roomRequest.addAdminIdsItem(session.getUserId());
            roomRequest.setAdminGroupIds(null);
            roomRequest.setName(folder.getName());
            try {
                final Node r = new NodesApi(session.getClient()).createRoom(session.getToken(), null, roomRequest);
                return new Path(folder.getParent(), folder.getName(), EnumSet.of(Path.Type.directory, Path.Type.volume),
                        new PathAttributes(folder.attributes()).withVersionId(String.valueOf(r.getId())));
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map("Cannot create data room {0}", e, folder);
            }
        }
        else {
            try {
                final CreateFolderRequest folderRequest = new CreateFolderRequest();
                folderRequest.setParentId(Long.parseLong(idProvider.getFileid(folder.getParent(), new DisabledListProgressListener())));
                folderRequest.setName(folder.getName());
                final Node f = new NodesApi(session.getClient()).createFolder(session.getToken(), folderRequest, null);
                return new Path(folder.getParent(), folder.getName(), folder.getType(),
                        new PathAttributes(folder.attributes()).withVersionId(String.valueOf(f.getId())));
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService().map("Cannot create folder {0}", e, folder);
            }
        }
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return true;
    }

    @Override
    public Directory withWriter(final Write writer) {
        return this;
    }
}
