package ch.cyberduck.core.box;/*
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


import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiClient;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.UploadsChunkedApi;
import ch.cyberduck.core.box.io.swagger.client.model.FileIdUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.FilesUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSession;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferStatus;

public class BoxUploadHelper {

    private BoxUploadHelper() {
    }

    public static UploadSession getUploadSession(final TransferStatus status, ApiClient client, Path file, BoxFileidProvider fileid) throws BackgroundException, ApiException {
        if(status.isExists()) {
            return new UploadsChunkedApi(client).postFilesIdUploadSessions(
                fileid.getFileId(file, new DisabledListProgressListener()), new FileIdUploadSessionsBody()
                    .fileName(file.getName())
                    .fileSize(status.getLength()), null);
        }
        else {
            // Creates an upload session for a new file
            return new UploadsChunkedApi(client).postFilesUploadSessions(new FilesUploadSessionsBody()
                .folderId(fileid.getFileId(file.getParent(), new DisabledListProgressListener()))
                .fileName(file.getName())
                .fileSize(status.getLength()));
        }
    }
}
