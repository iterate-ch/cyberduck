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
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.diagnostics.SystemConfigurationReachability;
import ch.cyberduck.core.editor.FSEventWatchEditorFactory;
import ch.cyberduck.core.i18n.BundleLocale;
import ch.cyberduck.core.io.watchservice.FSEventWatchService;
import ch.cyberduck.core.local.DisabledFilesystemBookmarkResolver;
import ch.cyberduck.core.local.FileManagerWorkingDirectoryFinder;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.LaunchServicesQuarantineService;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.local.WorkspaceBrowserLauncher;
import ch.cyberduck.core.local.WorkspaceIconService;
import ch.cyberduck.core.local.WorkspaceSymlinkFeature;
import ch.cyberduck.core.proxy.SystemConfigurationProxy;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;

public class ApplicationTerminalPreferences extends TerminalPreferences {

    @Override
    protected void setFactories() {
        super.setFactories();

        defaults.put("factory.supportdirectoryfinder.class", SecurityApplicationGroupSupportDirectoryFinder.class.getName());
        defaults.put("factory.applicationresourcesfinder.class", BundleApplicationResourcesFinder.class.getName());
        defaults.put("factory.locale.class", BundleLocale.class.getName());
        defaults.put("factory.editorfactory.class", FSEventWatchEditorFactory.class.getName());
        defaults.put("factory.applicationlauncher.class", WorkspaceApplicationLauncher.class.getName());
        defaults.put("factory.applicationfinder.class", LaunchServicesApplicationFinder.class.getName());
        defaults.put("factory.local.class", FinderLocal.class.getName());
        defaults.put("factory.autorelease.class", AutoreleaseActionOperationBatcher.class.getName());
        defaults.put("factory.passwordstore.class", Keychain.class.getName());
        defaults.put("factory.proxy.class", SystemConfigurationProxy.class.getName());
        defaults.put("factory.sleeppreventer.class", IOKitSleepPreventer.class.getName());
        defaults.put("factory.reachability.class", SystemConfigurationReachability.class.getName());
        defaults.put("factory.quarantine.class", LaunchServicesQuarantineService.class.getName());
        defaults.put("factory.iconservice.class", WorkspaceIconService.class.getName());
        defaults.put("factory.filedescriptor.class", LaunchServicesFileDescriptor.class.getName());
        defaults.put("factory.workingdirectory.class", FileManagerWorkingDirectoryFinder.class.getName());
        defaults.put("factory.symlink.class", WorkspaceSymlinkFeature.class.getName());
        defaults.put("factory.watchservice.class", FSEventWatchService.class.getName());
        defaults.put("factory.bookmarkresolver.class", DisabledFilesystemBookmarkResolver.class.getName());
        defaults.put("factory.browserlauncher.class", WorkspaceBrowserLauncher.class.getName());
    }
}
