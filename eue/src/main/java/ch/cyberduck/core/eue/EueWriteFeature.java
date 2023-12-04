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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ChecksumException;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DefaultHttpResponseExceptionMappingService;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.SHA256ChecksumCompute;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

public class EueWriteFeature extends AbstractHttpWriteFeature<EueWriteFeature.Chunk> {
    private static final Logger log = LogManager.getLogger(EueWriteFeature.class);

    public static final String RESOURCE_ID = "resourceId";

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueWriteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        super(new EueAttributesAdapter());
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<Chunk> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String uploadUri;
        final String resourceId;
        if(null == status.getUrl()) {
            if(status.isExists()) {
                resourceId = fileid.getFileId(file);
                uploadUri = EueUploadHelper.updateResource(session, resourceId, status, UploadType.SIMPLE).getUploadURI();
            }
            else {
                final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = EueUploadHelper
                        .createResource(session, fileid.getFileId(file.getParent()), file.getName(),
                                status, UploadType.SIMPLE);
                resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(uploadResourceCreationResponseEntry.getHeaders().getLocation());
                uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
            }
        }
        else {
            uploadUri = status.getUrl();
            resourceId = status.getParameters().get(RESOURCE_ID);
        }
        final HttpResponseOutputStream<Chunk> stream = this.write(file, status,
                new DelayedHttpEntityCallable<Chunk>(file) {
                    @Override
                    public Chunk call(final AbstractHttpEntity entity) throws BackgroundException {
                        try {
                            final HttpResponse response;
                            final StringBuilder uploadUriWithParameters = new StringBuilder(uploadUri);
                            if(!Checksum.NONE.equals(status.getChecksum())) {
                                uploadUriWithParameters.append(String.format("&x_cdash64=%s",
                                        new ChunkListSHA256ChecksumCompute().compute(status.getLength(), Hex.decodeHex(status.getChecksum().hash))));
                            }
                            if(status.getLength() != -1) {
                                uploadUriWithParameters.append(String.format("&x_size=%d", status.getLength()));
                            }
                            if(status.isSegment()) {
                                // Chunked upload from large upload service
                                uploadUriWithParameters.append(String.format("&x_offset=%d",
                                        new HostPreferences(session.getHost()).getLong("eue.upload.multipart.size") * (status.getPart() - 1)));
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
                                    return new Chunk(resourceId, status.getPart(), status.getLength(), status.getChecksum());
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
                        catch(DecoderException e) {
                            throw new ChecksumException(LocaleFactory.localizedString("Checksum failure", "Error"), e);
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
        return new SHA256ChecksumCompute();
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        return new Append(false).withStatus(status);
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.timestamp);
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

    public static final class Chunk {
        private final String resourceId;
        private final Integer partnumber;
        private final Long length;
        private final String cdash64;
        private final Checksum checksum;

        public Chunk(final String resourceId, final Long length, final String cdash64) {
            this.resourceId = resourceId;
            this.partnumber = -1;
            this.length = length;
            this.cdash64 = cdash64;
            this.checksum = Checksum.NONE;
        }

        public Chunk(final String resourceId, final Integer partnumber, final Long length, final Checksum checksum) {
            this.resourceId = resourceId;
            this.partnumber = partnumber;
            this.length = length;
            this.cdash64 = StringUtils.EMPTY;
            this.checksum = checksum;
        }

        public String getResourceId() {
            return resourceId;
        }

        public Integer getPartnumber() {
            return partnumber;
        }

        public Long getLength() {
            return length;
        }

        public String getCdash64() {
            return cdash64;
        }

        public Checksum getChecksum() {
            return checksum;
        }
    }
}
