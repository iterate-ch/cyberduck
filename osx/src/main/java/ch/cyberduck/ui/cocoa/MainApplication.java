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
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.googledrive.DriveProtocol;
import ch.cyberduck.core.dropbox.DropboxProtocol;
import ch.cyberduck.core.googlestorage.GoogleStorageProtocol;
import ch.cyberduck.core.irods.IRODSProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.spectra.SpectraProtocol;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;

import org.apache.log4j.Logger;

public final class MainApplication {
    private static Logger log = Logger.getLogger(MainApplication.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
    }

    private MainApplication() {
        //
    }

    public static void main(String... arguments) {
        final ActionOperationBatcher autorelease = new AutoreleaseActionOperationBatcher();
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
            ProtocolFactory.register(
                    new FTPProtocol(),
                    new FTPTLSProtocol(),
                    new SFTPProtocol(),
                    new DAVProtocol(),
                    new DAVSSLProtocol(),
                    new SwiftProtocol(),
                    new S3Protocol(),
                    new GoogleStorageProtocol(),
                    new AzureProtocol(),
                    new IRODSProtocol(),
                    new SpectraProtocol(),
                    new B2Protocol(),
                    new DriveProtocol(),
                    new DropboxProtocol()
            );
            if(log.isInfoEnabled()) {
                log.info(String.format("Running version %s", NSBundle.mainBundle()
                        .objectForInfoDictionaryKey("CFBundleVersion").toString()));
                log.info(String.format("Running Java %s on %s", System
                        .getProperty("java.version"), System.getProperty("os.arch")));
                log.info(String.format("Available localizations:%s", preferences.applicationLocales()));
                log.info(String.format("Native library path:%s", System.getProperty("java.library.path")));
                log.info(String.format("Using default encoding %s", System.getProperty("file.encoding")));
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
            autorelease.operate();
        }
    }
}
