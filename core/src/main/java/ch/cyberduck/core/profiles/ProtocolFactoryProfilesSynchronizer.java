package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ProtocolFactoryProfilesSynchronizer implements ProfilesSynchronizer {
    private static final Logger log = LogManager.getLogger(ProtocolFactoryProfilesSynchronizer.class.getName());

    private final ProtocolFactory registry;
    private final LocalProfilesFinder local;
    private final RemoteProfilesFinder remote;

    public ProtocolFactoryProfilesSynchronizer(final Session<?> session) {
        this(ProtocolFactory.get(), session, LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
                PreferencesFactory.get().getProperty("profiles.folder.name")));
    }

    public ProtocolFactoryProfilesSynchronizer(final ProtocolFactory registry, final Session<?> session, final Local directory) {
        this(ProtocolFactory.get(),
                // Find all locally installed profiles
                new LocalProfilesFinder(registry, directory, ProtocolFactory.BUNDLED_PROFILE_PREDICATE), session);
    }

    public ProtocolFactoryProfilesSynchronizer(final ProtocolFactory registry, final LocalProfilesFinder local, final Session<?> session) {
        this(registry, local,
                // Find all profiles from repository
                new RemoteProfilesFinder(registry, session));
    }

    public ProtocolFactoryProfilesSynchronizer(final ProtocolFactory registry, final RemoteProfilesFinder remote) {
        this(registry, LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
                PreferencesFactory.get().getProperty("profiles.folder.name")), remote);
    }

    public ProtocolFactoryProfilesSynchronizer(final ProtocolFactory registry, final Local directory, final RemoteProfilesFinder remote) {
        this(registry,
                // Find all locally installed profiles
                new LocalProfilesFinder(registry, directory, ProtocolFactory.BUNDLED_PROFILE_PREDICATE), remote);
    }

    public ProtocolFactoryProfilesSynchronizer(final ProtocolFactory registry, final LocalProfilesFinder local, final RemoteProfilesFinder remote) {
        this.registry = registry;
        this.local = local;
        this.remote = remote;
    }

    @Override
    public Set<ProfileDescription> sync(final ProfileMatcher matcher, final ProfilesFinder.Visitor visitor) throws BackgroundException {
        final Set<ProfileDescription> result = new HashSet<>();
        final Set<ProfileDescription> installed = local.find();
        final Set<ProfileDescription> available = remote.find();
        // Iterate over every installed profile and find match in repository
        installed.forEach(l -> {
            // Check for matching remote checksum and download profile if this version is not equal to latest
            final Optional<ProfileDescription> match = matcher.compare(available, l);
            if(match.isPresent()) {
                // Found matching checksum for profile in remote list which is not marked as latest version
                log.warn("Override {} with latest profile verison {}", l, match);
                // Remove previous version
                l.getProfile().ifPresent(registry::unregister);
                // Register updated profile by copying temporary file to application support
                match.get().getFile().ifPresent(value -> {
                    final Local copy = registry.register(value);
                    if(null != copy) {
                        final LocalProfileDescription d = new LocalProfileDescription(registry, copy);
                        log.debug("Add synched profile {}", d);
                        result.add(d);
                        visitor.visit(d);
                    }
                });
            }
            else {
                log.debug("Add local only profile {}", l);
                result.add(l);
                visitor.visit(l);
            }
        });
        // Iterate over all fetched profiles and when not installed
        available.forEach(description -> {
            if(description.isLatest()) {
                // Check if not already added previously when syncing with local list
                if(!result.contains(description)) {
                    log.debug("Add remote profile {}", description);
                    result.add(description);
                    visitor.visit(description);
                }
            }
        });
        local.cleanup();
        remote.cleanup();
        return result;
    }
}
