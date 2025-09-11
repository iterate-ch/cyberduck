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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.logging.LoggingConfiguration;

import java.util.Collections;
import java.util.EnumSet;

import com.azure.core.exception.HttpResponseException;
import com.azure.storage.blob.models.BlobAnalyticsLogging;
import com.azure.storage.blob.models.BlobRetentionPolicy;
import com.azure.storage.blob.models.BlobServiceProperties;

public class AzureLoggingFeature implements Logging {

    private final AzureSession session;

    public AzureLoggingFeature(final AzureSession session) {
        this.session = session;
    }

    @Override
    public LoggingConfiguration getConfiguration(final Path container) throws BackgroundException {
        try {
            final BlobServiceProperties properties = session.getClient().getProperties();
            final LoggingConfiguration configuration = new LoggingConfiguration(
                    properties.getLogging().isRead() || properties.getLogging().isWrite() || properties.getLogging().isDelete(),
                    "$logs"
            );
            // When you have configured Storage Logging to log request data from your storage account, it saves the log data
            // to blobs in a container named $logs in your storage account.
            configuration.setContainers(Collections.singletonList(
                    new Path("/$logs", EnumSet.of(Path.Type.volume, Path.Type.directory)))
            );
            return configuration;
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Cannot read container configuration", e);
        }
    }

    @Override
    public void setConfiguration(final Path container, final LoggingConfiguration configuration) throws BackgroundException {
        try {
            final BlobServiceProperties properties = session.getClient().getProperties();
            properties.setLogging(new BlobAnalyticsLogging()
                    .setVersion("2.0")
                    .setRetentionPolicy(new BlobRetentionPolicy().setEnabled(false))
                    .setDelete(configuration.isEnabled())
                    .setRead(configuration.isEnabled())
                    .setWrite(configuration.isEnabled())
            );
            session.getClient().setProperties(properties);
        }
        catch(HttpResponseException e) {
            throw new AzureExceptionMappingService().map("Failure to write attributes of {0}", e, container);
        }
    }
}
