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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;

public class GoogleStorageLoggingFeature implements Logging, DistributionLogging {
    private static final Logger log = LogManager.getLogger(GoogleStorageLoggingFeature.class);

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
            final Storage.Buckets.Get request = session.getClient().buckets().get(bucket.getName());
            if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            final Bucket.Logging status = request.execute().getLogging();
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
    public void setConfiguration(final Path file, final LoggingConfiguration configuration) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            final Storage.Buckets.Patch request = session.getClient().buckets().patch(bucket.getName(),
                    new Bucket().setLogging(new Bucket.Logging()
                            .setLogObjectPrefix(configuration.isEnabled() ? new HostPreferences(session.getHost()).getProperty("google.logging.prefix") : null)
                            .setLogBucket(StringUtils.isNotBlank(configuration.getLoggingTarget()) ? configuration.getLoggingTarget() : bucket.getName()))
            );
            if(bucket.attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            request.execute();
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
