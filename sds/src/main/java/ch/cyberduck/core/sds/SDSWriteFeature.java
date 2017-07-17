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
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.PublicKeyContainer;
import ch.cyberduck.core.sds.swagger.CompleteUploadRequest;
import ch.cyberduck.core.sds.triplecrypt.CryptoOutputStream;
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

import eu.ssp_europe.sds.crypto.Crypto;
import eu.ssp_europe.sds.crypto.CryptoSystemException;
import eu.ssp_europe.sds.crypto.InvalidFileKeyException;
import eu.ssp_europe.sds.crypto.InvalidKeyPairException;
import eu.ssp_europe.sds.crypto.model.EncryptedFileKey;
import eu.ssp_europe.sds.crypto.model.PlainFileKey;
import eu.ssp_europe.sds.crypto.model.UserPublicKey;

public class SDSWriteFeature extends AbstractHttpWriteFeature<VersionId> {

    private final SDSSession session;
    private final Find finder;
    private final AttributesFinder attributes;

    public static final int DEFAULT_CLASSIFICATION = 1; // public

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
            final String id = response.getUploadId();
            final DelayedHttpMultipartEntity entity = new DelayedHttpMultipartEntity(file.getName(), status);
            final PlainFileKey fileKey = Crypto.generateFileKey();
            final DelayedHttpEntityCallable<VersionId> command = new DelayedHttpEntityCallable<VersionId>() {
                @Override
                public VersionId call(final AbstractHttpEntity entity) throws BackgroundException {
                    try {
                        final SDSApiClient client = session.getClient();
                        final HttpPost request = new HttpPost(String.format("%s/nodes/files/uploads/%s", client.getBasePath(), id));
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
                        if(file.getParent().getType().contains(Path.Type.encrypted)) {
                            final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(fileKey, this.convert(session.getKeys().getPublicKeyContainer()));
                            body.setFileKey(this.convert(encryptFileKey));
                        }
                        final Node upload = new NodesApi(client).completeFileUpload(session.getToken(), id, null, body);
                        return new VersionId(String.valueOf(upload.getId()));
                    }
                    catch(IOException e) {
                        throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                    catch(ApiException e) {
                        throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                    catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException e) {
                        throw new BackgroundException(String.format("Upload %s failed", file), e);
                    }
                }

                @Override
                public long getContentLength() {
                    return entity.getContentLength();
                }

                private FileKey convert(final EncryptedFileKey k) {
                    final FileKey key = new FileKey();
                    key.setIv(k.getIv());
                    key.setKey(k.getKey());
                    key.setTag(k.getTag());
                    key.setVersion(k.getVersion());
                    return key;
                }

                private UserPublicKey convert(final PublicKeyContainer c) {
                    final UserPublicKey key = new UserPublicKey();
                    key.setPublicKey(c.getPublicKey());
                    key.setVersion(c.getVersion());
                    return key;
                }
            };
            if(file.getParent().getType().contains(Path.Type.encrypted)) {
                return new CryptoOutputStream<>(this.write(file, status, command, entity), Crypto.createFileEncryptionCipher(fileKey), fileKey);
            }
            return this.write(file, status, command, entity);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(CryptoSystemException | InvalidFileKeyException e) {
            throw new BackgroundException(String.format("Upload %s failed", file), e);
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
