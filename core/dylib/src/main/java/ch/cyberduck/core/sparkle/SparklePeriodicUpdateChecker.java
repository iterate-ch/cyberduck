package ch.cyberduck.core.sparkle;

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

import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.Controller;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.library.Native;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sparkle.bindings.SPUStandardUserDriver;
import ch.cyberduck.core.sparkle.bindings.SPUStandardUserDriverDelegate;
import ch.cyberduck.core.sparkle.bindings.SPUUpdater;
import ch.cyberduck.core.sparkle.bindings.SPUUpdaterDelegate;
import ch.cyberduck.core.updater.AbstractPeriodicUpdateChecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;

public class SparklePeriodicUpdateChecker extends AbstractPeriodicUpdateChecker {
    private static final Logger log = LogManager.getLogger(SparklePeriodicUpdateChecker.class);

    static {
        Native.load("core");
    }

    private final SPUUpdater updater;

    @Delegate
    private final SparklePeriodicUpdateCheckerDelegate delegate;

    private static final Preferences preferences
            = PreferencesFactory.get();

    public SparklePeriodicUpdateChecker(final Controller controller) {
        super(controller);
        delegate = new SparklePeriodicUpdateCheckerDelegate();
        updater = SPUUpdater.create(SPUStandardUserDriver.create(NSBundle.mainBundle(), delegate.id()), delegate.id());
        updater.clearFeedURLFromUserDefaults();
        // Update checks are scheduled using own timer from super class
        updater.setAutomaticallyChecksForUpdates(false);
        updater.setAutomaticallyDownloadsUpdates(false);
        updater.setUserAgentString(new PreferencesUseragentProvider().get());
        updater.setSendsSystemProfile(false);
        if(!updater.startUpdater(null)) {
            log.error("Failure starting updater");
        }
    }

    @Override
    public void check(boolean background) {
        if(this.hasUpdatePrivileges()) {
            if(background) {
                if(log.isDebugEnabled()) {
                    log.debug("Check for update in background");
                }
                updater.checkForUpdatesInBackground();
            }
            else {
                if(log.isDebugEnabled()) {
                    log.debug("Check for update");
                }
                updater.checkForUpdates();
            }
        }
    }

    @Override
    public boolean hasUpdatePrivileges() {
        return updater.canCheckForUpdates();
    }

    @Override
    public boolean isUpdateInProgress() {
        return updater.sessionInProgress();
    }

    private final class SparklePeriodicUpdateCheckerDelegate extends ProxyController implements SPUUpdaterDelegate, SPUStandardUserDriverDelegate {
        @Override
        public String feedURLStringForUpdater(final ID updater) {
            return SparklePeriodicUpdateChecker.this.getFeedUrl();
        }
    }
}
