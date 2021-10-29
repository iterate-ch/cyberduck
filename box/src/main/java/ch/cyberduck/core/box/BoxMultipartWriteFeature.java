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

import ch.cyberduck.core.BytecountStreamListener;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.api.UploadsChunkedApi;
import ch.cyberduck.core.box.io.swagger.client.model.FileIdUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.FilesUploadSessionsBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSession;
import ch.cyberduck.core.box.io.swagger.client.model.UploadSessionIdCommitBody;
import ch.cyberduck.core.box.io.swagger.client.model.UploadedPart;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class BoxMultipartWriteFeature implements Write<Files> {
    private static final Logger log = Logger.getLogger(BoxMultipartWriteFeature.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;
    private final BoxApiClient client;

    public BoxMultipartWriteFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.client = new BoxApiClient(this.session.getClient());
        this.client.setBasePath("https://upload.box.com/api/2.0");
    }

    @Override
    public HttpResponseOutputStream<Files> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final UploadSession uploadSession;
            if(status.isExists()) {
                uploadSession = new UploadsChunkedApi(client).postFilesIdUploadSessions(
                        fileid.getFileId(file, new DisabledListProgressListener()), new FileIdUploadSessionsBody()
                                .fileName(file.getName())
                                .fileSize(status.getLength()), null);
            }
            else {
                // Creates an upload session for a new file
                uploadSession = new UploadsChunkedApi(client).postFilesUploadSessions(new FilesUploadSessionsBody()
                        .folderId(fileid.getFileId(file.getParent(), new DisabledListProgressListener()))
                        .fileName(file.getName())
                        .fileSize(status.getLength()));
            }
            if(log.isDebugEnabled()) {
                log.debug(String.format("Obtained session %s for file %s", uploadSession, file));
            }
            final BoxOutputStream proxy = new BoxOutputStream(file, uploadSession, status);
            return new HttpResponseOutputStream<Files>(new MemorySegementingOutputStream(proxy,
                    uploadSession.getPartSize().intValue())) {
                @Override
                public Files getStatus() {
                    return proxy.getResult();
                }
            };
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map("Upload {0} failed", e, file);
        }
    }

    private final class BoxOutputStream extends OutputStream {
        private final Path file;
        private final UploadSession uploadSession;
        private final TransferStatus overall;
        private final List<UploadedPart> checksums = new ArrayList<>();
        private final AtomicBoolean close = new AtomicBoolean();
        private final BytecountStreamListener byteCounter = new BytecountStreamListener();
        private final AtomicReference<Files> result = new AtomicReference<>();

        public BoxOutputStream(final Path file, final UploadSession uploadSession, final TransferStatus status) {
            this.file = file;
            this.uploadSession = uploadSession;
            this.overall = status;
        }

        @Override
        public void write(final int value) throws IOException {
            throw new IOException(new UnsupportedOperationException());
        }

        @Override
        public void write(byte[] buffer) throws IOException {
            this.write(buffer, 0, buffer.length);
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            final byte[] content = Arrays.copyOfRange(b, off, len);
            try {
                final HttpRange range = HttpRange.withStatus(new TransferStatus()
                        .withLength(content.length)
                        .withOffset(byteCounter.getSent()));
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Send range %s for file %s", range, file));
                }
                final HttpPut request = new HttpPut(String.format("%s/files/upload_sessions/%s", client.getBasePath(), uploadSession.getId()));
                // Must not overlap with the range of a part already uploaded this session.
                request.addHeader(new BasicHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", range.getStart(), range.getEnd(),
                        overall.getOffset() + overall.getLength())));
                request.addHeader(new BasicHeader("Digest", String.format("sha=%s",
                        new BoxBase64SHA1ChecksumCompute().compute(new ByteArrayInputStream(content), overall).hash)));
                request.setEntity(new ByteArrayEntity(content));
                checksums.add(session.getClient().execute(request, new BrickClientErrorResponseHandler<UploadedPart>() {
                    @Override
                    public UploadedPart handleEntity(final HttpEntity entity) throws IOException {
                        return new JSON().getContext(null).readValue(entity.getContent(), UploadedPart.class);
                    }
                }));
                byteCounter.sent(len);
            }
            catch(ChecksumException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void close() throws IOException {
            if(close.get()) {
                log.warn(String.format("Skip double close of stream %s", this));
                return;
            }
            try {
                super.close();
                final HttpPost request = new HttpPost(String.format("%s/files/upload_sessions/%s/commit", client.getBasePath(), uploadSession.getId()));
                if(!Checksum.NONE.equals(overall.getChecksum())) {
                    request.addHeader(new BasicHeader("Digest", String.format("sha=%s", overall.getChecksum().hash)));
                }
                final ByteArrayOutputStream content = new ByteArrayOutputStream();
                new JSON().getContext(null).writeValue(content, new UploadSessionIdCommitBody().parts(
                        checksums.stream().map(UploadedPart::getPart).collect(Collectors.toCollection(ArrayList::new))
                ));
                request.setEntity(new ByteArrayEntity(content.toByteArray()));
                if(overall.isExists()) {
                    if(StringUtils.isNotBlank(overall.getRemote().getETag())) {
                        request.addHeader(new BasicHeader(HttpHeaders.IF_MATCH, overall.getRemote().getETag()));
                    }
                    else {
                        log.warn(String.format("Missing remote attributes in transfer status to read current ETag for %s", file));
                    }
                }
                final Files files = session.getClient().execute(request, new BrickClientErrorResponseHandler<Files>() {
                    @Override
                    public Files handleEntity(final HttpEntity entity) throws IOException {
                        return new JSON().getContext(null).readValue(entity.getContent(), Files.class);
                    }
                });
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Received response %s for upload of %s", files, file));
                }
                result.set(files);
            }
            finally {
                close.set(true);
            }
        }

        public Files getResult() {
            return result.get();
        }
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new BoxBase64SHA1ChecksumCompute();
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }
}
