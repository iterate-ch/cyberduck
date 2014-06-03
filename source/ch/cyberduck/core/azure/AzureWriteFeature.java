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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.windowsazure.services.blob.client.BlobRequestOptions;
import com.microsoft.windowsazure.services.blob.client.CloudBlockBlob;
import com.microsoft.windowsazure.services.core.storage.RetryNoRetry;
import com.microsoft.windowsazure.services.core.storage.StorageException;

/**
 * @version $Id$
 */
public class AzureWriteFeature implements Write {
    private static final Logger log = Logger.getLogger(AzureWriteFeature.class);

    private AzureSession session;

    private PathContainerService containerService
            = new AzurePathContainerService();

    public AzureWriteFeature(AzureSession session) {
        this.session = session;
    }

    @Override
    public Append append(final Path file, final Long length, final Cache cache) throws BackgroundException {
        if(new AzureFindFeature(session).withCache(cache).find(file)) {
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
            // Default metadata for new files
            final Map<String, String> metadata = new HashMap<String, String>();
            for(String m : Preferences.instance().getList("azure.metadata.default")) {
                if(StringUtils.isBlank(m)) {
                    continue;
                }
                if(!m.contains("=")) {
                    log.warn(String.format("Invalid header %s", m));
                    continue;
                }
                int split = m.indexOf('=');
                String key = m.substring(0, split);
                if(StringUtils.isBlank(key)) {
                    log.warn(String.format("Missing key in %s", m));
                    continue;
                }
                String value = m.substring(split + 1);
                if(StringUtils.isEmpty(value)) {
                    log.warn(String.format("Missing value in %s", m));
                    continue;
                }
                metadata.put(key, value);
            }
            blob.setMetadata(new HashMap<String, String>(metadata));
            final BlobRequestOptions options = new BlobRequestOptions();
            options.setRetryPolicyFactory(new RetryNoRetry());
            options.setStoreBlobContentMD5(Preferences.instance().getBoolean("azure.upload.md5"));
            return blob.openOutputStream(null, options, null);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Upload failed", e);
        }
        catch(URISyntaxException e) {
            throw new NotfoundException(e.getMessage(), e);
        }
    }
}
