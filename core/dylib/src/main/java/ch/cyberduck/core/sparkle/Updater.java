package ch.cyberduck.core.sparkle;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.FactoryException;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.rococoa.ID;
import org.rococoa.ObjCClass;

public abstract class Updater extends NSObject {
    private static final _Class CLASS = org.rococoa.Rococoa.createClass("SUUpdater", _Class.class);

    public static final String PROPERTY_FEED_URL = "SUFeedURL";

    public static Updater create() throws FactoryException {
        if(null == getFeed()) {
            throw new FactoryException("Missing SUFeedURL property");
        }
        final Updater updater = CLASS.sharedUpdater();
        updater.setUserAgentString(new PreferencesUseragentProvider().get());
        return updater;
    }

    public interface _Class extends ObjCClass {
        Updater sharedUpdater();
    }

    public static String getFeed() {
        return PreferencesFactory.get().getDefault(PROPERTY_FEED_URL);
    }

    public abstract Updater init();

    /**
     * Explicitly checks for updates and displays a progress dialog while doing so.
     * <p>
     * This method is meant for a main menu item.
     * Connect any menu item to this action in Interface Builder,
     * and Sparkle will check for updates and report back its findings verbosely
     * when it is invoked.
     */
    public abstract void checkForUpdates(ID sender);

    /**
     * Checks for updates, but does not display any UI unless an update is found.
     * <p>
     * This is meant for programmatically initating a check for updates. That is,
     * it will display no UI unless it actually finds an update, in which case it
     * proceeds as usual.
     * <p>
     * If the fully automated updating is turned on, however, this will invoke that
     * behavior, and if an update is found, it will be downloaded and prepped for
     * installation.
     */
    public abstract void checkForUpdatesInBackground();

    public abstract void setUserAgentString(String userAgentString);
}
