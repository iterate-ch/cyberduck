package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UploadType;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.security.MessageDigest;

public class GmxcloudSingleUploadService extends HttpUploadFeature<GmxcloudUploadHelper.GmxcloudUploadResponse, MessageDigest> {

    private final GmxcloudSession session;
    private final GmxcloudResourceIdProvider fileid;
    private final Write<GmxcloudUploadHelper.GmxcloudUploadResponse> writer;

    public GmxcloudSingleUploadService(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid, final Write<GmxcloudUploadHelper.GmxcloudUploadResponse> writer) {
        super(writer);
        this.session = session;
        this.fileid = fileid;
        this.writer = writer;
    }

    @Override
    public GmxcloudUploadHelper.GmxcloudUploadResponse upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                                              final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String uploadUri;
        if(status.isExists()) {
            uploadUri = GmxcloudUploadHelper.updateResource(session, fileid.getFileId(file, new DisabledListProgressListener()),
                    UploadType.SIMPLE).getUploadURI();
        }
        else {
            final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = GmxcloudUploadHelper.
                    createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                            UploadType.SIMPLE);
            uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
        }
        status.setUrl(uploadUri);
        status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
        return super.upload(file, local, throttle, listener, status, callback);
    }
}
