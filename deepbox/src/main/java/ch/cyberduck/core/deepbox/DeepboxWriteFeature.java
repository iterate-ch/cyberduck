package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.deepbox.io.swagger.client.JSON;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.AbstractResponseHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;

import com.fasterxml.jackson.databind.ObjectReader;

public class DeepboxWriteFeature extends AbstractHttpWriteFeature<Node> {
    private static final Logger log = LogManager.getLogger(DeepboxWriteFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxWriteFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        super(new DeepboxAttributesFinderFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<Node> command = new DelayedHttpEntityCallable<Node>(file) {
            @Override
            public Node call(final HttpEntity entity) throws BackgroundException {
                try {
                    final HttpEntityEnclosingRequestBase request;
                    if(status.isExists()) {
                        request = new HttpPut(String.format("%s/api/v1/nodes/%s/revisions", session.getClient().getBasePath(), fileid.getFileId(file)));
                    }
                    else {
                        if(new DeepboxPathContainerService(session, fileid).isInbox(file.getParent())) {
                            request = new HttpPost(String.format("%s/api/v1/deepBoxes/%s/boxes/%s/queue",
                                    session.getClient().getBasePath(),
                                    fileid.getDeepBoxNodeId(file),
                                    fileid.getBoxNodeId(file)));
                        }
                        else {
                            request = new HttpPost(String.format("%s/api/v1/deepBoxes/%s/boxes/%s/files/%s",
                                    session.getClient().getBasePath(),
                                    fileid.getDeepBoxNodeId(file),
                                    fileid.getBoxNodeId(file),
                                    fileid.getFileId(file.getParent())));
                        }
                    }
                    final Checksum checksum = status.getChecksum();
                    if(Checksum.NONE != checksum) {
                        switch(checksum.algorithm) {
                            case sha1:
                                request.addHeader(HttpHeaders.CONTENT_MD5, checksum.hash);
                        }
                    }
                    final MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
                    multipart.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    multipart.setCharset(StandardCharsets.UTF_8);
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    entity.writeTo(out);
                    if(status.isExists()) {
                        multipart.addBinaryBody("file", out.toByteArray(),
                                null == status.getMime() ? ContentType.APPLICATION_OCTET_STREAM : ContentType.create(status.getMime()), file.getName());
                        request.setEntity(multipart.build());
                        return session.getClient().getClient().execute(request, new AbstractResponseHandler<Node>() {
                            @Override
                            public Node handleEntity(final HttpEntity entity) throws IOException {
                                final ObjectReader reader = new JSON().getContext(null).reader(Node.class);
                                return reader.readValue(entity.getContent());
                            }
                        });
                    }
                    else {
                        multipart.addBinaryBody("files", out.toByteArray(),
                                null == status.getMime() ? ContentType.APPLICATION_OCTET_STREAM : ContentType.create(status.getMime()), file.getName());
                        request.setEntity(multipart.build());
                        return session.getClient().getClient().execute(request, new AbstractResponseHandler<Node>() {
                            @Override
                            public Node handleEntity(final HttpEntity entity) throws IOException {
                                final ObjectReader reader = new JSON().getContext(null).readerForArrayOf(Node.class);
                                final Node[] node = reader.readValue(entity.getContent());
                                return node[0];
                            }
                        });
                    }
                }
                catch(HttpResponseException e) {
                    throw new DefaultHttpResponseExceptionMappingService().map("Upload {0} failed", e, file);
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
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.checksum, Flags.mime);
    }
}
