package ch.cyberduck.core.b2;

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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.io.InputStream;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2DownloadFileResponse;

public class B2ReadFeature implements Read {

    private final B2Session session;

    public B2ReadFeature(final B2Session session) {
        this.session = session;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                return session.getClient().downloadFileRangeByIdToStream(
                        new B2FileidProvider(session).getFileid(file, new DisabledListProgressListener()),
                        range.getStart(), range.getEnd()
                );
            }
            final B2DownloadFileResponse response = session.getClient().downloadFileById(new B2FileidProvider(session).getFileid(file, new DisabledListProgressListener()));
            return new HttpMethodReleaseInputStream(response.getResponse());
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Download {0} failed", e, file);
        }
    }

    @Override
    public boolean offset(final Path file) {
        return true;
    }
}
