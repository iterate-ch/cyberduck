package ch.cyberduck.core.profiles;/*
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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;
import java.util.stream.Stream;

public class PeriodicProfilesUpdater implements ProfilesUpdater {
    private static final Logger log = Logger.getLogger(PeriodicProfilesUpdater.class.getName());

    private final Controller controller;
    private final ProtocolFactory protocols;
    private final Duration delay;
    private final Timer timer = new Timer("profiles", true);
    private final Local directory;

    public PeriodicProfilesUpdater(final Controller controller) {
        this(controller, ProtocolFactory.get(), LocalFactory.get(SupportDirectoryFinderFactory.get().find(),
            PreferencesFactory.get().getProperty("profiles.folder.name")), Duration.ofSeconds(PreferencesFactory.get().getLong("update.check.interval")));
    }

    public PeriodicProfilesUpdater(final Controller controller, final ProtocolFactory protocols, final Local directory, final Duration delay) {
        this.controller = controller;
        this.protocols = protocols;
        this.delay = delay;
        this.directory = directory;
    }

    @Override
    public void unregister() {
        timer.cancel();
    }

    @Override
    public void register() {
        log.info(String.format("Register profiles checker hook after %s", delay));
        try {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Check for new profiles after %s", delay));
                    }
                    try {
                        PeriodicProfilesUpdater.this.synchronize();
                    }
                    catch(BackgroundException e) {
                        log.warn(String.format("Failure %s refreshing profiles", e));
                    }
                }
            }, 0L, delay.toMillis());
        }
        catch(IllegalStateException e) {
            log.warn(String.format("Failure scheduling timer. %s", e.getMessage()));
        }
    }

    public Future<Stream<ProfileDescription>> synchronize() throws BackgroundException {
        return controller.background(new WorkerBackgroundAction<>(controller, SessionPoolFactory.create(controller,
            HostParser.parse(PreferencesFactory.get().getProperty("profiles.discovery.updater.url")).withCredentials(
                new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name")))), new SynchronizeWorker()));
    }

    private final class SynchronizeWorker extends Worker<Stream<ProfileDescription>> {
        @Override
        public Stream<ProfileDescription> run(final Session<?> session) throws BackgroundException {
            // Find all locally installed profiles
            final Stream<ProfileDescription> installed = new LocalProfilesFinder(new ProfilePlistReader(protocols), directory).find();
            // Find all profiles from repository
            final Stream<ProfileDescription> repository = new RemoteProfilesFinder(new ProfilePlistReader(protocols), session).find();
            final ProfileMatcher matcher = new ChecksumProfileMatcher(repository);
            // Iterate over every installed profile and find match in repository
            installed.forEach(description -> {
                final Optional<Profile> optional = matcher.compare(description);
                if(optional.isPresent()) {
                    // Optional returned if matching profile with later version in repository found
                    final Profile profile = optional.get();
                    log.warn(String.format("Override %s with latest profile verison %s", description.getName(), profile));
                    // Override in registry taking name from existing file to override
                    protocols.register(profile, description.getName());
                }
            });
            return repository;
        }
    }
}
