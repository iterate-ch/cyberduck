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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.DelayedHttpMultipartEntity;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.swagger.CompleteUploadRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;

import java.io.IOException;
import java.util.Collections;

public class SDSWriteFeature extends AbstractHttpWriteFeature<VersionId> {

    private final SDSSession session;

    public SDSWriteFeature(final SDSSession session) {
        super(session);
        this.session = session;
    }

    public SDSWriteFeature(final SDSSession session, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
    }

    @Override
    public HttpResponseOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final CreateFileUploadRequest body = new CreateFileUploadRequest();
        body.setParentId(Long.parseLong(new SDSNodeIdProvider(session).getFileid(file.getParent(), new DisabledListProgressListener())));
        body.setName(file.getName());
        body.classification(2); // internal
        try {
            final CreateFileUploadResponse response = new NodesApi(session.getClient()).createFileUpload(session.getToken(), body);
            final String id = response.getUploadId();
            final DelayedHttpMultipartEntity entity = new DelayedHttpMultipartEntity(file.getName(), status);
            final DelayedHttpEntityCallable<VersionId> command = new DelayedHttpEntityCallable<VersionId>() {
                @Override
                public VersionId call(final AbstractHttpEntity entity) throws BackgroundException {
                    try {
                        final HttpPost post = new HttpPost(String.format("%s/nodes/files/uploads/%s", session.getClient().getBasePath(), id));
                        post.setEntity(entity);
                        post.setHeader(SDSSession.SDS_AUTH_TOKEN_HEADER, session.getToken());
                        post.setHeader(HTTP.CONTENT_TYPE, String.format("multipart/form-data; boundary=%s", DelayedHttpMultipartEntity.DEFAULT_BOUNDARY));
                        final HttpResponse response = ApacheConnectorProvider.getHttpClient(session.getClient().getHttpClient()).execute(post);
                        try {
                            // Validate response
                            switch(response.getStatusLine().getStatusCode()) {
                                case HttpStatus.SC_CREATED:
                                    // Upload complete
                                    break;
                                default:
                                    EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                                    throw new SDSExceptionMappingService().map(
                                            new ApiException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase(), Collections.emptyMap(),
                                                    EntityUtils.toString(response.getEntity())));
                            }
                        }
                        finally {
                            EntityUtils.consume(response.getEntity());
                        }
                        final CompleteUploadRequest body = new CompleteUploadRequest();
                        body.setResolutionStrategy(CompleteUploadRequest.ResolutionStrategyEnum.OVERWRITE);
                        final Node upload = new NodesApi(session.getClient()).completeFileUpload(session.getToken(), id, null, body);
                        return new VersionId(String.valueOf(upload.getId()));
                    }
                    catch(IOException e) {
                        throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                    catch(ApiException e) {
                        throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                }

                @Override
                public long getContentLength() {
                    return entity.getContentLength();
                }
            };
            return this.write(file, status, command, entity);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
        }
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
    public ChecksumCompute checksum() {
        return new DisabledChecksumCompute();
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        return override;
    }
}
