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

import ch.cyberduck.core.ApplescriptTerminalService;
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.NSObjectPathReference;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.RendezvousResponder;
import ch.cyberduck.core.SystemConfigurationProxy;
import ch.cyberduck.core.SystemConfigurationReachability;
import ch.cyberduck.core.aquaticprime.Donation;
import ch.cyberduck.core.aquaticprime.Receipt;
import ch.cyberduck.core.editor.MultipleEditorFactory;
import ch.cyberduck.core.i18n.BundleLocale;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.LaunchServicesQuarantineService;
import ch.cyberduck.core.local.TemporaryFileService;
import ch.cyberduck.core.local.WorkspaceApplicationBadgeLabeler;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.local.WorkspaceBrowserLauncher;
import ch.cyberduck.core.local.WorkspaceIconService;
import ch.cyberduck.core.local.WorkspaceRevealService;
import ch.cyberduck.core.local.WorkspaceSymlinkFeature;
import ch.cyberduck.core.local.WorkspaceTrashFeature;
import ch.cyberduck.core.serializer.impl.HostPlistReader;
import ch.cyberduck.core.serializer.impl.PlistDeserializer;
import ch.cyberduck.core.serializer.impl.PlistSerializer;
import ch.cyberduck.core.serializer.impl.PlistWriter;
import ch.cyberduck.core.serializer.impl.ProfilePlistReader;
import ch.cyberduck.core.serializer.impl.TransferPlistReader;
import ch.cyberduck.core.sparkle.Sandbox;
import ch.cyberduck.core.sparkle.Updater;
import ch.cyberduck.core.threading.ActionOperationBatcher;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.core.urlhandler.LaunchServicesSchemeHandler;
import ch.cyberduck.ui.cocoa.application.NSApplication;
import ch.cyberduck.ui.cocoa.quicklook.QuartzQuickLook;
import ch.cyberduck.ui.cocoa.threading.AlertTransferErrorCallback;
import ch.cyberduck.ui.growl.NotificationCenter;
import ch.cyberduck.ui.resources.NSImageIconCache;

import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public final class MainApplication {
    private static Logger log = Logger.getLogger(MainApplication.class);

    private MainApplication() {
        //
    }

    public static void main(String[] arguments) {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                // Swallow the exception
                log.error(String.format("Thread %s has thrown uncaught exception:%s",
                        t.getName(), e.getMessage()), e);
            }
        });
        final ActionOperationBatcher autorelease = new AutoreleaseActionOperationBatcher();
        try {
            // This method also makes a connection to the window server and completes other initialization.
            // Your program should invoke this method as one of the first statements in main();
            // The NSApplication class sets up autorelease pools (instances of the NSAutoreleasePool class)
            // during initialization and inside the event loop—specifically, within its initialization
            // (or sharedApplication) and run methods.
            final NSApplication app = NSApplication.sharedApplication();

            // Register factory implementations.
            {
                AutoreleaseActionOperationBatcher.register();
                FinderLocal.register();
                UserDefaultsPreferences.register();
                BundleLocale.register();
                NSImageIconCache.register();
                NotificationCenter.register();
                if(null == Updater.getFeed()) {
                    Receipt.register();
                }
                else {
                    Donation.register();
                }

                PlistDeserializer.register();
                PlistSerializer.register();

                HostPlistReader.register();
                TransferPlistReader.register();
                ProfilePlistReader.register();
                NSObjectPathReference.register();

                PlistWriter.register();

                Keychain.register();
                SystemConfigurationProxy.register();
                SystemConfigurationReachability.register();
                UserDefaultsDateFormatter.register();
                LaunchServicesApplicationFinder.register();
                LaunchServicesFileDescriptor.register();
                LaunchServicesQuarantineService.register();
                LaunchServicesSchemeHandler.register();
                WorkspaceIconService.register();
                WorkspaceApplicationLauncher.register();
                WorkspaceBrowserLauncher.register();
                WorkspaceRevealService.register();
                WorkspaceApplicationBadgeLabeler.register();
                WorkspaceSymlinkFeature.register();
                WorkspaceTrashFeature.register();
                IOKitSleepPreventer.register();
                TemporaryFileService.register();
                MultipleEditorFactory.register();
                QuartzQuickLook.register();
                PromptLoginController.register();
                AlertHostKeyController.register();
                AlertTransferPromptControllerFactory.register();
                AlertTransferErrorCallback.register();
                RendezvousResponder.register();
                ProtocolFactory.register();
                if(!Sandbox.get().isSandboxed()) {
                    // Disabled in App Store
                    ApplescriptTerminalService.register();
                }
            }

            if(log.isInfoEnabled()) {
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
            autorelease.operate();
        }
    }
}
