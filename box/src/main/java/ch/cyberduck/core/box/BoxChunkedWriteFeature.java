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
import ch.cyberduck.core.box.io.swagger.client.model.UploadPart;
import ch.cyberduck.core.box.io.swagger.client.model.UploadedPart;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpRange;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.SHA1ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicHeader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class BoxChunkedWriteFeature extends AbstractHttpWriteFeature<File> {
    private static final Logger log = LogManager.getLogger(BoxChunkedWriteFeature.class);

    private final BoxSession session;
    private final BoxApiClient client;

    public BoxChunkedWriteFeature(final BoxSession session, final BoxFileidProvider fileid) {
        super(new BoxAttributesFinderFeature(session, fileid));
        this.session = session;
        this.client = new BoxApiClient(session.getClient());
        this.client.setBasePath("https://upload.box.com/api/2.0");
    }

    @Override
    public HttpResponseOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>(file) {
            @Override
            public File call(final HttpEntity entity) throws BackgroundException {
                try {
                    final HttpRange range = HttpRange.withStatus(new TransferStatus()
                            .withLength(status.getLength())
                            .withOffset(status.getOffset()));
                    final String uploadSessionId = status.getParameters().get(BoxLargeUploadService.UPLOAD_SESSION_ID);
                    final String overall_length = status.getParameters().get(BoxLargeUploadService.OVERALL_LENGTH);
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Send range %s for file %s", range, file));
                    }
                    final HttpPut request = new HttpPut(String.format("%s/files/upload_sessions/%s", client.getBasePath(), uploadSessionId));
                    // Must not overlap with the range of a part already uploaded this session.
                    request.addHeader(new BasicHeader(HttpHeaders.CONTENT_RANGE, String.format("bytes %d-%d/%d", range.getStart(), range.getEnd(),
                            Long.valueOf(overall_length))));
                    request.addHeader(new BasicHeader("Digest", String.format("sha=%s", status.getChecksum().base64)));
                    request.setEntity(entity);
                    final UploadPart response = session.getClient().execute(request, new BoxClientErrorResponseHandler<UploadedPart>() {
                        @Override
                        public UploadedPart handleEntity(final HttpEntity entity1) throws IOException {
                            return new JSON().getContext(null).readValue(entity1.getContent(), UploadedPart.class);
                        }
                    }).getPart();
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Received response %s for upload of %s", response, file));
                    }
                    return new File().size(response.getSize()).sha1(response.getSha1()).id(response.getPartId());
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
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new SHA1ChecksumCompute();
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }
}
