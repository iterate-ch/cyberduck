package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.cdn.features.DistributionLogging;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.storage.model.Bucket;

public class GoogleStorageLoggingFeature implements Logging, DistributionLogging {
    private static final Logger log = Logger.getLogger(GoogleStorageLoggingFeature.class);

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    public GoogleStorageLoggingFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public LoggingConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        if(bucket.isRoot()) {
            return LoggingConfiguration.empty();
        }
        try {
            final Bucket.Logging status = session.getClient().buckets().get(bucket.getName()).execute().getLogging();
            if(null == status) {
                return LoggingConfiguration.empty();
            }
            final LoggingConfiguration configuration = new LoggingConfiguration(
                status.getLogObjectPrefix() != null, status.getLogBucket());
            try {
                configuration.setContainers(new GoogleStorageBucketListService(session).list(
                    new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory)),
                    new DisabledListProgressListener()).toList());
            }
            catch(AccessDeniedException | InteroperabilityException e) {
                log.warn(String.format("Failure listing buckets. %s", e.getMessage()));
            }
            return configuration;
        }
        catch(IOException e) {
            try {
                throw new GoogleStorageExceptionMappingService().map("Failure to read attributes of {0}", e, bucket);
            }
            catch(AccessDeniedException | InteroperabilityException l) {
                log.warn(String.format("Missing permission to read logging configuration for %s %s", bucket.getName(), e.getMessage()));
                return LoggingConfiguration.empty();
            }
        }
    }

    @Override
    public void setConfiguration(final Path container, final LoggingConfiguration configuration) throws BackgroundException {
        try {
            session.getClient().buckets().patch(container.getName(),
                new Bucket().setLogging(new Bucket.Logging()
                    .setLogObjectPrefix(configuration.isEnabled() ? new HostPreferences(session.getHost()).getProperty("google.logging.prefix") : null)
                    .setLogBucket(StringUtils.isNotBlank(configuration.getLoggingTarget()) ? configuration.getLoggingTarget() : container.getName()))
            ).execute();
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to write attributes of {0}", e, container);
        }
    }
}
