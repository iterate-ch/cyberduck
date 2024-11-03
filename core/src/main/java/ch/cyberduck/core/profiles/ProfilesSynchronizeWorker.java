package ch.cyberduck.core.profiles;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.transfer.download.CompareFilter;
import ch.cyberduck.core.transfer.symlink.DisabledDownloadSymlinkResolver;
import ch.cyberduck.core.worker.Worker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Merge local set with latest versions from server
 */
public class ProfilesSynchronizeWorker extends Worker<Set<ProfileDescription>> {
    private static final Logger log = LogManager.getLogger(ProfilesSynchronizeWorker.class.getName());

    private final ProtocolFactory registry;
    private final Local directory;
    private final ProfilesFinder.Visitor visitor;

    public ProfilesSynchronizeWorker(final ProfilesFinder.Visitor visitor) {
        this(ProtocolFactory.get(), visitor);
    }

    /**
     * @param registry Protocol registry
     * @param visitor  Callback for synched profiles
     */
    public ProfilesSynchronizeWorker(final ProtocolFactory registry, final ProfilesFinder.Visitor visitor) {
        this(registry, LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
            PreferencesFactory.get().getProperty("profiles.folder.name")), visitor);
    }

    public ProfilesSynchronizeWorker(final ProtocolFactory registry, final Local directory, final ProfilesFinder.Visitor visitor) {
        this.registry = registry;
        this.directory = directory;
        this.visitor = visitor;
    }

    @Override
    public Set<ProfileDescription> initialize() {
        try {
            return new LocalProfilesFinder(registry, directory, ProtocolFactory.BUNDLED_PROFILE_PREDICATE).find();
        }
        catch(BackgroundException e) {
            return Collections.emptySet();
        }
    }

    @Override
    public Set<ProfileDescription> run(final Session<?> session) throws BackgroundException {
        final Set<ProfileDescription> returned = new HashSet<>();
        // Find all locally installed profiles
        final LocalProfilesFinder localProfilesFinder = new LocalProfilesFinder(registry, directory, ProtocolFactory.BUNDLED_PROFILE_PREDICATE);
        final Set<ProfileDescription> installed = localProfilesFinder.find();
        // Find all profiles from repository
        final RemoteProfilesFinder remoteProfilesFinder = new RemoteProfilesFinder(registry, session, this.filter(session));
        final Set<ProfileDescription> remote = remoteProfilesFinder.find();
        final ProfileMatcher matcher = new ChecksumProfileMatcher(remote);
        // Iterate over every installed profile and find match in repository
        installed.forEach(local -> {
            // Check for matching remote checksum and download profile if this version is not equal to latest
            final Optional<ProfileDescription> match = matcher.compare(local);
            if(match.isPresent()) {
                // Found matching checksum for profile in remote list which is not marked as latest version
                log.warn("Override {} with latest profile verison {}", local, match);
                // Remove previous version
                local.getProfile().ifPresent(registry::unregister);
                // Register updated profile by copying temporary file to application support
                match.get().getFile().ifPresent(value -> {
                    final Local copy = registry.register(value);
                    if(null != copy) {
                        final LocalProfileDescription d = new LocalProfileDescription(registry, copy);
                        if(log.isDebugEnabled()) {
                            log.debug("Add synched profile {}", d);
                        }
                        returned.add(d);
                        visitor.visit(d);
                    }
                });
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Add local only profile {}", local);
                }
                returned.add(local);
                visitor.visit(local);
            }
        });
        // Iterate over all fetched profiles and when not installed
        remote.forEach(description -> {
            if(description.isLatest()) {
                // Check if not already added previously when syncing with local list
                if(!returned.contains(description)) {
                    if(log.isDebugEnabled()) {
                        log.debug("Add remote profile {}", description);
                    }
                    returned.add(description);
                    visitor.visit(description);
                }
            }
        });
        localProfilesFinder.cleanup();
        remoteProfilesFinder.cleanup();
        return returned;
    }

    protected CompareFilter filter(final Session<?> session) {
        return new CompareFilter(new DisabledDownloadSymlinkResolver(), session, new DisabledProgressListener());
    }
}
