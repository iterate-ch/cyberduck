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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FileActionsApi;
import ch.cyberduck.core.brick.io.swagger.client.api.FileMigrationsApi;
import ch.cyberduck.core.brick.io.swagger.client.model.CopyPathBody;
import ch.cyberduck.core.brick.io.swagger.client.model.FileActionEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FileMigrationEntity;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;

public class BrickCopyFeature implements Copy {
    private static final Logger log = Logger.getLogger(BrickCopyFeature.class);

    private final BrickSession session;

    public BrickCopyFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path file, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final BrickApiClient client = new BrickApiClient(session.getApiKey(), session.getClient());
            if(status.isExists()) {
                new BrickDeleteFeature(session).delete(Collections.singletonList(target), callback, new Delete.DisabledCallback());
            }
            final FileActionEntity entity = new FileActionsApi(client)
                .copy(new CopyPathBody().destination(StringUtils.removeStart(target.getAbsolute(), String.valueOf(Path.DELIMITER))),
                    StringUtils.removeStart(file.getAbsolute(), String.valueOf(Path.DELIMITER)));
            if(entity.getFileMigrationId() != null) {
                while(true) {
                    // Poll status
                    final FileMigrationEntity.StatusEnum migration = new FileMigrationsApi(client)
                        .getFileMigrationsId(entity.getFileMigrationId()).getStatus();
                    switch(migration) {
                        case COMPLETE:
                            return target.withAttributes(file.attributes());
                        default:
                            log.warn(String.format("Wait for copy to complete with current status %s", migration));
                            break;
                    }
                }
            }
            return target.withAttributes(file.attributes());
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Cannot copy {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }
}
