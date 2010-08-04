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

import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.SystemConfigurationProxy;
import ch.cyberduck.core.SystemConfigurationReachability;
import ch.cyberduck.core.aquaticprime.NativeLicense;
import ch.cyberduck.ui.cocoa.application.NSApplication;
import ch.cyberduck.ui.cocoa.foundation.NSAutoreleasePool;
import ch.cyberduck.ui.cocoa.foundation.NSGarbageCollector;
import ch.cyberduck.ui.cocoa.i18n.BundleLocale;
import ch.cyberduck.ui.cocoa.model.FinderLocal;
import ch.cyberduck.ui.cocoa.model.OutlinePathReference;
import ch.cyberduck.ui.cocoa.quicklook.DeprecatedQuickLook;
import ch.cyberduck.ui.cocoa.quicklook.QuartzQuickLook;
import ch.cyberduck.ui.cocoa.serializer.*;
import ch.cyberduck.ui.growl.GrowlNative;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public class MainApplication {
    private static Logger log = Logger.getLogger(MainApplication.class);

    /**
     * @param arguments
     */
    public static void main(String[] arguments) {
        final NSAutoreleasePool pool = NSAutoreleasePool.push();

        try {
            // This method also makes a connection to the window server and completes other initialization.
            // Your program should invoke this method as one of the first statements in main();
            // The NSApplication class sets up autorelease pools (instances of the NSAutoreleasePool class)
            // during initialization and inside the event loop—specifically, within its initialization
            // (or sharedApplication) and run methods.
            final NSApplication app = NSApplication.sharedApplication();

            /**
             * Register factory implementations.
             */
            {
                FinderLocal.register();
                UserDefaultsPreferences.register();
                BundleLocale.register();
                GrowlNative.registerImpl();
                NativeLicense.register();

                PlistDeserializer.register();
                PlistSerializer.register();

                HostPlistReader.register();
                TransferPlistReader.register();
                OutlinePathReference.register();

                PlistWriter.register();

                Keychain.register();
                SystemConfigurationProxy.register();
                SystemConfigurationReachability.register();
                UserDefaultsDateFormatter.register();

                DeprecatedQuickLook.register();
                QuartzQuickLook.register();

                PromptLoginController.register();
                AlertHostKeyController.register();
            }

            final Logger root = Logger.getRootLogger();
            root.setLevel(Level.toLevel(Preferences.instance().getProperty("logging")));

            if(log.isInfoEnabled()) {
                log.info("Default garbage collector for the current process:" + NSGarbageCollector.defaultCollector());
                log.info("Encoding " + System.getProperty("file.encoding"));
            }

            final MainController c = new MainController();

            // Must implement NSApplicationDelegate protocol
            app.setDelegate(c.id());

            // Starts the main event loop. The loop continues until a stop: or terminate: message is
            // received. Upon each iteration through the loop, the next available event
            // from the window server is stored and then dispatched by sending it to NSApp using sendEvent:.
            // The global application object uses autorelease pools in its run method.
            app.run();
        }
        finally {
            pool.drain();
        }
    }
}
