package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;

public class AzureTouchFeature implements Touch {

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureTouchFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public boolean isSupported(final Path workdir) {
        return !workdir.isRoot();
    }

    @Override
    public void touch(final Path file, final TransferStatus transferStatus) throws BackgroundException {
        try {
            final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                    .getAppendBlobReference(containerService.getKey(file));
            final BlobRequestOptions options = new BlobRequestOptions();
            blob.upload(new ByteArrayInputStream(new byte[]{}), 0L, AccessCondition.generateEmptyCondition(), options, context);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot create file {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot create file {0}", e, file);
        }
    }
}
