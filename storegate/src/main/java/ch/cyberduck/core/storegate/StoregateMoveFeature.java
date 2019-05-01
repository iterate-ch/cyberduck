package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.MoveFileRequest;
import ch.cyberduck.core.transfer.TransferStatus;

public class StoregateMoveFeature implements Move {

    private final StoregateSession session;
    private final StoregateIdProvider nodeid;

    public StoregateMoveFeature(final StoregateSession session, final StoregateIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            final MoveFileRequest move = new MoveFileRequest()
                .name(renamed.getName())
                .parentID(nodeid.getFileid(renamed.getParent(), new DisabledListProgressListener()))
                .mode(MoveFileRequest.ModeEnum.NUMBER_1); // Overwrite

            new FilesApi(session.getClient()).filesMove(
                nodeid.getFileid(file, new DisabledListProgressListener()), move);
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        // Copy original file attributes
        return new Path(renamed.getParent(), renamed.getName(), renamed.getType(),
            new PathAttributes(renamed.attributes()).withVersionId(file.attributes().getVersionId()));
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }
}
