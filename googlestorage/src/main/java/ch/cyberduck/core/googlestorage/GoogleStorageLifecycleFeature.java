package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Lifecycle;
import ch.cyberduck.core.lifecycle.LifecycleConfiguration;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;

public class GoogleStorageLifecycleFeature implements Lifecycle {
    private static final Logger log = LogManager.getLogger(GoogleStorageLifecycleFeature.class);

    private final GoogleStorageSession session;
    private final PathContainerService containerService;

    public GoogleStorageLifecycleFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public void setConfiguration(final Path file, final LifecycleConfiguration configuration) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            if(configuration.getTransition() != null || configuration.getExpiration() != null) {
                final Bucket.Lifecycle config = new Bucket.Lifecycle();
                // Unique identifier for the rule. The value cannot be longer than 255 characters. When you specify an empty prefix, the rule applies to all objects in the bucket
                final List<Bucket.Lifecycle.Rule> rules = new ArrayList<>();
                if(configuration.getTransition() != null) {
                    rules.add(new Bucket.Lifecycle.Rule().setCondition(new Bucket.Lifecycle.Rule.Condition()
                                    .setAge(configuration.getTransition()))
                            .setAction(new Bucket.Lifecycle.Rule.Action()
                                    .setType("SetStorageClass").setStorageClass(
                                            new HostPreferences(session.getHost()).getProperty("googlestorage.lifecycle.transition.class")
                                    )));
                }
                if(configuration.getExpiration() != null) {
                    rules.add(new Bucket.Lifecycle.Rule().setCondition(new Bucket.Lifecycle.Rule.Condition()
                                    .setAge(configuration.getExpiration()))
                            .setAction(new Bucket.Lifecycle.Rule.Action()
                                    .setType("Delete")));
                }
                final Storage.Buckets.Patch request = session.getClient().buckets().patch(container.getName(),
                        new Bucket().setLifecycle(config.setRule(rules)));
                if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    request.setUserProject(session.getHost().getCredentials().getUsername());
                }
                request.execute();
            }
            else {
                // Empty lifecycle configuration
                final Storage.Buckets.Patch request = session.getClient().buckets().patch(container.getName(), new Bucket()
                        .setLifecycle(new Bucket.Lifecycle().setRule(Collections.emptyList())));
                if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    request.setUserProject(session.getHost().getCredentials().getUsername());
                }
                request.execute();
            }
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to write attributes of {0}", e, container);
        }
    }


    @Override
    public LifecycleConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(container.isRoot()) {
            return LifecycleConfiguration.empty();
        }
        try {
            final Storage.Buckets.Get request = session.getClient().buckets().get(container.getName());
            if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            final Bucket.Lifecycle status = request.execute().getLifecycle();
            if(null != status) {
                Integer transition = null;
                Integer expiration = null;
                for(Bucket.Lifecycle.Rule rule : status.getRule()) {
                    if("SetStorageClass".equals(rule.getAction().getType())) {
                        transition = rule.getCondition().getAge();
                    }
                    if("Delete".equals(rule.getAction().getType())) {
                        expiration = rule.getCondition().getAge();
                    }
                }
                return new LifecycleConfiguration(transition, expiration);
            }
            return LifecycleConfiguration.empty();
        }
        catch(IOException e) {
            try {
                throw new GoogleStorageExceptionMappingService().map("Failure to read attributes of {0}", e, container);
            }
            catch(AccessDeniedException | InteroperabilityException l) {
                log.warn(String.format("Missing permission to read lifecycle configuration for %s %s", container, e.getMessage()));
                return LifecycleConfiguration.empty();
            }
        }
    }
}
