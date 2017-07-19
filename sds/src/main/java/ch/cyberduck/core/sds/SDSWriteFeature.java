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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.DelayedHttpMultipartEntity;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.DisabledChecksumCompute;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UserApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.UserKeyPairContainer;
import ch.cyberduck.core.sds.swagger.CompleteUploadRequest;
import ch.cyberduck.core.sds.triplecrypt.CryptoExceptionMappingService;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectReader;
import eu.ssp_europe.sds.crypto.Crypto;
import eu.ssp_europe.sds.crypto.CryptoSystemException;
import eu.ssp_europe.sds.crypto.InvalidFileKeyException;
import eu.ssp_europe.sds.crypto.InvalidKeyPairException;
import eu.ssp_europe.sds.crypto.model.EncryptedFileKey;

public class SDSWriteFeature extends AbstractHttpWriteFeature<VersionId> {

    private final SDSSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public static final int DEFAULT_CLASSIFICATION = 1; // public

    private final PathContainerService containerService
            = new PathContainerService();

    public SDSWriteFeature(final SDSSession session) {
        this(session, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public SDSWriteFeature(final SDSSession session, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final CreateFileUploadRequest body = new CreateFileUploadRequest();
        body.setParentId(Long.parseLong(new SDSNodeIdProvider(session).getFileid(file.getParent(), new DisabledListProgressListener())));
        body.setName(file.getName());
        body.classification(DEFAULT_CLASSIFICATION);
        try {
            final CreateFileUploadResponse response = new NodesApi(session.getClient()).createFileUpload(session.getToken(), body);
            final String uploadId = response.getUploadId();
            final DelayedHttpMultipartEntity entity = new DelayedHttpMultipartEntity(file.getName(), status);
            final DelayedHttpEntityCallable<VersionId> command = new DelayedHttpEntityCallable<VersionId>() {
                @Override
                public VersionId call(final AbstractHttpEntity entity) throws BackgroundException {
                    try {
                        final SDSApiClient client = session.getClient();
                        final HttpPost request = new HttpPost(String.format("%s/nodes/files/uploads/%s", client.getBasePath(), uploadId));
                        request.setEntity(entity);
                        request.setHeader(SDSSession.SDS_AUTH_TOKEN_HEADER, session.getToken());
                        request.setHeader(HTTP.CONTENT_TYPE, String.format("multipart/form-data; boundary=%s", DelayedHttpMultipartEntity.DEFAULT_BOUNDARY));
                        final HttpResponse response = client.getClient().execute(request);
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
                        if(status.getHeader() != null) {
                            final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                            final FileKey fileKey = reader.readValue(status.getHeader().array());
                            final UserKeyPairContainer keyPairContainer = new UserApi(session.getClient()).getUserKeyPair(session.getToken());
                            final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                                    TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                                    TripleCryptConverter.toCryptoUserPublicKey(keyPairContainer.getPublicKeyContainer())
                            );
                            body.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
                        }
                        final Node upload = new NodesApi(client).completeFileUpload(session.getToken(), uploadId, null, body);
                        return new VersionId(String.valueOf(upload.getId()));
                    }
                    catch(IOException e) {
                        throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                    catch(ApiException e) {
                        throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                    catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException e) {
                        throw new CryptoExceptionMappingService().map("Upload {0} failed", e, file);
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
    public ChecksumCompute checksum(final Path file) {
        return new DisabledChecksumCompute();
    }

    @Override
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attributes = this.attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attributes.getSize()).withChecksum(attributes.getChecksum());
        }
        return Write.notfound;
    }
}
