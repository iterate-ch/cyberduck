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


import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.model.FileIdUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.FilesUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadPart;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSession;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSessionIdCommitBody;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class BoxUploadHelper {
    private static final Logger log = LogManager.getLogger(BoxUploadHelper.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;
    private final BoxApiClient client;

    public BoxUploadHelper(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.client = new BoxApiClient(session.getClient());
        this.client.setBasePath("https://upload.box.com/api/2.0");
    }

    public UploadSession createUploadSession(final TransferStatus status, final Path file) throws BackgroundException {
        try {
            final HttpEntityEnclosingRequestBase request;
            if(status.isExists()) {
                request = new HttpPost(String.format("%s/files/%s/upload_sessions",
                        client.getBasePath(), fileid.getFileId(file)));
                final ByteArrayOutputStream content = new ByteArrayOutputStream();
                final FileIdUploadSessionsBody idUploadSessionsBody = new FileIdUploadSessionsBody().fileName(file.getName());
                if(status.getLength() != TransferStatus.UNKNOWN_LENGTH) {
                    idUploadSessionsBody.fileSize(status.getLength());
                }
                new JSON().getContext(null).writeValue(content, idUploadSessionsBody);
                request.setEntity(new ByteArrayEntity(content.toByteArray()));
            }
            else {
                request = new HttpPost(String.format("%s/files/upload_sessions", client.getBasePath()));
                final ByteArrayOutputStream content = new ByteArrayOutputStream();
                final FilesUploadSessionsBody uploadSessionsBody = new FilesUploadSessionsBody()
                        .folderId(fileid.getFileId(file.getParent()))
                        .fileName(file.getName());
                if(status.getLength() != TransferStatus.UNKNOWN_LENGTH) {
                    uploadSessionsBody.fileSize(status.getLength());
                }
                new JSON().getContext(null).writeValue(content, uploadSessionsBody);
                request.setEntity(new ByteArrayEntity(content.toByteArray()));
            }
            final BoxClientErrorResponseHandler<UploadSession> responseHandler = new BoxClientErrorResponseHandler<UploadSession>() {
                @Override
                public UploadSession handleEntity(final HttpEntity entity) throws IOException {
                    return new JSON().getContext(null).readValue(entity.getContent(), UploadSession.class);
                }
            };
            return session.getClient().execute(request, responseHandler);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    public Files commitUploadSession(final Path file, final String uploadSessionId,
                                     final TransferStatus overall, final List<UploadPart> uploadParts) throws BackgroundException {
        try {
            final HttpPost request = new HttpPost(String.format("%s/files/upload_sessions/%s/commit", client.getBasePath(), uploadSessionId));
            if(!Checksum.NONE.equals(overall.getChecksum())) {
                request.addHeader(new BasicHeader("Digest", String.format("sha=%s", overall.getChecksum().hash)));
            }
            final ByteArrayOutputStream content = new ByteArrayOutputStream();
            final UploadSessionIdCommitBody body = new UploadSessionIdCommitBody().parts(uploadParts);
            new JSON().getContext(null).writeValue(content, body);
            request.setEntity(new ByteArrayEntity(content.toByteArray()));
            if(overall.isExists()) {
                if(StringUtils.isNotBlank(overall.getRemote().getETag())) {
                    request.addHeader(new BasicHeader(HttpHeaders.IF_MATCH, overall.getRemote().getETag()));
                }
                else {
                    log.warn(String.format("Missing remote attributes in transfer status to read current ETag for %s", file));
                }
            }
            return session.getClient().execute(request, new BoxClientErrorResponseHandler<Files>() {
                @Override
                public Files handleResponse(final HttpResponse response) throws IOException {
                    if(response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Wait for server to process chunks with response %s", response));
                        }
                        this.flush(file, response, uploadSessionId);
                        return session.getClient().execute(request, this);
                    }
                    return super.handleResponse(response);
                }

                @Override
                public Files handleEntity(final HttpEntity entity) throws IOException {
                    return new JSON().getContext(null).readValue(entity.getContent(), Files.class);
                }

                /**
                 * Wait for server processing all pending chunks
                 */
                private void flush(final Path file, final HttpResponse response, final String uploadSessionId) throws IOException {
                    UploadSession uploadSession;
                    do {
                        final HttpGet request = new HttpGet(String.format("%s/files/upload_sessions/%s", client.getBasePath(), uploadSessionId));
                        uploadSession = new JSON().getContext(null).readValue(session.getClient().execute(request).getEntity().getContent(), UploadSession.class);
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Server processed %d of %d parts",
                                    uploadSession.getNumPartsProcessed(), uploadSession.getTotalParts()));
                        }
                    }
                    while(!Objects.equals(uploadSession.getNumPartsProcessed(), uploadSession.getTotalParts()));
                }
            });
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }
}
