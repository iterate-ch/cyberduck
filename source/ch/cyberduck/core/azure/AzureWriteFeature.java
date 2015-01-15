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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultFindFeature;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.azure.storage.AccessCondition;
import com.microsoft.azure.storage.RetryNoRetry;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

/**
 * @version $Id$
 */
public class AzureWriteFeature implements Write {
    private static final Logger log = Logger.getLogger(AzureWriteFeature.class);

    private AzureSession session;

    private Find finder;

    private PathContainerService containerService
            = new AzurePathContainerService();

    private Preferences preferences
            = PreferencesFactory.get();

    private Map<String, String> metadata
            = preferences.getMap("azure.metadata.default");

    public AzureWriteFeature(final AzureSession session) {
        this.session = session;
        this.finder = new DefaultFindFeature(session);
    }

    public AzureWriteFeature withMetadata(final Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache cache) throws BackgroundException {
        if(finder.withCache(cache).find(file)) {
            return Write.override;
        }
        return Write.notfound;
    }

    @Override
    public boolean temporary() {
        return false;
    }

    @Override
    public OutputStream write(final Path file, final TransferStatus status) throws BackgroundException {
        try {
            final CloudBlockBlob blob = session.getClient().getContainerReference(containerService.getContainer(file).getName())
                    .getBlockBlobReference(containerService.getKey(file));
            blob.getProperties().setContentType(status.getMime());
            final HashMap<String, String> headers = new HashMap<>();
            headers.putAll(metadata); // Default
            headers.putAll(status.getMetadata()); // Previous
            blob.setMetadata(headers);
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setRetryPolicyFactory(new RetryNoRetry());
            options.setStoreBlobContentMD5(preferences.getBoolean("azure.upload.md5"));
            return blob.openOutputStream(AccessCondition.generateEmptyCondition(), options, null);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Upload {0} failed", e, file);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
