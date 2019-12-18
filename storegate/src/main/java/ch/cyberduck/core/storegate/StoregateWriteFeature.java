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
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.JSON;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
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
import java.util.Collections;

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
            private HttpResponse startUpload(final Path file, final TransferStatus status) throws BackgroundException, IOException {
                final StoregateApiClient client = session.getClient();
                final HttpEntityEnclosingRequestBase request;
                request = new HttpPost(String.format("%s/v4/upload/resumable", client.getBasePath()));
                FileMetadata meta = new FileMetadata();
                meta.setId(StringUtils.EMPTY);
                if(status.isHidden()) {
                    meta.setAttributes(2); // Hidden
                }
                else {
                    meta.setAttributes(0);
                }
                meta.setFlags(FileMetadata.FlagsEnum.NUMBER_0);
                if(status.getLockId() != null) {
                    request.addHeader("X-Lock-Id", status.getLockId().toString());
                }
                meta.setFileName(file.getName());
                meta.setParentId(fileid.getFileid(file.getParent(), new DisabledListProgressListener()));
                meta.setFileSize(status.getLength());
                meta.setCreated(DateTime.now());
                if(null != status.getTimestamp()) {
                    meta.setModified(new DateTime(status.getTimestamp()));
                }
                request.setEntity(new StringEntity(new JSON().getContext(meta.getClass()).writeValueAsString(meta),
                    ContentType.create("application/json", StandardCharsets.UTF_8.name())));
                request.addHeader(HTTP.CONTENT_TYPE, MEDIA_TYPE);
                final CloseableHttpResponse response = client.getClient().execute(request);
                try {
                    switch(response.getStatusLine().getStatusCode()) {
                        case HttpStatus.SC_OK:
                            break;
                        default:
                            throw new StoregateExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                EntityUtils.toString(response.getEntity())));
                    }
                }
                finally {
                    EntityUtils.consume(response.getEntity());
                }
                return response;
            }

            @Override
            public VersionId call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    // Initiate a resumable upload
                    HttpResponse response;
                    try {
                        response = this.startUpload(file, status);
                    }
                    catch(InteroperabilityException e) {
                        if(null == status.getLockId()) {
                            throw e;
                        }
                        response = this.startUpload(file, status.withLockId(null));
                    }
                    if(response.containsHeader(HttpHeaders.LOCATION)) {
                        final String putTarget = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
                        // Upload the file
                        final HttpPut put = new HttpPut(putTarget);
                        put.setEntity(entity);
                        final String header;
                        if(status.getLength() == 0) {
                            // Touch
                            header = "*/0";
                        }
                        else {
                            final HttpRange range = HttpRange.byLength(0, status.getLength());
                            header = String.format("%d-%d/%d", range.getStart(), range.getEnd(), status.getLength());
                        }
                        put.addHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %s", header));
                        final StoregateApiClient client = session.getClient();
                        final HttpResponse putResponse = client.getClient().execute(put);
                        try {
                            switch(putResponse.getStatusLine().getStatusCode()) {
                                case HttpStatus.SC_OK:
                                case HttpStatus.SC_CREATED:
                                    final FileMetadata result = new JSON().getContext(FileMetadata.class).readValue(new InputStreamReader(putResponse.getEntity().getContent(), StandardCharsets.UTF_8),
                                        FileMetadata.class);
                                    final VersionId version = new VersionId(result.getId());
                                    status.setVersion(version);
                                    return version;
                                default:
                                    throw new StoregateExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                        EntityUtils.toString(response.getEntity())));
                            }
                        }
                        finally {
                            EntityUtils.consume(putResponse.getEntity());
                        }
                    }
                    else {
                        throw new StoregateExceptionMappingService().map(new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                            EntityUtils.toString(response.getEntity())));
                    }
                }
                catch(IOException e) {
                    throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
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
