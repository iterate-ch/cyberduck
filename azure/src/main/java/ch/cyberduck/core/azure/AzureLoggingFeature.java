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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.logging.LoggingConfiguration;

import java.util.EnumSet;

import com.microsoft.azure.storage.LoggingOperations;
import com.microsoft.azure.storage.LoggingProperties;
import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.ServiceProperties;
import com.microsoft.azure.storage.StorageException;

public class AzureLoggingFeature implements Logging {

    private AzureSession session;

    private OperationContext context;

    public AzureLoggingFeature(final AzureSession session, final OperationContext context) {
        this.session = session;
        this.context = context;
    }

    @Override
    public LoggingConfiguration getConfiguration(final Path container) throws BackgroundException {
        try {
            final ServiceProperties properties = session.getClient().downloadServiceProperties(null, context);
            return new LoggingConfiguration(!properties.getLogging().getLogOperationTypes().isEmpty());
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Cannot read container configuration", e);
        }
    }

    @Override
    public void setConfiguration(final Path container, final LoggingConfiguration configuration) throws BackgroundException {
        try {
            final ServiceProperties properties = session.getClient().downloadServiceProperties(null, context);
            final LoggingProperties l = new LoggingProperties();
            if(configuration.isEnabled()) {
                l.setLogOperationTypes(EnumSet.of(LoggingOperations.DELETE, LoggingOperations.READ, LoggingOperations.WRITE));
            }
            else {
                l.setLogOperationTypes(EnumSet.noneOf(LoggingOperations.class));
            }
            properties.setLogging(l);
            session.getClient().uploadServiceProperties(properties, null, context);
        }
        catch(StorageException e) {
            throw new AzureExceptionMappingService().map("Failure to write attributes of {0}", e, container);
        }
    }
}