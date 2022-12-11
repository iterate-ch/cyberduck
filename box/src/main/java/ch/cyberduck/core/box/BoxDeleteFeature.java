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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.box.io.swagger.client.api.FoldersApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

public class BoxDeleteFeature implements Delete {

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxDeleteFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path f : files.keySet()) {
            try {
                if(f.isDirectory()) {
                    new FoldersApi(new BoxApiClient(session.getClient())).deleteFoldersId(fileid.getFileId(f), null, true);
                }
                else {
                    new FilesApi(new BoxApiClient(session.getClient())).deleteFilesId(fileid.getFileId(f), null);
                }
            }
            catch(ApiException e) {
                throw new BoxExceptionMappingService(fileid).map("Cannot delete {0}", e, f);
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
