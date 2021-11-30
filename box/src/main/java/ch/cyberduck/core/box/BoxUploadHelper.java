package ch.cyberduck.core.box;/*
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.model.FileIdUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.FilesUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadPart;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSession;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSessionIdCommitBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadedPart;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class BoxUploadHelper {
    private static final Logger log = Logger.getLogger(BoxUploadHelper.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxUploadHelper(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    public UploadSession createUploadSession(final TransferStatus status, final Path file) throws BackgroundException {
        try {
            final HttpEntityEnclosingRequestBase request;
            if(status.isExists()) {
                request = new HttpPost(String.format("https://upload.box.com/api/2.0/files/%s/upload_sessions",
                        fileid.getFileId(file, new DisabledListProgressListener())));
                final ByteArrayOutputStream content = new ByteArrayOutputStream();
                final FileIdUploadSessionsBody idUploadSessionsBody = new FileIdUploadSessionsBody().fileName(file.getName());
                if(status.getLength() != TransferStatus.UNKNOWN_LENGTH) {
                    idUploadSessionsBody.fileSize(status.getLength());
                }
                new JSON().getContext(null).writeValue(content, idUploadSessionsBody);
                request.setEntity(new ByteArrayEntity(content.toByteArray()));
            }
            else {
                request = new HttpPost("https://upload.box.com/api/2.0/files/upload_sessions");
                final ByteArrayOutputStream content = new ByteArrayOutputStream();
                final FilesUploadSessionsBody uploadSessionsBody = new FilesUploadSessionsBody()
                        .folderId(fileid.getFileId(file.getParent(), new DisabledListProgressListener()))
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
            final HttpPost request = new HttpPost(String.format("https://upload.box.com/api/2.0/files/upload_sessions/%s/commit", uploadSessionId));
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
                public Files handleEntity(final HttpEntity entity) throws IOException {
                    return new JSON().getContext(null).readValue(entity.getContent(), Files.class);
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

    static class BoxUploadResponse {
        private final Files files;

        public BoxUploadResponse(final Files files) {
            this.files = files;
        }

        public Files getFiles() {
            return files;
        }
    }

    static class BoxPartUploadResponse extends BoxUploadResponse {
        private final UploadedPart part;

        public BoxPartUploadResponse(final UploadedPart part) {
            super(new Files());
            this.part = part;
        }

        public UploadedPart getPart() {
            return part;
        }
    }
}