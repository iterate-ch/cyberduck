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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpExceptionMappingService;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.api.UploadsApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CompleteUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.CreateFileUploadResponse;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptExceptionMappingService;
import ch.cyberduck.core.shared.DefaultAttributesFinderFeature;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.CryptoSystemException;
import com.dracoon.sdk.crypto.InvalidFileKeyException;
import com.dracoon.sdk.crypto.InvalidKeyPairException;
import com.dracoon.sdk.crypto.model.EncryptedFileKey;
import com.fasterxml.jackson.databind.ObjectReader;

public class SDSWriteFeature extends AbstractHttpWriteFeature<VersionId> {
    private static final Logger log = Logger.getLogger(SDSWriteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final Find finder;
    private final AttributesFinder attributes;

    public static final CreateFileUploadRequest.ClassificationEnum DEFAULT_CLASSIFICATION
        = CreateFileUploadRequest.ClassificationEnum.NUMBER_1; // public

    public SDSWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this(session, nodeid, new DefaultFindFeature(session), new DefaultAttributesFinderFeature(session));
    }

    public SDSWriteFeature(final SDSSession session, final SDSNodeIdProvider nodeid, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.nodeid = nodeid;
        this.finder = finder;
        this.attributes = attributes;
    }

    @Override
    public HttpResponseOutputStream<VersionId> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final CreateFileUploadRequest body = new CreateFileUploadRequest()
            .size(status.getLength())
            .parentId(Long.parseLong(nodeid.getFileid(file.getParent(), new DisabledListProgressListener())))
            .name(file.getName());
        try {
            final CreateFileUploadResponse response = new NodesApi(session.getClient()).createFileUpload(body, StringUtils.EMPTY);
            final String uploadToken = response.getToken();
            final DelayedHttpEntityCallable<VersionId> command = new DelayedHttpEntityCallable<VersionId>() {
                @Override
                public VersionId call(final AbstractHttpEntity entity) throws BackgroundException {
                    try {
                        final SDSApiClient client = session.getClient();
                        final HttpPost request = new HttpPost(String.format("%s/v4/uploads/%s", client.getBasePath(), uploadToken));
                        request.setEntity(entity);
                        request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                        request.setHeader(SDSSession.SDS_AUTH_TOKEN_HEADER, StringUtils.EMPTY);
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
                        if(status.isComplete()) {
                            VersionId version;
                            try {
                                version = complete(file, uploadToken, status);
                            }
                            catch(ConflictException e) {
                                version = complete(file, uploadToken, new TransferStatus(status).exists(true));
                            }
                            status.setVersion(version);
                            return version;
                        }
                        return new VersionId(null);
                    }
                    catch(IOException e) {
                        try {
                            if(log.isInfoEnabled()) {
                                log.info(String.format("Cancel failed upload %s for %s", uploadToken, file));
                            }
                            new UploadsApi(session.getClient()).cancelFileUploadByToken(uploadToken);
                        }
                        catch(ApiException f) {
                            throw new SDSExceptionMappingService().map(f);
                        }
                        throw new HttpExceptionMappingService().map("Upload {0} failed", e, file);
                    }
                }

                @Override
                public long getContentLength() {
                    return status.getLength();
                }
            };
            return this.write(file, status, command);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
        }
    }

    protected VersionId complete(final Path file, final String uploadToken, final TransferStatus status) throws BackgroundException {
        final SDSApiClient client = session.getClient();
        try {
            final CompleteUploadRequest body = new CompleteUploadRequest()
                .keepShareLinks(status.isExists() ? PreferencesFactory.get().getBoolean("sds.upload.sharelinks.keep") : false)
                .resolutionStrategy(status.isExists() ? CompleteUploadRequest.ResolutionStrategyEnum.OVERWRITE : CompleteUploadRequest.ResolutionStrategyEnum.FAIL);
            if(status.getFilekey() != null) {
                final ObjectReader reader = session.getClient().getJSON().getContext(null).readerFor(FileKey.class);
                final FileKey fileKey = reader.readValue(status.getFilekey().array());
                final EncryptedFileKey encryptFileKey = Crypto.encryptFileKey(
                    TripleCryptConverter.toCryptoPlainFileKey(fileKey),
                    TripleCryptConverter.toCryptoUserPublicKey(session.keyPair().getPublicKeyContainer())
                );
                body.setFileKey(TripleCryptConverter.toSwaggerFileKey(encryptFileKey));
            }
            final Node upload = new UploadsApi(client).completeFileUploadByToken(uploadToken, null, body);
            return new VersionId(String.valueOf(upload.getId()));
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(CryptoSystemException | InvalidFileKeyException | InvalidKeyPairException e) {
            throw new TripleCryptExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
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
    public Append append(final Path file, final Long length, final Cache<Path> cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            final PathAttributes attr = attributes.withCache(cache).find(file);
            return new Append(false, true).withSize(attr.getSize()).withChecksum(attr.getChecksum());
        }
        return Write.notfound;
    }
}
