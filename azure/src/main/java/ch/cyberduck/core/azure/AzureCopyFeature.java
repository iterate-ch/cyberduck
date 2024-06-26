package ch.cyberduck.core.azure;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DirectoryDelimiterPathContainerService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
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
    public Path copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
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
            listener.sent(status.getLength());
            return copy;
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        if(containerService.isContainer(source)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(source);
        }
        if(containerService.isContainer(directory)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source.getName())).withFile(source);
        }
    }
}
