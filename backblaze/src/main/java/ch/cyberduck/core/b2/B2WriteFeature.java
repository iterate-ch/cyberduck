package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.AbstractHttpWriteFeature;
import ch.cyberduck.core.http.DelayedHttpEntityCallable;
import ch.cyberduck.core.http.HttpResponseOutputStream;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.ChecksumCompute;
import ch.cyberduck.core.io.ChecksumComputeFactory;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.entity.AbstractHttpEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import synapticloop.b2.exception.B2ApiException;
import synapticloop.b2.response.B2FileInfoResponse;
import synapticloop.b2.response.B2FileResponse;
import synapticloop.b2.response.B2GetUploadPartUrlResponse;
import synapticloop.b2.response.B2GetUploadUrlResponse;
import synapticloop.b2.response.B2UploadPartResponse;
import synapticloop.b2.response.BaseB2Response;

import static ch.cyberduck.core.b2.B2MetadataFeature.X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS;

public class B2WriteFeature extends AbstractHttpWriteFeature<BaseB2Response> implements Write<BaseB2Response> {
    private static final Logger log = LogManager.getLogger(B2WriteFeature.class);

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    private final ThreadLocal<B2GetUploadUrlResponse> urls
        = new ThreadLocal<>();

    public B2WriteFeature(final B2Session session, final B2VersionIdProvider fileid) {
        super(new B2AttributesFinderFeature(session, fileid));
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public HttpResponseOutputStream<BaseB2Response> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        // Submit store call to background thread
        final DelayedHttpEntityCallable<BaseB2Response> command = new DelayedHttpEntityCallable<BaseB2Response>(file) {
            /**
             * @return The SHA-1 returned by the server for the uploaded object
             */
            @Override
            public BaseB2Response call(final AbstractHttpEntity entity) throws BackgroundException {
                try {
                    final Checksum checksum = status.getChecksum();
                    if(status.isSegment()) {
                        final B2GetUploadPartUrlResponse uploadUrl = session.getClient().getUploadPartUrl(status.getParameters().get("fileId"));
                        return session.getClient().uploadLargeFilePart(uploadUrl, status.getPart(), entity, checksum.hash);
                    }
                    else {
                        if(null == urls.get()) {
                            final B2GetUploadUrlResponse uploadUrl = session.getClient().getUploadUrl(fileid.getVersionId(containerService.getContainer(file)));
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Obtained upload URL %s for file %s", uploadUrl, file));
                            }
                            urls.set(uploadUrl);
                            return this.upload(uploadUrl, entity, checksum);
                        }
                        else {
                            final B2GetUploadUrlResponse uploadUrl = urls.get();
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Use cached upload URL %s for file %s", uploadUrl, file));
                            }
                            try {
                                return this.upload(uploadUrl, entity, checksum);
                            }
                            catch(IOException | B2ApiException e) {
                                // Upload many files to the same upload_url until that URL gives an error
                                log.warn(String.format("Remove cached upload URL after failure %s", e));
                                urls.remove();
                                // Retry
                                return this.upload(uploadUrl, entity, checksum);
                            }
                        }
                    }
                }
                catch(B2ApiException e) {
                    throw new B2ExceptionMappingService(fileid).map("Upload {0} failed", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map("Upload {0} failed", e, file);
                }
            }

            private BaseB2Response upload(final B2GetUploadUrlResponse uploadUrl, final AbstractHttpEntity entity, final Checksum checksum) throws B2ApiException, IOException {
                final Map<String, String> fileinfo = new HashMap<>(status.getMetadata());
                if(null != status.getTimestamp()) {
                    fileinfo.put(X_BZ_INFO_SRC_LAST_MODIFIED_MILLIS, String.valueOf(status.getTimestamp()));
                }
                final B2FileResponse response = session.getClient().uploadFile(uploadUrl,
                        containerService.getKey(file),
                        entity, checksum.algorithm == HashAlgorithm.sha1 ? checksum.hash : "do_not_verify",
                        status.getMime(), fileinfo);
                fileid.cache(file, response.getFileId());
                return response;
            }

            @Override
            public long getContentLength() {
                return status.getLength();
            }
        };
        return this.write(file, status, command);
    }

    @Override
    public boolean timestamp() {
        return true;
    }

    @Override
    public ChecksumCompute checksum(final Path file, final TransferStatus status) {
        return ChecksumComputeFactory.get(HashAlgorithm.sha1);
    }

    @Override
    public Append append(final Path file, final TransferStatus status) throws BackgroundException {
        final B2LargeUploadPartService partService = new B2LargeUploadPartService(session, fileid);
        final List<B2FileInfoResponse> upload = partService.find(file);
        if(!upload.isEmpty()) {
            Long size = 0L;
            for(B2UploadPartResponse completed : partService.list(upload.iterator().next().getFileId())) {
                size += completed.getContentLength();
            }
            return new Append(true).withStatus(status).withSize(size);
        }
        return new Append(false).withStatus(status);
    }
}
