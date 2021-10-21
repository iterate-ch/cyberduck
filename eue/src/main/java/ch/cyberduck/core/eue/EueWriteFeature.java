package ch.cyberduck.core.eue;

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
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

public class EueWriteFeature extends AbstractHttpWriteFeature<EueUploadHelper.UploadResponse> {
    private static final Logger log = Logger.getLogger(EueWriteFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueWriteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<EueUploadHelper.UploadResponse> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String uploadUri;
        final String resourceId;
        if(null == status.getUrl()) {
            if(status.isExists()) {
                resourceId = fileid.getFileId(file, new DisabledListProgressListener());
                uploadUri = EueUploadHelper.updateResource(session, resourceId, UploadType.SIMPLE).getUploadURI();
            }
            else {
                final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = EueUploadHelper
                        .createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                                status, UploadType.SIMPLE);
                resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation());
                uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
            }
        }
        else {
            uploadUri = status.getUrl();
            resourceId = status.getParameters().get(EueLargeUploadService.RESOURCE_ID);
        }
        final HttpResponseOutputStream<EueUploadHelper.UploadResponse> stream = this.write(file, status,
                new DelayedHttpEntityCallable<EueUploadHelper.UploadResponse>() {
                    @Override
                    public EueUploadHelper.UploadResponse call(final AbstractHttpEntity entity) throws BackgroundException {
                        try {
                            final HttpResponse response;
                            final StringBuilder uploadUriWithParameters = new StringBuilder(uploadUri);
                            if(!Checksum.NONE.equals(status.getChecksum())) {
                                uploadUriWithParameters.append(String.format("&x_cdash64=%s", status.getChecksum().hash));
                            }
                            if(status.getLength() != -1) {
                                uploadUriWithParameters.append(String.format("&x_size=%d", status.getLength()));
                            }
                            if(status.isSegment()) {
                                final HttpPut request = new HttpPut(uploadUriWithParameters.toString());
                                request.setEntity(entity);
                                response = session.getClient().execute(request);
                            }
                            else {
                                final HttpPost request = new HttpPost(uploadUriWithParameters.toString());
                                request.setEntity(entity);
                                request.setHeader(HttpHeaders.CONTENT_TYPE, MimeTypeService.DEFAULT_CONTENT_TYPE);
                                response = session.getClient().execute(request);
                            }
                            try {
                                if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                                    return EueUploadHelper.parseUploadResponse(response);
                                }
                                EntityUtils.updateEntity(response, new BufferedHttpEntity(response.getEntity()));
                                throw new EueExceptionMappingService().map(response);
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
                }
        );
        fileid.cache(file, resourceId);
        return stream;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return new ChunkListSHA256ChecksumCompute();
    }

    public void cancel(final String uploadUri) throws BackgroundException {
        final HttpDelete request = new HttpDelete(uploadUri);
        try {
            session.getClient().execute(request);
        }
        catch(HttpResponseException e) {
            throw new DefaultHttpResponseExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }
}
