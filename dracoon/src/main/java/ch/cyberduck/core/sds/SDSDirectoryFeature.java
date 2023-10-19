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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.EncryptRoomRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;

public class SDSDirectoryFeature implements Directory<VersionId> {
    private static final Logger log = LogManager.getLogger(SDSDirectoryFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSDirectoryFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                return this.createRoom(folder, new HostPreferences(session.getHost()).getBoolean("sds.create.dataroom.encrypt"));
            }
            else {
                return this.createFolder(folder);
            }
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Cannot create folder {0}", e, folder);
        }
    }

    private Path createFolder(final Path folder) throws BackgroundException, ApiException {
        final CreateFolderRequest folderRequest = new CreateFolderRequest();
        folderRequest.setParentId(Long.parseLong(nodeid.getVersionId(folder.getParent())));
        folderRequest.setName(folder.getName());
        final Node node = new NodesApi(session.getClient()).createFolder(folderRequest, StringUtils.EMPTY, null);
        nodeid.cache(folder, String.valueOf(node.getId()));
        return folder.withAttributes(new SDSAttributesAdapter(session).toAttributes(node));
    }

    protected Path createRoom(final Path room, final boolean encrypt) throws BackgroundException, ApiException {
        final CreateRoomRequest roomRequest = new CreateRoomRequest();
        roomRequest.setParentId(null);
        final UserAccountWrapper user = session.userAccount();
        roomRequest.addAdminIdsItem(user.getId());
        roomRequest.setAdminGroupIds(null);
        if(!room.getParent().isRoot()) {
            roomRequest.setParentId(Long.parseLong(nodeid.getVersionId(room.getParent())));
        }
        roomRequest.setName(room.getName());
        final Node node = new NodesApi(session.getClient()).createRoom(roomRequest, StringUtils.EMPTY, null);
        nodeid.cache(room, String.valueOf(node.getId()));
        if(encrypt) {
            final EncryptRoomRequest options = new EncryptRoomRequest();
            options.setIsEncrypted(true);
            return room.withType(EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new SDSAttributesAdapter(session).toAttributes(
                            new NodesApi(session.getClient()).encryptRoom(options, Long.valueOf(nodeid.getVersionId(room
                            )), StringUtils.EMPTY, null)));
        }
        else {
            return room.withType(EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new SDSAttributesAdapter(session).toAttributes(node));
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            if(!new HostPreferences(session.getHost()).getBoolean("sds.create.dataroom.enable")) {
                log.warn(String.format("Disallow creating new top level data room %s", filename));
                throw new AccessDeniedException(LocaleFactory.localizedString("Unsupported", "Error")).withFile(workdir);
            }
        }
        if(!SDSTouchFeature.validate(filename)) {
            throw new InvalidFilenameException();
        }
        final SDSPermissionsFeature permissions = new SDSPermissionsFeature(session, nodeid);
        if(!permissions.containsRole(workdir, SDSPermissionsFeature.CREATE_ROLE)) {
            throw new AccessDeniedException(LocaleFactory.localizedString("Unsupported", "Error")).withFile(workdir);
        }
    }

    @Override
    public Directory<VersionId> withWriter(final Write<VersionId> writer) {
        return this;
    }
}
