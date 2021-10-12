package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;

public class GmxcloudWriteFeature extends AbstractHttpWriteFeature<GmxcloudUploadResponse> {
    private static final Logger log = Logger.getLogger(GmxcloudWriteFeature.class);

    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;


    public GmxcloudWriteFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<GmxcloudUploadResponse> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String resourceId = fileid.getFileId(file.getParent(), new DisabledListProgressListener());
        String uploadURI = status.getUrl();
        String uploadedFileId = status.getParameters().get(Constant.RESOURCE_ID);
        if(uploadURI == null) {
            final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = GmxcloudUploadHelper.getUploadResourceCreationResponseEntry(session, file, ResourceCreationRepresentationArrayInner.UploadTypeEnum.SIMPLE, resourceId);
            uploadURI = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
            uploadedFileId = Util.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation());
        }
        try {
            final HttpResponseOutputStream<GmxcloudUploadResponse> httpResponseOutputStream = this.write(file, status, getDelayedHttpEntityCallable(status, uploadURI));
            fileid.cache(file, uploadedFileId);
            return httpResponseOutputStream;
        }
        catch(BackgroundException exception) {
            cancel(file, uploadedFileId);
            throw exception;
        }
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new GmxcloudCdash64Compute();
    }

    private DelayedHttpEntityCallable<GmxcloudUploadResponse> getDelayedHttpEntityCallable(final TransferStatus status, final String uploadUri) {
        return new DelayedHttpEntityCallable<GmxcloudUploadResponse>() {
            @Override
            public GmxcloudUploadResponse call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final HttpResponse response;
                    final String cdash64 = status.getChecksum().hash;
                    final long size = status.getLength();
                    String cdash64SizeIncludedUri = uploadUri + Constant.X_CDASH64 + cdash64 + Constant.X_SIZE + size;
                    if(status.isSegment()) {
                        final HttpPut request = new HttpPut(cdash64SizeIncludedUri);
                        request.setEntity(entity);
                        response = session.getClient().execute(request);
                    }
                    else {
                        final HttpPost request = new HttpPost(cdash64SizeIncludedUri);
                        request.setEntity(entity);
                        request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                        response = session.getClient().execute(request);
                    }
                    try {
                        if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                            return GmxcloudUploadHelper.getGmxcloudUploadResponse(response);
                        }
                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                        throw new DefaultHttpResponseExceptionMappingService().map(
                            new HttpResponseException(response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
                    }
                    finally {
                        EntityUtils.consume(response.getEntity());
                    }
                }
                catch(HttpResponseException e) {
                    throw new DefaultHttpResponseExceptionMappingService().map(e);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e);
                }
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }

        };
    }

    protected void cancel(final Path file, final String resourceId) throws BackgroundException {
        log.warn(String.format("Cancel failed upload %s for %s", resourceId, file));
        new GmxcloudDeleteFeature(session, fileid).delete(Collections.singletonList(file), new DisabledLoginCallback(), new Delete.DisabledCallback());
    }
}
