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
import ch.cyberduck.core.sparkle.bindings.SPUUserUpdateState;
import ch.cyberduck.core.sparkle.bindings.SUAppcastItem;
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
        updater.setAutomaticallyDownloadsUpdates(preferences.getBoolean("update.check.auto"));
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
                log.debug("Check for update in background");
                updater.checkForUpdatesInBackground();
            }
            else {
                log.debug("Check for update");
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
        private final Logger log = LogManager.getLogger(SparklePeriodicUpdateCheckerDelegate.class);

        @Override
        public boolean supportsGentleScheduledUpdateReminders() {
            return !handlers.isEmpty();
        }

        @Override
        public boolean standardUserDriverShouldHandleShowingScheduledUpdate_andInImmediateFocus(final SUAppcastItem item, final boolean immediateFocus) {
            // If the standard user driver will show the update in immediate focus (e.g. near app launch), then let
            // Sparkle take care of showing the update. Otherwise, we will handle showing any other scheduled updates
            if(!immediateFocus) {
                return !handlers.isEmpty();
            }
            return true;
        }

        @Override
        public void standardUserDriverWillHandleShowingUpdate_forUpdate_state(final boolean handleShowingUpdate, final SUAppcastItem item, final SPUUserUpdateState state) {
            if(!handleShowingUpdate) {
                for(Handler handler : handlers) {
                    log.debug("Notify handler {} with update {}", handler, item);
                    if(handler.handle(new Update(item.versionString(), item.displayVersionString()))) {
                        break;
                    }
                }
            }
        }

        @Override
        public void updater_didFindValidUpdate(final SPUUpdater updater, final SUAppcastItem item) {
            if(updater.automaticallyDownloadsUpdates()) {
                for(Handler handler : handlers) {
                    log.debug("Notify handler {} with update {}", handler, item);
                    if(handler.handle(new Update(item.versionString(), item.displayVersionString()))) {
                        break;
                    }
                }
            }
        }

        @Override
        public void updaterWillRelaunchApplication(final SPUUpdater updater) {
            for(Handler handler : handlers) {
                log.debug("Notify handler {} for application relaunch", handler);
                handler.quit();
            }
        }

        @Override
        public String feedURLStringForUpdater(final ID updater) {
            return SparklePeriodicUpdateChecker.this.getFeedUrl();
        }
    }
}
