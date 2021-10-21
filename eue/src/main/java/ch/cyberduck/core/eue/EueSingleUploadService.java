package ch.cyberduck.core.eue;

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
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.UploadType;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Upload;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.http.HttpUploadFeature;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.security.MessageDigest;

public class EueSingleUploadService extends HttpUploadFeature<EueUploadHelper.UploadResponse, MessageDigest> {

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    private Write<EueUploadHelper.UploadResponse> writer;

    public EueSingleUploadService(final EueSession session, final EueResourceIdProvider fileid, final Write<EueUploadHelper.UploadResponse> writer) {
        super(writer);
        this.session = session;
        this.fileid = fileid;
        this.writer = writer;
    }

    @Override
    public EueUploadHelper.UploadResponse upload(final Path file, final Local local, final BandwidthThrottle throttle, final StreamListener listener,
                                                 final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final String uploadUri;
        if(status.isExists()) {
            uploadUri = EueUploadHelper.updateResource(session, fileid.getFileId(file, new DisabledListProgressListener()),
                    UploadType.SIMPLE).getUploadURI();
        }
        else {
            final ResourceCreationResponseEntry uploadResourceCreationResponseEntry = EueUploadHelper.
                    createResource(session, fileid.getFileId(file.getParent(), new DisabledListProgressListener()), file.getName(),
                            status, UploadType.SIMPLE);
            uploadUri = uploadResourceCreationResponseEntry.getEntity().getUploadURI();
        }
        status.setUrl(uploadUri);
        status.setChecksum(writer.checksum(file, status).compute(local.getInputStream(), status));
        return super.upload(file, local, throttle, listener, status, callback);
    }

    @Override
    public Upload<EueUploadHelper.UploadResponse> withWriter(final Write<EueUploadHelper.UploadResponse> writer) {
        this.writer = writer;
        return super.withWriter(writer);
    }
}
