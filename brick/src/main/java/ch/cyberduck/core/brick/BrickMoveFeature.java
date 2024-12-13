package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FileActionsApi;
import ch.cyberduck.core.brick.io.swagger.client.model.FileActionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.MovePathBody;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;

public class BrickMoveFeature extends BrickFileMigrationFeature implements Move {
    private static final Logger log = LogManager.getLogger(BrickMoveFeature.class);

    private final BrickSession session;

    public BrickMoveFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public Path move(final Path file, final Path target, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            final BrickApiClient client = new BrickApiClient(session);
            if(status.isExists()) {
                if(!new CaseInsensitivePathPredicate(file).test(target)) {
                    log.warn("Delete file {} to be replaced with {}", target, file);
                    new BrickDeleteFeature(session).delete(Collections.singletonList(target), callback, delete);
                }
            }
            final FileActionEntity entity = new FileActionsApi(client)
                    .move(new MovePathBody().destination(StringUtils.removeStart(target.getAbsolute(), String.valueOf(Path.DELIMITER))),
                            StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)));
            if(entity.getFileMigrationId() != null) {
                this.poll(client, entity);
            }
            return target.withAttributes(file.attributes());
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        Move.super.preflight(source, target);
    }
}
