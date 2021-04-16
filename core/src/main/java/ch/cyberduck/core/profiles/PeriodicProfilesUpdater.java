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
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.Worker;

import org.apache.log4j.Logger;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

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
                        // Find all locally installed profiles
                        final List<ProfileDescription> installed = new LocalProfilesFinder(directory).find(ProfilesFinder.Visitor.Noop);
                        PeriodicProfilesUpdater.this.synchronize(installed, ProfilesFinder.Visitor.Noop);
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

    public Future<List<ProfileDescription>> synchronize(final List<ProfileDescription> installed,
                                                        final ProfilesFinder.Visitor visitor) throws BackgroundException {
        final SynchronizeWorker worker = new SynchronizeWorker(installed, visitor);
        return controller.background(new WorkerBackgroundAction<>(controller, SessionPoolFactory.create(controller,
            HostParser.parse(PreferencesFactory.get().getProperty("profiles.discovery.updater.url")).withCredentials(
                new Credentials(PreferencesFactory.get().getProperty("connection.login.anon.name")))), worker));
    }

    private final class SynchronizeWorker extends Worker<List<ProfileDescription>> {
        private final List<ProfileDescription> installed;
        private final ProfilesFinder.Visitor visitor;

        public SynchronizeWorker(final List<ProfileDescription> installed, final ProfilesFinder.Visitor visitor) {
            this.installed = installed;
            this.visitor = visitor;
        }

        @Override
        public List<ProfileDescription> run(final Session<?> session) throws BackgroundException {
            // Find all profiles from repository
            final List<ProfileDescription> repository = new RemoteProfilesFinder(session).find(visitor);
            final ProfileMatcher matcher = new ChecksumProfileMatcher(repository);
            // Iterate over every installed profile and find match in repository
            installed.forEach(description -> {
                final Optional<ProfileDescription> optional = matcher.compare(description);
                if(optional.isPresent()) {
                    // Optional returned if matching profile with later version in repository found
                    final Local profile = optional.get().getProfile();
                    log.warn(String.format("Override %s with latest profile verison %s", description, profile));
                    // Override in registry taking name from existing file to override
                    protocols.register(profile);
                }
            });
            return repository;
        }
    }
}
