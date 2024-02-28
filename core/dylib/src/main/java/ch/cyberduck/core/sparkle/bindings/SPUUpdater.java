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

import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;

import org.rococoa.ID;
import org.rococoa.ObjCClass;
import org.rococoa.cocoa.foundation.NSError;

/**
 * The main API in Sparkle for controlling the update mechanism. This class is used to configure the update
 * parameters as well as manually and automatically schedule and control checks for updates.
 */
public abstract class SPUUpdater extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("SPUUpdater", _Class.class);

    /**
     * @param userDriver The user driver that Sparkle uses for user update interaction.
     * @param delegate   SPUUpdaterDelegate
     */
    public static SPUUpdater create(final SPUUserDriver userDriver, final ID delegate) {
        return CLASS.alloc().initWithHostBundle_applicationBundle_userDriver_delegate(
                NSBundle.mainBundle(), NSBundle.mainBundle(), userDriver, delegate);
    }

    public interface _Class extends ObjCClass {
        SPUUpdater alloc();
    }

    /**
     * This creates an updater, but to start it and schedule update checks -startUpdater: needs to be invoked first.
     *
     * @param hostBundle        The bundle that should be targeted for updating.
     * @param applicationBundle The application bundle that should be waited for termination and relaunched (unless overridden). Usually this can be the same as hostBundle. This may differ when updating a plug-in or other non-application bundle.
     * @param userDriver        The user driver that Sparkle uses for user update interaction.
     * @param delegate          SPUUpdaterDelegate. The delegate for SPUUpdater.
     * @return Initializes a new SPUUpdater instance
     */
    public abstract SPUUpdater initWithHostBundle_applicationBundle_userDriver_delegate(
            NSBundle hostBundle, NSBundle applicationBundle, SPUUserDriver userDriver, ID delegate);

    /**
     * Starts the updater. This method first checks if Sparkle is configured properly. A valid feed URL should be set before this method is invoked.
     *
     * @param error The error that is populated if this method fails. Pass NULL if not interested in the error information.
     * @return YES if the updater started otherwise NO with a populated error
     */
    public abstract boolean startUpdater(final NSError error);

    /**
     * Checks for updates, and displays progress while doing so if needed.
     * <p>
     * This is meant for users initiating a new update check or checking the current update progress.
     * <p>
     * If an update hasn’t started, the user may be shown that a new check for updates is occurring. If an
     * update has already been downloaded or begun installing from a previous session, the user may be
     * presented to install that update. If the user is already being presented with an update, that update
     * will be shown to the user in active focus.
     * <p>
     * This will find updates that the user has previously opted into skipping.
     */
    public abstract void checkForUpdates();

    /**
     * Checks for updates, but does not display any UI unless an update is found.
     * <p>
     * This is meant for programmatically initiating a check for updates. That is,
     * it will display no UI unless it actually finds an update, in which case it
     * proceeds as usual.
     * <p>
     * If the fully automated updating is turned on, however, this will invoke that
     * behavior, and if an update is found, it will be downloaded and prepped for
     * installation.
     */
    public abstract void checkForUpdatesInBackground();

    /**
     * Begins a “probing” check for updates which will not actually offer to update to that version.
     * <p>
     * However, the delegate methods -[SPUUpdaterDelegate updater:didFindValidUpdate:]
     * and -[SPUUpdaterDelegate updaterDidNotFindUpdate:] will be called, so you can use that information in your UI.
     */
    public abstract void checkForUpdateInformation();

    /**
     * The user agent used when checking for updates.
     *
     * @param userAgentString
     */
    public abstract void setUserAgentString(String userAgentString);

    /**
     * Setting this property will persist in the host bundle’s user defaults.
     *
     * @param sendsSystemProfile A property indicating whether the user’s system profile information is sent when checking for updates.
     */
    public abstract void setSendsSystemProfile(boolean sendsSystemProfile);

    /**
     * By default, Sparkle asks users on second launch for permission if they want automatic update
     * checks enabled and sets this property based on their response. If SUEnableAutomaticChecks is set
     * in the Info.plist, this permission request is not performed however.
     *
     * @param automaticallyChecks A property indicating whether to check for updates automatically.
     */
    public abstract void setAutomaticallyChecksForUpdates(boolean automaticallyChecks);

    /**
     * @return A property indicating whether to check for updates automatically.
     */
    public abstract boolean automaticallyChecksForUpdates();

    /**
     * By default, updates are not automatically downloaded.
     *
     * @param automaticallyDownloadsUpdates Setting this property will persist in the host bundle’s user defaults
     */
    public abstract void setAutomaticallyDownloadsUpdates(boolean automaticallyDownloadsUpdates);

    /**
     * @return A property indicating whether updates can be automatically downloaded in the background.
     */
    public abstract boolean automaticallyDownloadsUpdates();

    /**
     * A property indicating whether an update session is in progress.
     * <p>
     * An update session is in progress when the appcast is being downloaded, an update is being downloaded, an
     * update is being shown, update permission is being requested, or the installer is being started.
     *
     * @return An active session is when Sparkle’s fired scheduler is running.
     */
    public abstract boolean sessionInProgress();

    /**
     * An update check can be made by the user when an update session isn’t in progress, or when an update or its
     * progress is being shown to the user. A user cannot check for updates when data (such as the feed or an
     * update) is still being downloaded automatically in the background.
     *
     * @return A property indicating whether updates can be checked by the user.
     */
    public abstract boolean canCheckForUpdates();

    /**
     * Clears any feed URL from the host bundle’s user defaults that was set via -setFeedURL:
     *
     * @return A previously set feed URL in the host bundle’s user defaults, if available, otherwise this returns nil
     */
    public abstract NSURL clearFeedURLFromUserDefaults();
}
