package ch.cyberduck.core.deepbox;/*
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
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

public class DeepboxWriteFeature extends AbstractHttpWriteFeature<File> {
    private static final Logger log = LogManager.getLogger(DeepboxWriteFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxWriteFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        super(new DeepboxAttributesFinderFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
    }

    //    @Override
//    public HttpResponseOutputStream<Node> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
//        return null;
//    }
    @Override
    public HttpResponseOutputStream<File> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {

        final DelayedHttpEntityCallable<File> command = new DelayedHttpEntityCallable<File>(file) {
            @Override
            public File call(final HttpEntity entity) throws BackgroundException {
                try {
                    final HttpEntityEnclosingRequestBase request;
                    final String nodeId = fileid.getFileId(file);
                    if(nodeId != null) {
                        request = new HttpPut(String.format("%s/api/v1/nodes/%s/revisions", session.getClient().getBasePath(), nodeId));
                    }
                    else {
                        // TODO is this safe not to overwrite files?
                        request = new HttpPost(String.format("%s/api/v1/deepBoxes/%s/boxes/%s/files/%s",
                                session.getClient().getBasePath(),
                                fileid.getDeepBoxNodeId(file),
                                fileid.getBoxNodeId(file),
                                fileid.getFileId(file.getParent()),
                                nodeId));
                    }
                    final Checksum checksum = status.getChecksum();
                    if(Checksum.NONE != checksum) {
                        switch(checksum.algorithm) {
                            case sha1:
                                request.addHeader(HttpHeaders.CONTENT_MD5, checksum.hash);
                        }
                    }
                    //final ByteArrayOutputStream content = new ByteArrayOutputStream();
//                    new JSON().getContext(null).writeValue(content, new FilescontentAttributes()
//                            .name(file.getName())
//                            .parent(new FilescontentAttributesParent().id(fileid.getFileId(file.getParent())))
//                            .contentCreatedAt(status.getCreated() != null ? new DateTime(status.getCreated()) : null)
//                            .contentModifiedAt(status.getModified() != null ? new DateTime(status.getModified()) : null)
//                    );
                    final MultipartEntityBuilder multipart = MultipartEntityBuilder.create();
                    //multipart.addBinaryBody("attributes", content.toByteArray());
                    final ByteArrayOutputStream out = new ByteArrayOutputStream();
                    entity.writeTo(out);
                    multipart.addBinaryBody(nodeId != null ? "file" : "files", out.toByteArray(),
                            null == status.getMime() ? ContentType.APPLICATION_OCTET_STREAM : ContentType.create(status.getMime()), file.getName());
                    request.setEntity(multipart.build());
//                    if(status.isExists()) {
//                        if(StringUtils.isNotBlank(status.getRemote().getETag())) {
//                            request.addHeader(new BasicHeader(HttpHeaders.IF_MATCH, status.getRemote().getETag()));
//                        }
//                        else {
//                            log.warn(String.format("Missing remote attributes in transfer status to read current ETag for %s", file));
//                        }
//                    }
                    session.getClient().getClient().execute(request);
//                    if(log.isDebugEnabled()) {
//                        log.debug(String.format("Received response %s for upload of %s", files, file));
//                    }
//                    if(files.getEntries().stream().findFirst().isPresent()) {
//                        return files.getEntries().stream().findFirst().get();
//                    }
//                    throw new NotfoundException(file.getAbsolute());
                    return null;
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

    /*protected String start(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final DeepboxApiClient client = session.getClient();
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
                        throw new DeepboxExceptionMappingService(fileid).map("Upload {0} failed",
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
            throw new DeepboxExceptionMappingService(fileid).map("Upload {0} failed",
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
    }*/
}
