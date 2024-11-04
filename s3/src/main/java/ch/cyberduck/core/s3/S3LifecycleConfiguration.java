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

import ch.cyberduck.core.AlphanumericRandomStringService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.LifecycleConfig;

public class S3LifecycleConfiguration implements Lifecycle {
    private static final Logger log = LogManager.getLogger(S3LifecycleConfiguration.class);

    private final S3Session session;
    private final PathContainerService containerService;

    public S3LifecycleConfiguration(final S3Session session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public void setConfiguration(final Path file, final LifecycleConfiguration configuration) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        try {
            if(configuration.getTransition() != null || configuration.getExpiration() != null) {
                final LifecycleConfig config = new LifecycleConfig();
                // Unique identifier for the rule. The value cannot be longer than 255 characters. When you specify an empty prefix, the rule applies to all objects in the bucket
                final LifecycleConfig.Rule rule = config.newRule(
                        String.format("%s-%s", PreferencesFactory.get().getProperty("application.name"), new AlphanumericRandomStringService().random()),
                        StringUtils.EMPTY, true);
                if(configuration.getTransition() != null) {
                    final LifecycleConfig.Transition transition = rule.newTransition();
                    transition.setDays(configuration.getTransition());
                    transition.setStorageClass(new HostPreferences(session.getHost()).getProperty("s3.lifecycle.transition.class"));
                }
                if(configuration.getExpiration() != null) {
                    final LifecycleConfig.Expiration expiration = rule.newExpiration();
                    expiration.setDays(configuration.getExpiration());
                }
                session.getClient().setLifecycleConfig(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), config);
            }
            else {
                session.getClient().deleteLifecycleConfig(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
            }
        }
        catch(ServiceException e) {
            throw new S3ExceptionMappingService().map("Failure to write attributes of {0}", e, bucket);
        }
    }


    @Override
    public LifecycleConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path bucket = containerService.getContainer(file);
        if(file.getType().contains(Path.Type.upload)) {
            return LifecycleConfiguration.empty();
        }
        try {
            final LifecycleConfig status = session.getClient().getLifecycleConfig(bucket.isRoot() ? StringUtils.EMPTY : bucket.getName());
            if(null == status) {
                log.warn("Failure parsing lifecycle config for {}", bucket);
                return LifecycleConfiguration.empty();
            }
            Integer transition = null;
            Integer expiration = null;
            String storageClass = null;
            for(LifecycleConfig.Rule rule : status.getRules()) {
                if(StringUtils.isBlank(rule.getPrefix())) {
                    if(rule.getTransition() != null) {
                        transition = rule.getTransition().getDays();
                    }
                    if(rule.getExpiration() != null) {
                        expiration = rule.getExpiration().getDays();
                    }
                }
            }
            return new LifecycleConfiguration(transition, expiration);
        }
        catch(ServiceException e) {
            try {
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, bucket);
            }
            catch(AccessDeniedException | InteroperabilityException l) {
                log.warn("Missing permission to read lifecycle configuration for {} {}", bucket, e.getMessage());
                return LifecycleConfiguration.empty();
            }
        }
    }
}
