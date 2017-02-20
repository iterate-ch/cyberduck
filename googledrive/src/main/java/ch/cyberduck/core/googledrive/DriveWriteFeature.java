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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class DriveWriteFeature extends AbstractHttpWriteFeature<Void> implements Write<Void> {

    private final DriveSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public DriveWriteFeature(final DriveSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public DriveWriteFeature(final DriveSession session, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
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
    public HttpResponseOutputStream<Void> write(final Path file, final TransferStatus status) throws BackgroundException {
        final DelayedHttpEntityCallable<Void> command = new DelayedHttpEntityCallable<Void>() {
            @Override
            public Void call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final String base = session.getClient().getRootUrl();
                    // Initiate a resumable upload
                    final HttpEntityEnclosingRequestBase request;
                    if(status.isExists()) {
                        final String fileid = new DriveFileidProvider(session).getFileid(file);
                        request = new HttpPatch(String.format("%s/upload/drive/v3/files/%s", base, fileid));
                        // Upload the file
                        request.setEntity(entity);
                    }
                    else {
                        request = new HttpPost(String.format("%s/upload/drive/v3/files?uploadType=resumable", base));
                        request.setEntity(new StringEntity("{\"name\": \""
                                + file.getName() + "\", \"parents\": [\""
                                + new DriveFileidProvider(session).getFileid(file.getParent()) + "\"]}",
                                ContentType.create("application/json", "UTF-8")));
                    }
                    if(StringUtils.isNotBlank(status.getMime())) {
                        // Set to the media MIME type of the upload data to be transferred in subsequent requests.
                        request.addHeader("X-Upload-Content-Type", status.getMime());
                    }
                    request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
                    request.addHeader(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", session.getTokens().getAccessToken()));
                    final CloseableHttpClient client = session.getBuilder().build(new DisabledTranscriptListener()).build();
                    final CloseableHttpResponse response = client.execute(request);
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                break;
                            default:
                                throw new DriveExceptionMappingService().map(new HttpResponseException(
                                        response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                        }
                    }
                    finally {
                        EntityUtils.consume(response.getEntity());
                    }
                    if(!status.isExists()) {
                        if(response.containsHeader(HttpHeaders.LOCATION)) {
                            final String putTarget = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                            // Upload the file
                            final HttpPut put = new HttpPut(putTarget);
                            put.setEntity(entity);
                            final CloseableHttpResponse putResponse = client.execute(put);
                            try {
                                switch(putResponse.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                    case HttpStatus.SC_CREATED:
                                        break;
                                    default:
                                        throw new DriveExceptionMappingService().map(new HttpResponseException(
                                                putResponse.getStatusLine().getStatusCode(), putResponse.getStatusLine().getReasonPhrase()));
                                }
                            }
                            finally {
                                EntityUtils.consume(putResponse.getEntity());
                            }
                        }
                        else {
                            throw new DriveExceptionMappingService().map(new HttpResponseException(
                                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                        }
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

    @Override
    public ChecksumCompute checksum() {
        return new DisabledChecksumCompute();
    }
}
