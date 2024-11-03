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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Read;
import ch.cyberduck.core.io.StreamCopier;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.core.worker.DefaultExceptionMappingService;

import org.apache.commons.io.input.NullInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobInputStream;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.core.SR;

public class AzureReadFeature implements Read {
    private static final Logger log = LogManager.getLogger(AzureReadFeature.class);

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

    public AzureReadFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public InputStream read(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                    .getBlobReferenceFromServer(containerService.getKey(file));
            if(0L == blob.getProperties().getLength()) {
                return new NullInputStream(0L);
            }
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setConcurrentRequestCount(1);
            final BlobInputStream in = blob.openInputStream(AccessCondition.generateEmptyCondition(), options, context);
            if(status.isAppend()) {
                try {
                    return StreamCopier.skip(in, status.getOffset());
                }
                catch(IndexOutOfBoundsException e) {
                    // If offset is invalid
                    throw new DefaultExceptionMappingService().map("Download {0} failed", e, file);
                }
            }
            return new ProxyInputStream(in) {
                @Override
                protected void handleIOException(final IOException e) throws IOException {
                    if(StringUtils.equals(SR.STREAM_CLOSED, e.getMessage())) {
                        log.warn("Ignore failure {}", e);
                        return;
                    }
                    final Throwable cause = ExceptionUtils.getRootCause(e);
                    if(cause instanceof StorageException) {
                        throw new IOException(e.getMessage(), new AzureExceptionMappingService().map((StorageException) cause));
                    }
                    throw e;
                }
            };
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Download {0} failed", e, file);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
