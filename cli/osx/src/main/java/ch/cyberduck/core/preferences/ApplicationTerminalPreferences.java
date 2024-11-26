/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.core.preferences;

import ch.cyberduck.cli.TerminalPreferences;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.KeychainPasswordStore;
import ch.cyberduck.core.diagnostics.SystemConfigurationReachability;
import ch.cyberduck.core.editor.FSEventWatchEditorFactory;
import ch.cyberduck.core.i18n.BundleRegexLocale;
import ch.cyberduck.core.local.DisabledFilesystemBookmarkResolver;
import ch.cyberduck.core.local.FileManagerWorkingDirectoryFinder;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.FinderProgressIconService;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.LaunchServicesQuarantineService;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.local.WorkspaceBrowserLauncher;
import ch.cyberduck.core.local.WorkspaceSymlinkFeature;
import ch.cyberduck.core.proxy.SystemConfigurationProxy;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;

public class ApplicationTerminalPreferences extends TerminalPreferences {

    public ApplicationTerminalPreferences() {
        super(new MemoryPreferences());
    }

    @Override
    protected void setFactories() {
        super.setFactories();

        this.setDefault("factory.supportdirectoryfinder.class", SecurityApplicationGroupSupportDirectoryFinder.class.getName());
        this.setDefault("factory.localsupportdirectoryfinder.class", SecurityApplicationGroupSupportDirectoryFinder.class.getName());
        this.setDefault("factory.applicationresourcesfinder.class", BundleApplicationResourcesFinder.class.getName());
        if(Factory.Platform.osversion.matches("(10|11|12)\\..*")) {
            this.setDefault("factory.applicationloginregistry.class", SharedFileListApplicationLoginRegistry.class.getName());
        }
        else {
            this.setDefault("factory.applicationloginregistry.class", SMAppServiceApplicationLoginRegistry.class.getName());
        }
        this.setDefault("factory.locale.class", BundleRegexLocale.class.getName());
        this.setDefault("factory.editorfactory.class", FSEventWatchEditorFactory.class.getName());
        this.setDefault("factory.applicationlauncher.class", WorkspaceApplicationLauncher.class.getName());
        this.setDefault("factory.applicationfinder.class", LaunchServicesApplicationFinder.class.getName());
        this.setDefault("factory.local.class", FinderLocal.class.getName());
        this.setDefault("factory.autorelease.class", AutoreleaseActionOperationBatcher.class.getName());
        this.setDefault("factory.passwordstore.class", KeychainPasswordStore.class.getName());
        this.setDefault("factory.proxy.class", SystemConfigurationProxy.class.getName());
        this.setDefault("factory.sleeppreventer.class", IOKitSleepPreventer.class.getName());
        this.setDefault("factory.reachability.class", SystemConfigurationReachability.class.getName());
        this.setDefault("factory.quarantine.class", LaunchServicesQuarantineService.class.getName());
        this.setDefault("factory.iconservice.class", FinderProgressIconService.class.getName());
        this.setDefault("factory.filedescriptor.class", LaunchServicesFileDescriptor.class.getName());
        this.setDefault("factory.workingdirectory.class", FileManagerWorkingDirectoryFinder.class.getName());
        this.setDefault("factory.symlink.class", WorkspaceSymlinkFeature.class.getName());
        this.setDefault("factory.bookmarkresolver.class", DisabledFilesystemBookmarkResolver.class.getName());
        this.setDefault("factory.browserlauncher.class", WorkspaceBrowserLauncher.class.getName());
    }

    @Override
    protected void setDefaults() {
        super.setDefaults();

        this.setDefault("connection.ssl.keystore.type", "KeychainStore");
        this.setDefault("connection.ssl.keystore.provider", "Apple");

        this.setDefault("keychain.secure", String.valueOf(true));
    }
}
