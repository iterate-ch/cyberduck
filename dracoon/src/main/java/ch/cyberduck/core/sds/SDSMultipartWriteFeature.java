package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.MultipartWrite;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.MemorySegementingOutputStream;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SDSMultipartWriteFeature implements MultipartWrite<Node> {
    private static final Logger log = LogManager.getLogger(SDSMultipartWriteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final SDSUploadService upload;

    public SDSMultipartWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
        this.upload = new SDSUploadService(session, nodeid);
    }

    @Override
    public HttpResponseOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final CreateFileUploadResponse uploadResponse = upload.start(file, status);
        final String uploadUrl = uploadResponse.getUploadUrl();
        if(StringUtils.isBlank(uploadUrl)) {
            throw new InteroperabilityException("Missing upload URL in server response");
        }
        final String uploadToken = uploadResponse.getToken();
        if(StringUtils.isBlank(uploadToken)) {
            throw new InteroperabilityException("Missing upload token in server response");
        }
        final MultipartUploadTokenOutputStream proxy = new MultipartUploadTokenOutputStream(session, nodeid, file, status, uploadUrl);
        return new HttpResponseOutputStream<Node>(new MemorySegementingOutputStream(proxy, new HostPreferences(session.getHost()).getInteger("sds.upload.multipart.chunksize")),
                new SDSAttributesAdapter(session), status) {
            private final AtomicBoolean close = new AtomicBoolean();
            private final AtomicReference<Node> node = new AtomicReference<>();

            @Override
            public Node getStatus() {
                return node.get();
            }

            @Override
            public void close() throws IOException {
                try {
                    if(close.get()) {
                        log.warn("Skip double close of stream {}", this);
                        return;
                    }
                    super.close();
                    node.set(upload.complete(file, uploadToken, status));
                }
                catch(BackgroundException e) {
                    throw new IOException(e);
                }
                finally {
                    close.set(true);
                }
            }


            @Override
            protected void handleIOException(final IOException e) throws IOException {
                // Cancel upload on error reply
                try {
                    upload.cancel(file, uploadToken);
                }
                catch(BackgroundException f) {
                    log.warn("Failure {} cancelling upload for file {} with upload token {} after failure {}", f, file, uploadToken, e);
                }
                throw e;
            }
        };
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }
}
