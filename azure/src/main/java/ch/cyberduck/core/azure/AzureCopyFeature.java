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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlob;

public class AzureCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(AzureCopyFeature.class);

    private final AzureSession session;
    private final OperationContext context;

    private final PathContainerService containerService
        = new DirectoryDelimiterPathContainerService();

    public AzureCopyFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public Path copy(final Path source, final Path copy, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final CloudBlob target = session.getClient().getContainerReference(containerService.getContainer(copy).getName())
                .getAppendBlobReference(containerService.getKey(copy));
            final CloudBlob blob = session.getClient().getContainerReference(containerService.getContainer(source).getName())
                .getBlobReferenceFromServer(containerService.getKey(source));
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setStoreBlobContentMD5(new HostPreferences(session.getHost()).getBoolean("azure.upload.md5"));
            final URI s = session.getHost().getCredentials().isTokenAuthentication() ?
                URI.create(blob.getUri().toString() + session.getHost().getCredentials().getToken()) : blob.getUri();
            final String id = target.startCopy(s,
                AccessCondition.generateEmptyCondition(), AccessCondition.generateEmptyCondition(), options, context);
            if(log.isDebugEnabled()) {
                log.debug(String.format("Started copy for %s with copy operation ID %s", copy, id));
            }
            listener.sent(status.getLength());
            // Copy original file attributes
            return copy.withAttributes(new AzureAttributesFinderFeature(session, context).find(copy));
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(containerService.isContainer(source)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source)).withFile(source);
        }
        if(containerService.isContainer(target)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot copy {0}", "Error"), source)).withFile(source);
        }
    }
}
