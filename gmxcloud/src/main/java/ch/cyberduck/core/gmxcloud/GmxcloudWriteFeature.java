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
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UploadType;
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

public class GmxcloudWriteFeature extends AbstractHttpWriteFeature<GmxcloudUploadHelper.GmxcloudUploadResponse> {
    private static final Logger log = Logger.getLogger(GmxcloudWriteFeature.class);

    private final GmxcloudSession session;
    private final GmxcloudResourceIdProvider fileid;

    public GmxcloudWriteFeature(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<GmxcloudUploadHelper.GmxcloudUploadResponse> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        String uploadUri = status.getUrl();
        String resourceId = status.getParameters().get(GmxcloudLargeUploadService.RESOURCE_ID);
        if(null == uploadUri) {
            if(status.isExists()) {
                resourceId = fileid.getFileId(file, new DisabledListProgressListener());
                uploadUri = GmxcloudUploadHelper.updateResource(session, resourceId, UploadType.SIMPLE).getUploadURI();
            }
            else {
                final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = GmxcloudUploadHelper
                        .createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                                UploadType.SIMPLE);
                resourceId = GmxcloudResourceIdProvider.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation());
                uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
            }
        }
        final HttpResponseOutputStream<GmxcloudUploadHelper.GmxcloudUploadResponse> stream = this.write(
                file, status, getDelayedHttpEntityCallable(status, uploadUri));
        fileid.cache(file, resourceId);
        return stream;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new GmxcloudCdash64Compute();
    }

    private DelayedHttpEntityCallable<GmxcloudUploadHelper.GmxcloudUploadResponse> getDelayedHttpEntityCallable(final TransferStatus status, final String uploadUri) {
        return new DelayedHttpEntityCallable<GmxcloudUploadHelper.GmxcloudUploadResponse>() {
            @Override
            public GmxcloudUploadHelper.GmxcloudUploadResponse call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final HttpResponse response;
                    final String cdash64 = status.getChecksum().hash;
                    final long size = status.getLength();
                    String cdash64SizeIncludedUri = uploadUri + "&x_cdash64=" + cdash64 + "&x_size=" + size;
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
                            return GmxcloudUploadHelper.parseUploadResponse(response);
                        }
                        EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                        throw new GmxcloudExceptionMappingService().map(response);
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
}
