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
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.box.BoxProtocol;
import ch.cyberduck.core.brick.BrickProtocol;
import ch.cyberduck.core.ctera.CteraProtocol;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dropbox.DropboxProtocol;
import ch.cyberduck.core.eue.EueProtocol;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.googledrive.DriveProtocol;
import ch.cyberduck.core.googlestorage.GoogleStorageProtocol;
import ch.cyberduck.core.hubic.HubicProtocol;
import ch.cyberduck.core.irods.IRODSProtocol;
import ch.cyberduck.core.logging.LoggerPrintStream;
import ch.cyberduck.core.manta.MantaProtocol;
import ch.cyberduck.core.nextcloud.NextcloudProtocol;
import ch.cyberduck.core.nio.LocalProtocol;
import ch.cyberduck.core.onedrive.OneDriveProtocol;
import ch.cyberduck.core.onedrive.SharepointProtocol;
import ch.cyberduck.core.onedrive.SharepointSiteProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.owncloud.OwncloudProtocol;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.spectra.SpectraProtocol;
import ch.cyberduck.core.storegate.StoregateProtocol;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.ui.cocoa.controller.MainController;

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
            protocols.register(
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
                    new DropboxProtocol(),
                    new DriveProtocol(),
                    new HubicProtocol(),
                    new OneDriveProtocol(),
                    new SharepointProtocol(),
                    new SharepointSiteProtocol(),
                    new LocalProtocol(),
                    new MantaProtocol(),
                    new SDSProtocol(),
                    new StoregateProtocol(),
                    new BrickProtocol(),
                    new NextcloudProtocol(),
                    new OwncloudProtocol(),
                    new CteraProtocol(),
                    new BoxProtocol(),
                    new EueProtocol()
            );
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
