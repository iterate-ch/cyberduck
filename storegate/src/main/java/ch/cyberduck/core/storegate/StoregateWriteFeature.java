package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class StoregateWriteFeature extends AbstractHttpWriteFeature<VersionId> {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;
    private final Find finder;
    private final AttributesFinder attributes;

    public StoregateWriteFeature(final StoregateSession session, final StoregateIdProvider nodeid) {
        this(session, nodeid, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public StoregateWriteFeature(final StoregateSession session, final StoregateIdProvider fileid, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.fileid = fileid;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
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
    public HttpResponseOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<VersionId> command = new DelayedHttpEntityCallable<VersionId>() {
            @Override
            public VersionId call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    // Initiate a resumable upload
                    final HttpEntityEnclosingRequestBase request;
                    request = new HttpPost("/v4/resumable");

                    FileMetadata meta = new FileMetadata();
                    meta.setFileName(file.getName());
                    meta.setParentId(fileid.getFileid(file.getParent(), new DisabledListProgressListener()));
                    meta.setFileSize(0L);
                    meta.setCreated(new DateTime(file.attributes().getCreationDate()));
                    meta.setModified(new DateTime(file.attributes().getModificationDate()));
                    request.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(meta),
                        ContentType.create("application/json", "UTF-8")));
                    request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
                    final HttpClient client = session.getClient().getClient();
                    final HttpResponse response = client.execute(request);
                    try {
                        switch(response.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                                break;
                            default:
                                //TODO
                                /*
                                throw new DriveExceptionMappingService().map(new HttpResponseException(
                                    response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));

                                 */
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
                            final HttpResponse putResponse = client.execute(put);
                            try {
                                switch(putResponse.getStatusLine().getStatusCode()) {
                                    case HttpStatus.SC_OK:
                                    case HttpStatus.SC_CREATED:
                                        final File result = new ObjectMapper().readValue(new InputStreamReader(putResponse.getEntity().getContent(), StandardCharsets.UTF_8),
                                            File.class);
                                        return new VersionId(result.getId());
                                    default:
                                        //TODO
                                        /*
                                        throw new DriveExceptionMappingService().map(new HttpResponseException(
                                            putResponse.getStatusLine().getStatusCode(), putResponse.getStatusLine().getReasonPhrase()));

                                         */
                                }
                            }
                            finally {
                                EntityUtils.consume(putResponse.getEntity());
                            }
                        }
                        else {
                            //TODO
                            /*
                            throw new DriveExceptionMappingService().map(new HttpResponseException(
                                response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));*/
                        }
                    }
                    return null;
                }
                catch(IOException e) {
                    //TODO
                    //throw new DriveExceptionMappingService().map("Upload failed", e, file);
                }

                return null;
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }
}
