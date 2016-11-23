package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.binding.DisabledSheetCallback;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.TransferCollection;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.BackgroundAction;
import ch.cyberduck.core.threading.BackgroundActionRegistry;

import org.rococoa.cocoa.foundation.NSUInteger;

public final class TransferControllerFactory {

    private static TransferController shared = null;

    private TransferControllerFactory() {
        //
    }

    public static TransferController get() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == shared) {
                shared = new TransferController();
                shared.loadBundle();
            }
            return shared;
        }
    }

    /**
     * @param app Singleton
     * @return NSApplication.TerminateLater or NSApplication.TerminateNow depending if there are
     *         running transfers to be checked first
     */
    public static NSUInteger applicationShouldTerminate(final NSApplication app) {
        if(null == shared) {
            return NSApplication.NSTerminateNow;
        }
        //Saving state of transfer window
        PreferencesFactory.get().setProperty("queue.window.open.default", shared.isVisible());
        if(TransferCollection.defaultCollection().numberOfRunningTransfers() > 0) {
            final NSAlert alert = NSAlert.alert(LocaleFactory.localizedString("Transfer in progress"), //title
                    LocaleFactory.localizedString("There are files currently being transferred. Quit anyway?"), // message
                    LocaleFactory.localizedString("Quit"), // defaultbutton
                    LocaleFactory.localizedString("Cancel"), //alternative button
                    null //other button
            );
            shared.alert(alert, new DisabledSheetCallback() {
                @Override
                public void callback(int returncode) {
                    if(returncode == DEFAULT_OPTION) { //Quit
                        final BackgroundActionRegistry registry = shared.getActions();
                        for(BackgroundAction action : registry.toArray(new BackgroundAction[registry.size()])) {
                            action.cancel();
                        }
                        app.replyToApplicationShouldTerminate(true);
                    }
                    if(returncode == CANCEL_OPTION) { //Cancel
                        app.replyToApplicationShouldTerminate(false);
                    }
                }
            });
            return NSApplication.NSTerminateLater; //break
        }
        return NSApplication.NSTerminateNow;
    }
}
