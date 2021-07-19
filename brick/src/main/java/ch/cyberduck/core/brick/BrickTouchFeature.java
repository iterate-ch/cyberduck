package ch.cyberduck.core.brick;/*
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
import ch.cyberduck.core.brick.io.swagger.client.ApiException;
import ch.cyberduck.core.brick.io.swagger.client.api.FileActionsApi;
import ch.cyberduck.core.brick.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.brick.io.swagger.client.model.BeginUploadPathBody;
import ch.cyberduck.core.brick.io.swagger.client.model.FileEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FileUploadPartEntity;
import ch.cyberduck.core.brick.io.swagger.client.model.FilesPathBody;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.List;

public class BrickTouchFeature implements Touch<Void> {

    private final BrickSession session;

    public BrickTouchFeature(final BrickSession session) {
        this.session = session;
    }

    @Override
    public Path touch(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final List<FileUploadPartEntity> uploadPartEntities = new FileActionsApi(new BrickApiClient(session.getApiKey(), session.getClient()))
                .beginUpload(file.getAbsolute(), new BeginUploadPathBody().parts(1).part(1));
            for(FileUploadPartEntity uploadPartEntity : uploadPartEntities) {
                status
                    .segment(true)
                    .withLength(0L)
                    .withOffset(0L);
                status.setUrl(uploadPartEntity.getUploadUri());
                status.setPart(1);
                new BrickWriteFeature(session).write(file, status, new DisabledConnectionCallback()).close();
                final FileEntity entity = new FilesApi(new BrickApiClient(session.getApiKey(), session.getClient())).postFilesPath(
                    new FilesPathBody()
                        .providedMtime(new DateTime(System.currentTimeMillis()))
                        .action("end").ref(uploadPartEntity.getRef()), file.getAbsolute());
                return file.withAttributes(new BrickAttributesFinderFeature(session).toAttributes(entity));
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(ApiException e) {
            throw new BrickExceptionMappingService().map("Cannot create {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create {0}", e, file);
        }
    }

    @Override
    public Touch<Void> withWriter(final Write<Void> writer) {
        return this;
    }
}
