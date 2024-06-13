package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeMove;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeUpdate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.UUID;

public class DeepboxMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(DeepboxMoveFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxMoveFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(status.isExists()) {
                if(log.isWarnEnabled()) {
                    log.warn(String.format("Delete file %s to be replaced with %s", renamed, file));
                }
                new DeepboxDeleteFeature(session, fileid).delete(Collections.singletonList(renamed), callback, delete);
            }
            final String id = fileid.getFileId(file);
            if(id == null) {
                throw new NotfoundException(String.format("Cannot move %s", file));
            }
            final NodeMove nodeMove = new NodeMove();
            nodeMove.setTargetParentNodeId(UUID.fromString(fileid.getFileId(renamed.getParent())));
            final CoreRestControllerApi core = new CoreRestControllerApi(session.getClient());
            core.moveNode(nodeMove, UUID.fromString(id));
            final NodeUpdate nodeUpdate = new NodeUpdate();
            nodeUpdate.setName(renamed.getName());
            core.updateNode(nodeUpdate, UUID.fromString(id));
            fileid.cache(file, null);
            fileid.cache(renamed, id);
            return renamed.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).find(renamed));
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        // TODO cancan
    }
}
