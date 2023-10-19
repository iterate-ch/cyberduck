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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNode;
import ch.cyberduck.core.sds.io.swagger.client.model.MoveNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFileRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.UpdateRoomRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;

public class SDSMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(SDSMoveFeature.class);

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
            final long nodeId = Long.parseLong(nodeid.getVersionId(file));
            if(containerService.isContainer(file)) {
                final Node node = new NodesApi(session.getClient()).updateRoom(
                    new UpdateRoomRequest().name(renamed.getName()), nodeId, StringUtils.EMPTY, null);
                nodeid.cache(renamed, file.attributes().getVersionId());
                nodeid.cache(file, null);
                return renamed.withAttributes(new SDSAttributesAdapter(session).toAttributes(node));
            }
            else {
                if(status.isExists()) {
                    // Handle case insensitive. Find feature will have reported target to exist if same name with different case
                    if(!new CaseInsensitivePathPredicate(file).test(renamed)) {
                        log.warn(String.format("Delete existing file %s", renamed));
                        new SDSDeleteFeature(session, nodeid).delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
                    }
                }
                if(new SimplePathPredicate(file.getParent()).test(renamed.getParent())) {
                    // Rename only
                    if(file.isDirectory()) {
                        new NodesApi(session.getClient()).updateFolder(new UpdateFolderRequest().name(renamed.getName()), nodeId, StringUtils.EMPTY, null);
                    }
                    else {
                        new NodesApi(session.getClient()).updateFile(new UpdateFileRequest().name(renamed.getName()), nodeId, StringUtils.EMPTY, null);
                    }
                }
                else {
                    // Move to different parent
                    new NodesApi(session.getClient()).moveNodes(
                        new MoveNodesRequest()
                            .resolutionStrategy(MoveNodesRequest.ResolutionStrategyEnum.OVERWRITE)
                            .addItemsItem(new MoveNode().id(nodeId).name(renamed.getName()))
                            .keepShareLinks(new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep")),
                        Long.parseLong(nodeid.getVersionId(renamed.getParent())),
                        StringUtils.EMPTY, null);
                }
                nodeid.cache(renamed, file.attributes().getVersionId());
                nodeid.cache(file, null);
                // Copy original file attributes
                return renamed.withAttributes(new PathAttributes(file.attributes()).withVersionId(String.valueOf(nodeId)));
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(containerService.isContainer(source)) {
            if(!new SimplePathPredicate(source.getParent()).test(target.getParent())) {
                // Cannot move data room but only rename
                log.warn(String.format("Deny moving data room %s", source));
                throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
            }
        }
        if(target.getParent().isRoot() && !source.getParent().isRoot()) {
            // Cannot move file or directory to root but only rename data rooms
            log.warn(String.format("Deny moving file %s to root", source));
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
        }
        if(!SDSTouchFeature.validate(target.getName())) {
            log.warn(String.format("Validation failed for target name %s", target));
            throw new InvalidFilenameException().withFile(target);
        }
        final SDSPermissionsFeature acl = new SDSPermissionsFeature(session, nodeid);
        if(!new SimplePathPredicate(source.getParent()).test(target.getParent())) {
            // Change parent node
            if(!acl.containsRole(containerService.getContainer(source), SDSPermissionsFeature.CHANGE_ROLE)) {
                log.warn(String.format("Deny move of %s to %s changing parent node with missing role %s on data room %s",
                    source, target, SDSPermissionsFeature.CHANGE_ROLE, containerService.getContainer(source)));
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
            }
            if(!acl.containsRole(containerService.getContainer(source), SDSPermissionsFeature.DELETE_ROLE)) {
                log.warn(String.format("Deny move of %s to %s changing parent node with missing role %s on data room %s",
                    source, target, SDSPermissionsFeature.DELETE_ROLE, containerService.getContainer(source)));
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
            }
            if(!acl.containsRole(containerService.getContainer(target), SDSPermissionsFeature.CREATE_ROLE)) {
                log.warn(String.format("Deny move of %s to %s changing parent node with missing role %s on data room %s",
                    source, target, SDSPermissionsFeature.CREATE_ROLE, containerService.getContainer(target)));
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
            }
        }
        if(!acl.containsRole(containerService.getContainer(source), SDSPermissionsFeature.CHANGE_ROLE)) {
            log.warn(String.format("Deny move of %s to %s with missing permissions for user with missing role %s on data room %s",
                source, target, SDSPermissionsFeature.CHANGE_ROLE, containerService.getContainer(source)));
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source)).withFile(source);
        }
    }
}
