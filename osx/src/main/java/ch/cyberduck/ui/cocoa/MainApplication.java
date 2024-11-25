package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.logging.LoggerPrintStream;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.ui.cocoa.controller.MainController;

import java.util.ServiceLoader;

public final class MainApplication {

    static {
        System.err.close();
        System.setErr(new LoggerPrintStream());
    }

    private MainApplication() {
        //
    }

    public static void main(String... arguments) {
        final ActionOperationBatcher autorelease = new AutoreleaseActionOperationBatcher(1);
        try {
            // This method also makes a connection to the window server and completes other initialization.
            // Your program should invoke this method as one of the first statements in main();
            // The NSApplication class sets up autorelease pools (instances of the NSAutoreleasePool class)
            // during initialization and inside the event loop—specifically, within its initialization
            // (or sharedApplication) and run methods.
            final NSApplication app = NSApplication.sharedApplication();

            // Register factory implementations.
            final Preferences preferences = new ApplicationUserDefaultsPreferences();
            PreferencesFactory.set(preferences);

            final ProtocolFactory protocols = ProtocolFactory.get();
            for(Protocol p : ServiceLoader.load(Protocol.class)) {
                protocols.register(p);
            }
            protocols.load();
            final MainController c = new MainController();
            // Must implement NSApplicationDelegate protocol
            app.setDelegate(c.id());
            // When the Finder launches an app, using a value of NO for flag allows the app to become active if the user waits for it to launch
            app.activateIgnoringOtherApps(false);
            // Starts the main event loop. The loop continues until a stop: or terminate: message is
            // received. Upon each iteration through the loop, the next available event
            // from the window server is stored and then dispatched by sending it to NSApp using sendEvent:.
            // The global application object uses autorelease pools in its run method.
            app.run();
        }
        finally {
            autorelease.operate();
        }
    }
}
