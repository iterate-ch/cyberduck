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

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

public class PeriodicProfilesUpdater implements ProfilesUpdater {
    private static final Logger log = LogManager.getLogger(PeriodicProfilesUpdater.class.getName());

    private final Controller controller;
    private final Duration delay;
    private final Timer timer = new Timer("profiles", true);

    public PeriodicProfilesUpdater(final Controller controller) {
        this(controller, Duration.ofSeconds(PreferencesFactory.get().getLong("update.check.interval")));
    }

    public PeriodicProfilesUpdater(final Controller controller, final Duration delay) {
        this.controller = controller;
        this.delay = delay;
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
                        synchronize(ProfilesFinder.Visitor.Noop);
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

    public Future<Set<ProfileDescription>> synchronize(final ProfilesFinder.Visitor visitor) throws BackgroundException {
        return controller.background(new ProfilesWorkerBackgroundAction(controller,
            new ProfilesSynchronizeWorker(visitor)));
    }
}
