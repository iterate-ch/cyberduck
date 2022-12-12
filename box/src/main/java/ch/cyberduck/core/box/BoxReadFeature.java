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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

public class BoxReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(BoxReadFeature.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxReadFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final BoxApiClient client = new BoxApiClient(session.getClient());
            final HttpGet request = new HttpGet(String.format("%s/files/%s/content", client.getBasePath(), fileid.getFileId(file)));
            if(status.isAppend()) {
                final HttpRange range = HttpRange.withStatus(status);
                final String header;
                if(-1 == range.getEnd()) {
                    header = String.format("bytes=%d-", range.getStart());
                }
                else {
                    header = String.format("bytes=%d-%d", range.getStart(), range.getEnd());
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Add range header %s for file %s", header, file));
                }
                request.addHeader(new BasicHeader(HttpHeaders.RANGE, header));
            }
            final CloseableHttpResponse response = session.getClient().execute(request);
            return new HttpMethodReleaseInputStream(response, status);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
