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
import ch.cyberduck.core.HostParser;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProfileWriterFactory;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SessionPoolFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
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
                        synchronize(new FilenameProfileMatcher(new LocalProfilesFinder(new ProfilePlistReader(protocols), directory).find()));
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

    public Future<Stream<ProfilesFinder.ProfileDescription>> synchronize(final ProfileMatcher comparator) throws BackgroundException {
        return controller.background(new WorkerBackgroundAction<>(controller, SessionPoolFactory.create(controller,
            HostParser.parse(PreferencesFactory.get().getProperty("profiles.discovery.updater.url"))), new Worker<Stream<ProfilesFinder.ProfileDescription>>() {
            @Override
            public Stream<ProfilesFinder.ProfileDescription> run(final Session<?> session) throws BackgroundException {
                // Find all locally installed profiles
                final Stream<ProfilesFinder.ProfileDescription> stream = new RemoteProfilesFinder(new ProfilePlistReader(protocols), session).find();
                stream.forEach(description -> {
                    final Optional<Profile> optional = comparator.compare(description);
                    if(optional.isPresent()) {
                        final Profile profile = optional.get();
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Install updated profile %s", profile));
                        }
                        try {
                            ProfileWriterFactory.get().write(profile, LocalFactory.get(directory, optional.get().getName()));
                        }
                        catch(AccessDeniedException e) {
                            log.warn(String.format("Failure %s writing profile %s", e, profile));
                        }
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Register updated profile %s", profile));
                        }
                        protocols.register(profile);
                    }
                });
                return stream;
            }
        }));
    }
}
