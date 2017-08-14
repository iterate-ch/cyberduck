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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.http.HttpMethodReleaseInputStream;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.webloc.UrlFileWriterFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.NullInputStream;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class DriveReadFeature implements Read {
    private static final Logger log = Logger.getLogger(DriveReadFeature.class);

    private final DriveSession session;

    public DriveReadFeature(final DriveSession session) {
        this.session = session;
    }

    @Override
    public boolean offset(Path file) {
        return true;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(file.getType().contains(AbstractPath.Type.placeholder)) {
                final DescriptiveUrl link = new DriveUrlProvider().toUrl(file).find(DescriptiveUrl.Type.http);
                if(DescriptiveUrl.EMPTY.equals(link)) {
                    log.warn(String.format("Missing web link for file %s", file));
                    return new NullInputStream(file.attributes().getSize());
                }
                // Write web link file
                return IOUtils.toInputStream(UrlFileWriterFactory.get().write(link), Charset.defaultCharset());
            }
            else {
                final String base = session.getClient().getRootUrl();
                final HttpUriRequest request = new HttpGet(String.format("%s/drive/v3/files/%s?alt=media", base,
                        new DriveFileidProvider(session).getFileid(file, new DisabledListProgressListener())));
                request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
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
                    // Disable compression
                    request.addHeader(new BasicHeader(HttpHeaders.ACCEPT_ENCODING, "identity"));
                }
                final HttpClient client = session.getHttpClient();
                final HttpResponse response = client.execute(request);
                switch(response.getStatusLine().getStatusCode()) {
                    case HttpStatus.SC_OK:
                    case HttpStatus.SC_PARTIAL_CONTENT:
                        return new HttpMethodReleaseInputStream(response);
                    default:
                        throw new DriveExceptionMappingService().map(new HttpResponseException(
                                response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                }
            }
        }
        catch(IOException e) {
            throw new DriveExceptionMappingService().map("Download {0} failed", e, file);
        }
    }
}
