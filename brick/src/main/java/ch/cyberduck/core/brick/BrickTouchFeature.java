package ch.cyberduck.core.brick;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FileUploadPartEntity;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;
import java.util.Collections;

public class BrickTouchFeature implements Touch<FileEntity> {

    private final BrickSession session;

    public BrickTouchFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final BrickUploadFeature upload = new BrickUploadFeature(session, new BrickWriteFeature(session));
            final FileUploadPartEntity uploadPartEntity = upload.startUpload(file);
            status.withLength(0L).withOffset(0L);
            status.setUrl(uploadPartEntity.getUploadUri());
            status.setSegment(true);
            status.setTimestamp(System.currentTimeMillis());
            status.setPart(1);
            new BrickWriteFeature(session).write(file, status, new DisabledConnectionCallback()).close();
            final FileEntity entity = upload.completeUpload(file, uploadPartEntity.getRef(), status, Collections.singletonList(status));
            return file.withAttributes(new BrickAttributesFinderFeature(session).toAttributes(entity));
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }

    @Override
    public Touch<FileEntity> withWriter(final Write<FileEntity> writer) {
        return this;
    }
}
