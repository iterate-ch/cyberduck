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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.util.polling.SyncPoller;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.models.BlobCopyInfo;
import com.azure.storage.blob.options.BlobBeginCopyOptions;

public class AzureCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(AzureCopyFeature.class);

    private final AzureSession session;

    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

    public AzureCopyFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final BlobClient client = session.getClient().getBlobContainerClient(containerService.getContainer(copy).getName())
                .getBlobClient(containerService.getKey(copy));
            final SyncPoller<BlobCopyInfo, Void> poller = client.beginCopy(
                new BlobBeginCopyOptions(session.getClient().getBlobContainerClient(containerService.getContainer(source).getName())
                    .getBlobClient(containerService.getKey(source)).getBlobUrl()).setPollInterval(Duration.ofSeconds(1)));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Started copy for %s", copy));
            }
            poller.waitForCompletion();
            // Copy original file attributes
            return new Path(copy.getParent(), copy.getName(), copy.getType(), new PathAttributes(source.attributes()));
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source) && !containerService.isContainer(target);
    }
}
