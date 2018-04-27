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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
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
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSMoveFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        try {
            if(status.isExists()) {
                new SDSDeleteFeature(session, nodeid).delete(Collections.singletonList(renamed), connectionCallback, callback);
            }
            if(!new SimplePathPredicate(file.getParent()).test(renamed.getParent())) {
                // Change parent node
                new NodesApi(session.getClient()).moveNodes(StringUtils.EMPTY,
                    Long.parseLong(nodeid.getFileid(renamed.getParent(), new DisabledListProgressListener())),
                    new MoveNodesRequest().resolutionStrategy(MoveNodesRequest.ResolutionStrategyEnum.OVERWRITE).addNodeIdsItem(
                        Long.parseLong(nodeid.getFileid(file,
                            new DisabledListProgressListener()))), null);
            }
            if(!StringUtils.equals(file.getName(), renamed.getName())) {
                if(containerService.isContainer(file)) {
                    new NodesApi(session.getClient()).updateRoom(StringUtils.EMPTY,
                        Long.parseLong(nodeid.getFileid(file, new DisabledListProgressListener())),
                        new UpdateRoomRequest().name(renamed.getName()), null);
                }
                // Rename
                else if(file.isDirectory()) {
                    new NodesApi(session.getClient()).updateFolder(StringUtils.EMPTY,
                        Long.parseLong(nodeid.getFileid(
                            new Path(renamed.getParent(), file.getName(), file.getType()), new DisabledListProgressListener())),
                        new UpdateFolderRequest().name(renamed.getName()), null);
                }
                else {
                    new NodesApi(session.getClient()).updateFile(StringUtils.EMPTY,
                        Long.parseLong(nodeid.getFileid(
                            new Path(renamed.getParent(), file.getName(), file.getType()), new DisabledListProgressListener())),
                        new UpdateFileRequest().name(renamed.getName()), null);
                }
            }
            return new Path(renamed.getParent(), renamed.getName(), renamed.getType(),
                new PathAttributes(renamed.attributes()).withVersionId(file.attributes().getVersionId()));
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(containerService.isContainer(source)) {
            if(!new SimplePathPredicate(source.getParent()).test(target.getParent())) {
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
