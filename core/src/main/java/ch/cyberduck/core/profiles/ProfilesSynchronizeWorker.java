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

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.worker.Worker;

import java.util.Collections;
import java.util.Set;

/**
 * Merge local set with latest versions from server
 */
public class ProfilesSynchronizeWorker extends Worker<Set<ProfileDescription>> {

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
        return new ProtocolFactoryProfilesSynchronizer(session).sync(
                // Match profiles by ETag and MD5 checksum of profile on disk
                new ChecksumProfileMatcher(), visitor);
    }
}
