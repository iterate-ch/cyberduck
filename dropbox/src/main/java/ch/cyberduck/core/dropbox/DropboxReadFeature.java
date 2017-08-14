package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.InputStream;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.DbxUserFilesRequests;
import com.dropbox.core.v2.files.DownloadBuilder;
import com.dropbox.core.v2.files.FileMetadata;

public class DropboxReadFeature implements Read {

    private final DropboxSession session;

    public DropboxReadFeature(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final DownloadBuilder builder = new DbxUserFilesRequests(session.getClient()).downloadBuilder(file.getAbsolute());
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                builder.range(range.getStart());
            }
            final DbxDownloader<FileMetadata> downloader = builder.start();
            return downloader.getInputStream();
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(Path file) throws BackgroundException {
        return true;
    }
}
