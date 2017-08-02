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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFileRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.sds.swagger.MoveNodesRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;

public class SDSMoveFeature implements Move {

    private final SDSSession session;

    private final PathContainerService pathContainerService = new PathContainerService();

    public SDSMoveFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public void move(final Path source, final Path target, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                new SDSDeleteFeature(session).delete(Collections.singletonList(target), new DisabledLoginCallback(), new Delete.DisabledCallback());
            }
            if(!source.getParent().equals(target.getParent())) {
                // Change parent node
                new NodesApi(session.getClient()).moveNodes(StringUtils.EMPTY,
                        Long.parseLong(new SDSNodeIdProvider(session).getFileid(target.getParent(), new DisabledListProgressListener())),
                        new MoveNodesRequest().resolutionStrategy(MoveNodesRequest.ResolutionStrategyEnum.OVERWRITE).addNodeIdsItem(Long.parseLong(new SDSNodeIdProvider(session).getFileid(source, new DisabledListProgressListener()))), null);
            }
            if(!StringUtils.equals(source.getName(), target.getName())) {
                if(pathContainerService.isContainer(source)) {
                    new NodesApi(session.getClient()).updateRoom(StringUtils.EMPTY,
                            Long.parseLong(new SDSNodeIdProvider(session).getFileid(source, new DisabledListProgressListener())),
                            new UpdateRoomRequest().name(target.getName()), null);
                }
                // Rename
                else if(source.isDirectory()) {
                    new NodesApi(session.getClient()).updateFolder(StringUtils.EMPTY,
                            Long.parseLong(new SDSNodeIdProvider(session).getFileid(
                                    new Path(target.getParent(), source.getName(), source.getType()), new DisabledListProgressListener())),
                            new UpdateFolderRequest().name(target.getName()), null);
                }
                else {
                    new NodesApi(session.getClient()).updateFile(StringUtils.EMPTY,
                            Long.parseLong(new SDSNodeIdProvider(session).getFileid(
                                    new Path(target.getParent(), source.getName(), source.getType()), new DisabledListProgressListener())),
                            new UpdateFileRequest().name(target.getName()), null);
                }
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Cannot rename {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(pathContainerService.isContainer(source)) {
            if(!source.getParent().equals(target.getParent())) {
                // Cannot move data room but only rename
                return false;
            }
        }
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }
}
