package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Copy;

import java.net.URISyntaxException;

import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureCopyFeature implements Copy {

    private AzureSession session;

    private PathContainerService containerService
            = new AzurePathContainerService();

    public AzureCopyFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public void copy(Path source, Path copy) throws BackgroundException {
        try {
            final CloudBlockBlob target = session.getClient().getContainerReference(containerService.getContainer(copy).getName())
                    .getBlockBlobReference(containerService.getKey(copy));
            final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(source).getName())
                    .getBlockBlobReference(containerService.getKey(source));
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setRetryPolicyFactory(new RetryNoRetry());
            options.setStoreBlobContentMD5(Preferences.instance().getBoolean("azure.upload.md5"));
            target.copyFromBlob(blob, null, null, options, null);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
