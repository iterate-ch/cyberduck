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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.net.URISyntaxException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;

public class AzureCopyFeature implements Copy {
    private static final Logger log = Logger.getLogger(AzureCopyFeature.class);

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    public AzureCopyFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public void copy(final Path source, final Path copy, final TransferStatus status) throws BackgroundException {
        try {
            final CloudBlob target = session.getClient().getContainerReference(containerService.getContainer(copy).getName())
                    .getAppendBlobReference(containerService.getKey(copy));
            final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(source).getName())
                    .getBlobReferenceFromServer(containerService.getKey(source));
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setStoreBlobContentMD5(PreferencesFactory.get().getBoolean("azure.upload.md5"));
            final String id = target.startCopy(blob.getUri(),
                    AccessCondition.generateEmptyCondition(), AccessCondition.generateEmptyCondition(), options, context);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Started copy for %s with copy operation ID %s", copy, id));
            }
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }
}
