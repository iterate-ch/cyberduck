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
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.io.VoidStatusOutputStream;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.AppendWriteFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.HttpHeaders;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobOutputStream;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudAppendBlob;
import com.microsoft.azure.storage.core.SR;

public class AzureWriteFeature extends AppendWriteFeature<Void> implements Write<Void> {
    private static final Logger log = Logger.getLogger(AzureWriteFeature.class);

    private final AzureSession session;

    private final OperationContext context;

    private final PathContainerService containerService
            = new AzurePathContainerService();

    private final Preferences preferences
            = PreferencesFactory.get();

    public AzureWriteFeature(final AzureSession session, final OperationContext context) {
        super(session);
        this.session = session;
        this.context = context;
    }

    protected AzureWriteFeature(final AzureSession session, final OperationContext context, final Find finder, final AttributesFinder attributes) {
        super(finder, attributes);
        this.session = session;
        this.context = context;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public boolean random() {
        return false;
    }

    @Override
    public StatusOutputStream<Void> write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final CloudAppendBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                    .getAppendBlobReference(containerService.getKey(file));
            if(StringUtils.isNotBlank(status.getMime())) {
                blob.getProperties().setContentType(status.getMime());
            }
            final HashMap<String, String> headers = new HashMap<>();
            // Add previous metadata when overwriting file
            headers.putAll(status.getMetadata());
            blob.setMetadata(headers);
            // Remove additional headers not allowed in metadata and move to properties
            if(headers.containsKey(HttpHeaders.CACHE_CONTROL)) {
                blob.getProperties().setCacheControl(headers.get(HttpHeaders.CACHE_CONTROL));
                headers.remove(HttpHeaders.CACHE_CONTROL);
            }
            if(headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
                blob.getProperties().setCacheControl(headers.get(HttpHeaders.CONTENT_TYPE));
                headers.remove(HttpHeaders.CONTENT_TYPE);
            }
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setConcurrentRequestCount(1);
            options.setStoreBlobContentMD5(preferences.getBoolean("azure.upload.md5"));
            final BlobOutputStream out;
            if(status.isAppend()) {
                out = blob.openWriteExisting(AccessCondition.generateEmptyCondition(), options, context);
            }
            else {
                out = blob.openWriteNew(AccessCondition.generateEmptyCondition(), options, context);
            }
            return new VoidStatusOutputStream(out) {
                @Override
                protected void handleIOException(final IOException e) throws IOException {
                    if(StringUtils.equals(SR.STREAM_CLOSED, e.getMessage())) {
                        log.warn(String.format("Ignore failure %s", e));
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
            throw new AzureExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
