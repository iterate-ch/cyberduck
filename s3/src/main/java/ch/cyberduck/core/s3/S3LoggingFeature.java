package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.features.Logging;
import ch.cyberduck.core.logging.LoggingConfiguration;
import ch.cyberduck.core.preferences.HostPreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.S3BucketLoggingStatus;
import org.jets3t.service.model.StorageBucketLoggingStatus;

import java.util.Collections;
import java.util.EnumSet;

public class S3LoggingFeature implements Logging {
    private static final Logger log = LogManager.getLogger(S3LoggingFeature.class);

    private final S3Session session;
    private final PathContainerService containerService;

    public S3LoggingFeature(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public LoggingConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        if(file.getType().contains(Path.Type.upload)) {
            return LoggingConfiguration.empty();
        }
        try {
            final StorageBucketLoggingStatus status
                    = session.getClient().getBucketLoggingStatusImpl(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
            if(null == status) {
                log.warn("Failure parsing logging status for {}", bucket);
                return LoggingConfiguration.empty();
            }
            final LoggingConfiguration configuration = new LoggingConfiguration(status.isLoggingEnabled(),
                    status.getTargetBucketName());
            if(bucket.isRoot()) {
                configuration.setContainers(Collections.singletonList(
                        new Path(RequestEntityRestStorageService.findBucketInHostname(session.getHost()), EnumSet.of(Path.Type.volume, Path.Type.directory)))
                );
            }
            else {
                try {
                    configuration.setContainers(new S3BucketListService(session).list(Home.ROOT, new DisabledListProgressListener()).toList());
                }
                catch(AccessDeniedException | InteroperabilityException e) {
                    log.warn("Failure listing buckets. {}", e.getMessage());
                    configuration.setContainers(Collections.singletonList(bucket));
                }
            }
            return configuration;
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, file);
            }
            catch(AccessDeniedException | InteroperabilityException l) {
                log.warn("Missing permission to read logging configuration for {} {}", bucket.getName(), e.getMessage());
                return LoggingConfiguration.empty();
            }
        }
    }


    @Override
    public void setConfiguration(final Path file, final LoggingConfiguration configuration) throws BackgroundException {
        // Logging target bucket
        final Path bucket = containerService.getContainer(file);
        try {
            final S3BucketLoggingStatus status = new S3BucketLoggingStatus(
                    StringUtils.isNotBlank(configuration.getLoggingTarget()) ? configuration.getLoggingTarget() :
                            bucket.isRoot() ? RequestEntityRestStorageService.findBucketInHostname(session.getHost()) : bucket.getName(), null);
            if(configuration.isEnabled()) {
                status.setLogfilePrefix(HostPreferencesFactory.get(session.getHost()).getProperty("s3.logging.prefix"));
            }
            session.getClient().setBucketLoggingStatus(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), status, true);
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
