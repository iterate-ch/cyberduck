package ch.cyberduck.core.updater;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Controller;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.DefaultMainAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public abstract class AbstractPeriodicUpdateChecker implements PeriodicUpdateChecker {
    private static final Logger log = LogManager.getLogger(AbstractPeriodicUpdateChecker.class.getName());

    private final Controller controller;
    private final Duration delay;
    private final Timer timer = new Timer("updater", true);
    private final Preferences preferences = PreferencesFactory.get();

    /**
     * Defaults to 24 hours
     */
    public AbstractPeriodicUpdateChecker(final Controller controller) {
        this(controller, Duration.ofSeconds(PreferencesFactory.get().getLong("update.check.interval")));
    }

    public AbstractPeriodicUpdateChecker(final Controller controller, final Duration delay) {
        this.controller = controller;
        this.delay = delay;
    }

    @Override
    public void unregister() {
        timer.cancel();
    }

    @Override
    public Duration register() {
        log.info(String.format("Register update checker hook after %s", delay));
        try {
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Check for new updates after %s", delay));
                    }
                    preferences.setProperty("update.check.timestamp", System.currentTimeMillis());
                    controller.invoke(new DefaultMainAction() {
                        @Override
                        public void run() {
                            check(true);
                        }
                    });
                }
            }, delay.toMillis(), delay.toMillis());
            return delay;
        }
        catch(IllegalStateException e) {
            log.warn(String.format("Failure scheduling timer. %s", e.getMessage()));
            return Duration.ZERO;
        }
    }

    @Override
    public boolean hasUpdatePrivileges() {
        return true;
    }

    @Override
    public boolean isUpdateInProgress(final Object item) {
        return false;
    }

    protected String getFeedUrl() {
        final StringBuilder url = new StringBuilder(preferences.getProperty(String.format("update.feed.%s", preferences.getProperty("update.feed"))));
        final UpdateCheckerArguments arguments = UpdateCheckerArgumentsFactory.get();
        final Map<String, String> set = arguments.build();
        if(!set.isEmpty()) {
            url.append("?");
        }
        for(Map.Entry<String, String> arg : set.entrySet()) {
            if(!url.toString().endsWith("?")) {
                url.append("&");
            }
            url.append(URIEncoder.encode(arg.getKey())).append("=").append(URIEncoder.encode(arg.getValue()));
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Setting update feed to %s", url));
        }
        return url.toString();
    }
}
