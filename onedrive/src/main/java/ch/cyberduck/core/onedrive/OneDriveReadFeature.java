package ch.cyberduck.core.onedrive;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;

import java.io.IOException;
import java.io.InputStream;

public class OneDriveReadFeature implements Read {
    private static final Logger log = Logger.getLogger(OneDriveReadFeature.class);

    private final OneDriveSession session;

    public OneDriveReadFeature(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(-1 == range.getEnd()) {
                    header = String.format("%d-", range.getStart());
                }
                else {
                    header = String.format("%d-%d", range.getStart(), range.getEnd());
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Add range header %s for file %s", header, file));
                }
                return session.toFile(file).download(header);
            }
            return session.toFile(file).download();
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) throws BackgroundException {
        return false;
    }
}
