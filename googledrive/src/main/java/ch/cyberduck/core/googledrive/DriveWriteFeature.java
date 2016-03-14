package ch.cyberduck.core.googledrive;

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

import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.ResponseOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

public class DriveWriteFeature extends AbstractHttpWriteFeature<String> {
    private static final Logger log = Logger.getLogger(DriveWriteFeature.class);

    private DriveSession session;

    public DriveWriteFeature(final DriveSession session) {
        super(session);
        this.session = session;
    }

    @Override
    public Append append(final Path file, final Long length, final PathCache cache) throws BackgroundException {
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    @Override
    public ResponseOutputStream<String> write(final Path file, final TransferStatus status) throws BackgroundException {
        final DelayedHttpEntityCallable<String> command = new DelayedHttpEntityCallable<String>() {
            @Override
            public String call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final Drive.Files.Create insert = session.getClient().files().create(new File()
                            .setName(file.getName())
                            .setParents(Collections.singletonList(file.getParent().attributes().getVersionId())));
                    file.attributes().setVersionId(insert.execute().getId());

                    final String base = session.getClient().getBaseUrl();
                    // Upload the media only, without any metadata
                    final HttpPost request = new HttpPost(String.format("%sfiles?uploadType=media", base));
                    request.addHeader(HTTP.EXPECT_DIRECTIVE, HTTP.EXPECT_CONTINUE);
                    request.addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", session.getAccessToken()));
                    request.setEntity(entity);
                    final CloseableHttpResponse response = session.getBuilder().build(new DisabledTranscriptListener()).build().execute(request);
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                break;
                            default:
                                throw new HttpExceptionMappingService().map(new HttpResponseException(
                                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                        }
                    }
                    finally {
                        EntityUtils.consume(response.getEntity());
                    }
                    if(response.containsHeader(HttpHeaders.ETAG)) {
                        return response.getFirstHeader(HttpHeaders.ETAG).getValue();
                    }
                    return null;
                }
                catch(IOException e) {
                    throw new DriveExceptionMappingService().map("Upload failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }
}
