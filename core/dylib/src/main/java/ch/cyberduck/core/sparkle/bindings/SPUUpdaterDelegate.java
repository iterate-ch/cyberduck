package ch.cyberduck.core.sparkle.bindings;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ID;
import org.rococoa.cocoa.foundation.NSError;

/**
 * Provides delegation methods to control the behavior of an SPUUpdater object.
 */
public interface SPUUpdaterDelegate {
    Logger log = LogManager.getLogger(SPUUpdaterDelegate.class);

    /**
     * Returns a custom appcast URL used for checking for new updates.
     * <p>
     * Override this to dynamically specify the feed URL.
     *
     * @param updater The updater instance.
     * @return An appcast feed URL to check for new updates in, or nil for the default behavior and if you
     * don’t want to be delegated this task.
     */
    String feedURLStringForUpdater(ID updater);

    /**
     * Returns whether the application should be relaunched at all.
     * <p>
     * Some apps cannot be relaunched under certain circumstances. This method can be used to explicitly prevent a relaunch.
     *
     * @param updater The updater instance.
     * @return YES if the updater should be relaunched, otherwise NO if it shouldn’t.
     */
    default boolean updaterShouldRelaunchApplication(SPUUpdater updater) {
        return true;
    }

    /**
     * Called immediately before relaunching.
     *
     * @param updater The updater instance.
     */
    default void updaterWillRelaunchApplication(SPUUpdater updater) {
        if(log.isDebugEnabled()) {
            log.debug("Will relaunch application");
        }
    }

    /**
     * Returns whether Sparkle should prompt the user about checking for new updates automatically.
     *
     * @param updater The updater instance.
     * @return YES if the updater should prompt for permission to check for new updates automatically, otherwise NO
     */
    default boolean updaterShouldPromptForPermissionToCheckForUpdates(SPUUpdater updater) {
        return false;
    }

    /**
     * Called when a new valid update is found by the update driver.
     *
     * @param updater The updater instance.
     * @param item    The appcast item corresponding to the update that is proposed to be installed.
     */
    default void updater_didFindValidUpdate(SPUUpdater updater, SUAppcastItem item) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Did find update %s", item));
        }
    }

    /**
     * Called when a valid new update is not found.
     *
     * @param updater The updater instance.
     */
    default void updaterDidNotFindUpdate(SPUUpdater updater) {
        if(log.isDebugEnabled()) {
            log.debug("No update found");
        }
    }

    /**
     * Called immediately after successful download of the specified update.
     *
     * @param updater The SUUpdater instance.
     * @param item    The appcast item corresponding to the update that has been downloaded.
     */
    default void updater_didDownloadUpdate(SPUUpdater updater, SUAppcastItem item) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Did download update %s", item));
        }
    }

    /**
     * Called immediately before installing the specified update.
     *
     * @param updater The updater instance.
     * @param item    The appcast item corresponding to the update that is proposed to be installed.
     */
    default void updater_willInstallUpdate(SPUUpdater updater, SUAppcastItem item) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Will install update %s", item));
        }
    }

    /**
     * Called when the user cancels an update while it is being downloaded.
     *
     * @param updater The updater instance.
     */
    default void userDidCancelDownload(SPUUpdater updater) {
        if(log.isDebugEnabled()) {
            log.debug("Did cancel update");
        }
    }

    /**
     * Called after the update driver aborts due to an error.
     * <p>
     * The update driver runs when checking for updates. This delegate method is called an error occurs during this process.
     *
     * @param updater The updater instance.
     * @param error   The error that caused the update driver to abort.
     */
    default void updater_didAbortWithError(SPUUpdater updater, NSError error) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Did abort with error %s", error));
        }
    }
}
