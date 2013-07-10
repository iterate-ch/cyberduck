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

import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferCollection;
import ch.cyberduck.ui.cocoa.application.NSAlert;
import ch.cyberduck.ui.cocoa.application.NSApplication;

import org.rococoa.cocoa.foundation.NSUInteger;

/**
 * @version $Id$
 */
public class TransferControllerFactory {

    private static TransferController shared = null;

    private TransferControllerFactory() {
        //
    }

    public static TransferController get() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == shared) {
                shared = new TransferController();
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
        Preferences.instance().setProperty("queue.openByDefault", shared.window().isVisible());
        if(TransferCollection.defaultCollection().numberOfRunningTransfers() > 0) {
            final NSAlert alert = NSAlert.alert(Locale.localizedString("Transfer in progress"), //title
                    Locale.localizedString("There are files currently being transferred. Quit anyway?"), // message
                    Locale.localizedString("Quit"), // defaultbutton
                    Locale.localizedString("Cancel"), //alternative button
                    null //other button
            );
            shared.alert(alert, new SheetCallback() {
                @Override
                public void callback(int returncode) {
                    if(returncode == DEFAULT_OPTION) { //Quit
                        for(Transfer transfer : TransferCollection.defaultCollection()) {
                            if(transfer.isRunning()) {
                                transfer.cancel();
                            }
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
