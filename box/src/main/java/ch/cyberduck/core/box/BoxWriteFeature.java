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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.JSON;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.Files;
import ch.cyberduck.core.box.io.swagger.client.model.FilescontentAttributes;
import ch.cyberduck.core.box.io.swagger.client.model.FilescontentAttributesParent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BoxWriteFeature extends AbstractHttpWriteFeature<File> {
    private static final Logger log = LogManager.getLogger(BoxWriteFeature.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;
    private final BoxApiClient client;

    public BoxWriteFeature(final BoxSession session, final BoxFileidProvider fileid) {
        super(new BoxAttributesFinderFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
        this.client = new BoxApiClient(session.getClient());
        this.client.setBasePath("https://upload.box.com/api/2.0");
    }

    @Override
    public HttpResponseOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>(file) {
            @Override
            public File call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final HttpPost request;
                    if(status.isExists()) {
                        request = new HttpPost(String.format("%s/files/%s/content?fields=%s", client.getBasePath(),
                                fileid.getFileId(file),
                                String.join(",", BoxAttributesFinderFeature.DEFAULT_FIELDS)));
                    }
                    else {
                        request = new HttpPost(String.format("%s/files/content?fields=%s", client.getBasePath(),
                                String.join(",", BoxAttributesFinderFeature.DEFAULT_FIELDS)));
                    }
                    final Checksum checksum = status.getChecksum();
                    if(Checksum.NONE != checksum) {
                        switch(checksum.algorithm) {
                            case sha1:
                                request.addHeader(HttpHeaders.CONTENT_MD5, checksum.hash);
                        }
                    }
                    final ByteArrayOutputStream content = new ByteArrayOutputStream();
                    new JSON().getContext(null).writeValue(content, new FilescontentAttributes()
                            .name(file.getName())
                            .parent(new FilescontentAttributesParent().id(fileid.getFileId(file.getParent())))
                            .contentCreatedAt(status.getCreated() != null ? new DateTime(status.getModified()) : null)
                            .contentModifiedAt(status.getModified() != null ? new DateTime(status.getModified()) : null)
                    );
                    final MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
                    multipart.addBinaryBody("attributes", content.toByteArray());
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    entity.writeTo(out);
                    multipart.addBinaryBody("file", out.toByteArray(),
                            null == status.getMime() ? ContentType.APPLICATION_OCTET_STREAM : ContentType.create(status.getMime()), file.getName());
                    request.setEntity(multipart.build());
                    if(status.isExists()) {
                        if(StringUtils.isNotBlank(status.getRemote().getETag())) {
                            request.addHeader(new BasicHeader(HttpHeaders.IF_MATCH, status.getRemote().getETag()));
                        }
                        else {
                            log.warn(String.format("Missing remote attributes in transfer status to read current ETag for %s", file));
                        }
                    }
                    final Files files = session.getClient().execute(request, new BoxClientErrorResponseHandler<Files>() {
                        @Override
                        public Files handleEntity(final HttpEntity entity) throws IOException {
                            return new JSON().getContext(null).readValue(entity.getContent(), Files.class);
                        }
                    });
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Received response %s for upload of %s", files, file));
                    }
                    if(files.getEntries().stream().findFirst().isPresent()) {
                        return files.getEntries().stream().findFirst().get();
                    }
                    throw new NotfoundException(file.getAbsolute());
                }
                catch(HttpResponseException e) {
                    throw new DefaultHttpResponseExceptionMappingService().map(e);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            @Override
            public long getContentLength() {
                return -1L;
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new SHA1ChecksumCompute();
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public boolean timestamp() {
        return true;
    }
}
