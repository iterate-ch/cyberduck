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

import ch.cyberduck.core.CaseInsensitivePathPredicate;
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
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNode;
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;

public class SDSMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(SDSMoveFeature.class);

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
            final long nodeId = Long.parseLong(nodeid.getFileid(file, new DisabledListProgressListener()));
            if(containerService.isContainer(file)) {
                return new Path(renamed.getParent(), renamed.getName(), renamed.getType(), new SDSAttributesFinderFeature(session, nodeid).toAttributes(
                    new NodesApi(session.getClient()).updateRoom(
                        new UpdateRoomRequest().name(renamed.getName()), nodeId, StringUtils.EMPTY, null)
                ));
            }
            else {
                if(status.isExists()) {
                    // Handle case insensitive. Find feature will have reported target to exist if same name with different case
                    if(!new CaseInsensitivePathPredicate(file).test(renamed)) {
                        log.warn(String.format("Delete existing file %s", renamed));
                        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
                    }
                }
                new NodesApi(session.getClient()).moveNodes(
                    new MoveNodesRequest()
                        .resolutionStrategy(MoveNodesRequest.ResolutionStrategyEnum.OVERWRITE)
                        .addItemsItem(new MoveNode().id(nodeId).name(renamed.getName()))
                        .addNodeIdsItem(nodeId),
                    Long.parseLong(nodeid.getFileid(renamed.getParent(), new DisabledListProgressListener())),
                    StringUtils.EMPTY, null);
                // Copy original file attributes
                return new Path(renamed.getParent(), renamed.getName(), renamed.getType(),
                    new PathAttributes(renamed.attributes()).withVersionId(file.attributes().getVersionId()));
            }
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
                log.warn(String.format("Deny moving data room %s", source));
                return false;
            }
        }
        if(target.getParent().isRoot() && !source.getParent().isRoot()) {
            // Cannot move file or directory to root but only rename data rooms
            log.warn(String.format("Deny moving file %s to root", source));
            return false;
        }
        if(!new SDSTouchFeature(session, nodeid).validate(target.getName())) {
            log.warn(String.format("Validation failed for target name %s", target));
            return false;
        }
        final SDSPermissionsFeature acl = new SDSPermissionsFeature(session, nodeid);
        if(!new SimplePathPredicate(source.getParent()).test(target.getParent())) {
            // Change parent node
            if(!acl.containsRole(source, SDSPermissionsFeature.CHANGE_ROLE) &&
                acl.containsRole(source, SDSPermissionsFeature.DELETE_ROLE) &&
                acl.containsRole(target, SDSPermissionsFeature.CREATE_ROLE)) {
                log.warn(String.format("Deny move of %s with missing permissions for user", source));
                return false;
            }
            return true;
        }
        if(!acl.containsRole(source, SDSPermissionsFeature.CHANGE_ROLE)) {
            log.warn(String.format("Deny move of %s with missing permissions for user", source));
            return false;
        }
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }
}
