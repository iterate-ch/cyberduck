package ch.cyberduck.core.box;

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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.FoldersApi;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersBody;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersParent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Collections;

public class BoxDirectoryFeature implements Directory {

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxDirectoryFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            return folder.withAttributes(new BoxAttributesFinderFeature(session, fileid).toAttributes(
                    new FoldersApi(new BoxApiClient(session.getClient())).postFolders(new FoldersBody()
                            .parent(new FoldersParent().id(fileid.getFileId(folder.getParent())))
                            .name(folder.getName()), Collections.emptyList())));
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public Directory withWriter(final Write writer) {
        return this;
    }

    @Override
    public boolean isSupported(final Path workdir, final String name) {
        return new BoxTouchFeature(session, fileid).isSupported(workdir, name);
    }
}
