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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.JSON;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.storegate.io.swagger.client.model.FileMetadata;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.EnumSet;

import static com.google.api.client.json.Json.MEDIA_TYPE;

public class StoregateWriteFeature extends AbstractHttpWriteFeature<File> {
    private static final Logger log = LogManager.getLogger(StoregateWriteFeature.class);

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateWriteFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        super(new StoregateAttributesFinderFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }

    @Override
    public HttpResponseOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>(file) {
            @Override
            public File call(final HttpEntity entity) throws BackgroundException {
                // Initiate a resumable upload
                String location;
                try {
                    location = start(file, status);
                }
                catch(InteroperabilityException e) {
                    if(null == status.getLockId()) {
                        throw e;
                    }
                    location = start(file, status.withLockId(null));
                }
                final StoregateApiClient client = session.getClient();
                try {
                    // Upload the file
                    final HttpPut put = new HttpPut(location);
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
                    final HttpResponse putResponse = client.getClient().execute(put);
                    try {
                        switch(putResponse.getStatusLine().getStatusCode()) {
                            case HttpStatus.SC_OK:
                            case HttpStatus.SC_CREATED:
                                final File result = new JSON().getContext(FileMetadata.class).readValue(
                                        new InputStreamReader(putResponse.getEntity().getContent(), StandardCharsets.UTF_8),
                                        File.class);
                                fileid.cache(file, result.getId());
                                return result;
                            default:
                                throw new StoregateExceptionMappingService(fileid).map("Upload {0} failed",
                                        new ApiException(putResponse.getStatusLine().getStatusCode(), putResponse.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                EntityUtils.toString(putResponse.getEntity())), file);
                        }
                    }
                    catch(BackgroundException e) {
                        // Cancel upload on error reply
                        cancel(file, location);
                        throw e;
                    }
                    finally {
                        EntityUtils.consume(putResponse.getEntity());
                    }
                }
                catch(IOException e) {
                    // Cancel upload on I/O failure
                    cancel(file, location);
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

    protected String start(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final StoregateApiClient client = session.getClient();
            final HttpEntityEnclosingRequestBase request = new HttpPost(String.format("%s/v4.2/upload/resumable", client.getBasePath()));
            final FileMetadata meta = new FileMetadata();
            meta.setId(StringUtils.EMPTY);
            if(status.isHidden()) {
                meta.setAttributes(2); // Hidden
            }
            else {
                meta.setAttributes(0);
            }
            meta.setFlags(0);
            if(status.getLockId() != null) {
                request.addHeader("X-Lock-Id", status.getLockId().toString());
            }
            meta.setFileName(URIEncoder.encode(file.getName()));
            meta.setParentId(fileid.getFileId(file.getParent()));
            meta.setFileSize(status.getLength() > 0 ? status.getLength() : null);
            if(null != status.getModified()) {
                meta.setModified(new DateTime(status.getModified()));
            }
            if(null != status.getCreated()) {
                meta.setCreated(new DateTime(status.getCreated()));
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
                        throw new StoregateExceptionMappingService(fileid).map("Upload {0} failed",
                                new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                        EntityUtils.toString(response.getEntity())), file);
                }
            }
            finally {
                EntityUtils.consume(response.getEntity());
            }
            if(response.containsHeader(HttpHeaders.LOCATION)) {
                return response.getFirstHeader(HttpHeaders.LOCATION).getValue();
            }
            throw new StoregateExceptionMappingService(fileid).map("Upload {0} failed",
                    new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                            EntityUtils.toString(response.getEntity())), file);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    protected void cancel(final Path file, final String location) throws BackgroundException {
        log.warn(String.format("Cancel failed upload %s for %s", location, file));
        try {
            final HttpDelete delete = new HttpDelete(location);
            session.getClient().getClient().execute(delete);
        }
        catch(IOException e) {
            throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }
}
