package ch.cyberduck.core.preferences;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.core.ApplescriptTerminalService;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.KeychainCertificateStore;
import ch.cyberduck.core.KeychainPasswordStore;
import ch.cyberduck.core.aquaticprime.ReceiptFactory;
import ch.cyberduck.core.diagnostics.SystemConfigurationDiagnostics;
import ch.cyberduck.core.diagnostics.SystemConfigurationReachability;
import ch.cyberduck.core.editor.FSEventWatchEditorFactory;
import ch.cyberduck.core.i18n.BundleRegexLocale;
import ch.cyberduck.core.io.watchservice.FSEventWatchService;
import ch.cyberduck.core.local.DisabledFilesystemBookmarkResolver;
import ch.cyberduck.core.local.FileManagerTrashFeature;
import ch.cyberduck.core.local.FileManagerWorkingDirectoryFinder;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.LaunchServicesQuarantineService;
import ch.cyberduck.core.local.SecurityScopedFilesystemBookmarkResolver;
import ch.cyberduck.core.local.WorkspaceApplicationBadgeLabeler;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.local.WorkspaceBrowserLauncher;
import ch.cyberduck.core.local.WorkspaceIconService;
import ch.cyberduck.core.local.WorkspaceRevealService;
import ch.cyberduck.core.local.WorkspaceSymlinkFeature;
import ch.cyberduck.core.notification.NotificationCenter;
import ch.cyberduck.core.proxy.SystemConfigurationProxy;
import ch.cyberduck.core.proxy.SystemPreferencesProxyConfiguration;
import ch.cyberduck.core.proxy.SystemSettingsProxyConfiguration;
import ch.cyberduck.core.quicklook.QuartzQuickLook;
import ch.cyberduck.core.resources.NSImageIconCache;
import ch.cyberduck.core.socket.IOKitHardwareAddress;
import ch.cyberduck.core.sparkle.Sandbox;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.core.urlhandler.LaunchServicesSchemeHandler;
import ch.cyberduck.core.urlhandler.WorkspaceSchemeHandler;
import ch.cyberduck.core.webloc.WeblocFileWriter;

public class ApplicationPreferences extends UserDefaultsPreferences {

    @Override
    protected void setFactories() {
        super.setFactories();

        this.setDefault("factory.supportdirectoryfinder.class", MigratingSecurityApplicationGroupSupportDirectoryFinder.class.getName());
        this.setDefault("factory.logdirectoryfinder.class", LibraryLogDirectoryFinder.class.getName());
        this.setDefault("factory.localsupportdirectoryfinder.class", MigratingSecurityApplicationGroupSupportDirectoryFinder.class.getName());
        this.setDefault("factory.applicationresourcesfinder.class", BundleApplicationResourcesFinder.class.getName());
        if(Factory.Platform.osversion.matches("(10|11|12)\\..*")) {
            this.setDefault("factory.applicationloginregistry.class", SharedFileListApplicationLoginRegistry.class.getName());
        }
        else {
            this.setDefault("factory.applicationloginregistry.class", SMAppServiceApplicationLoginRegistry.class.getName());
        }
        this.setDefault("factory.autorelease.class", AutoreleaseActionOperationBatcher.class.getName());
        this.setDefault("factory.local.class", FinderLocal.class.getName());
        this.setDefault("factory.locale.class", BundleRegexLocale.class.getName());
        this.setDefault("factory.passwordstore.class", KeychainPasswordStore.class.getName());
        this.setDefault("factory.certificatestore.class", KeychainCertificateStore.class.getName());
        this.setDefault("factory.proxy.class", SystemConfigurationProxy.class.getName());
        if(Factory.Platform.osversion.matches("(10|11|12)\\..*")) {
            this.setDefault("factory.proxy.configuration.class", SystemPreferencesProxyConfiguration.class.getName());
        }
        else {
            this.setDefault("factory.proxy.configuration.class", SystemSettingsProxyConfiguration.class.getName());
        }
        this.setDefault("factory.sleeppreventer.class", IOKitSleepPreventer.class.getName());
        this.setDefault("factory.reachability.class", SystemConfigurationReachability.class.getName());
        if(Factory.Platform.osversion.matches("(10|11|12)\\..*")) {
            // Disabled on macOS 13 and later
            this.setDefault("factory.reachability.diagnostics.class", SystemConfigurationDiagnostics.class.getName());
        }
        this.setDefault("factory.applicationfinder.class", LaunchServicesApplicationFinder.class.getName());
        this.setDefault("factory.applicationlauncher.class", WorkspaceApplicationLauncher.class.getName());
        this.setDefault("factory.browserlauncher.class", WorkspaceBrowserLauncher.class.getName());
        this.setDefault("factory.reveal.class", WorkspaceRevealService.class.getName());
        this.setDefault("factory.trash.class", FileManagerTrashFeature.class.getName());
        this.setDefault("factory.quarantine.class", LaunchServicesQuarantineService.class.getName());
        this.setDefault("factory.symlink.class", WorkspaceSymlinkFeature.class.getName());
        this.setDefault("factory.terminalservice.class", ApplescriptTerminalService.class.getName());
        this.setDefault("factory.badgelabeler.class", WorkspaceApplicationBadgeLabeler.class.getName());
        this.setDefault("factory.watchservice.class", FSEventWatchService.class.getName());
        this.setDefault("factory.editorfactory.class", FSEventWatchEditorFactory.class.getName());
        this.setDefault("factory.notification.class", NotificationCenter.class.getName());
        this.setDefault("factory.iconservice.class", WorkspaceIconService.class.getName());
        this.setDefault("factory.filedescriptor.class", LaunchServicesFileDescriptor.class.getName());
        if(Factory.Platform.osversion.matches("(10|11)\\..*")) {
            this.setDefault("factory.schemehandler.class", LaunchServicesSchemeHandler.class.getName());
        }
        else {
            // macOS 12 and later
            this.setDefault("factory.schemehandler.class", WorkspaceSchemeHandler.class.getName());
        }
        this.setDefault("factory.iconcache.class", NSImageIconCache.class.getName());
        this.setDefault("factory.workingdirectory.class", FileManagerWorkingDirectoryFinder.class.getName());
        if(Sandbox.get().isSandboxed()) {
            // Only enable security bookmarks for Mac App Store when running in sandboxed environment
            this.setDefault("factory.bookmarkresolver.class", SecurityScopedFilesystemBookmarkResolver.class.getName());
        }
        else {
            this.setDefault("factory.bookmarkresolver.class", DisabledFilesystemBookmarkResolver.class.getName());
        }
        this.setDefault("factory.urlfilewriter.class", WeblocFileWriter.class.getName());
        this.setDefault("factory.quicklook.class", QuartzQuickLook.class.getName());
        this.setDefault("factory.hardwareaddress.class", IOKitHardwareAddress.class.getName());
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();
        if(null != NSBundle.mainBundle().appStoreReceiptURL()) {
            if(null != NSBundle.mainBundle().appStoreReceiptURL().fileReferenceURL()) {
                this.setDefault("factory.licensefactory.class", ReceiptFactory.class.getName());
            }
        }
    }
}
