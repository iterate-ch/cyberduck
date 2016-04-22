package ch.cyberduck.preferences;

import ch.cyberduck.core.ApplescriptTerminalService;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.IOKitSleepPreventer;
import ch.cyberduck.core.Keychain;
import ch.cyberduck.core.aquaticprime.ReceiptFactory;
import ch.cyberduck.core.bonjour.RendezvousResponder;
import ch.cyberduck.core.diagnostics.SystemConfigurationReachability;
import ch.cyberduck.core.editor.FSEventWatchEditorFactory;
import ch.cyberduck.core.i18n.BundleLocale;
import ch.cyberduck.core.local.DisabledFilesystemBookmarkResolver;
import ch.cyberduck.core.local.FileManagerWorkingDirectoryFinder;
import ch.cyberduck.core.local.FinderLocal;
import ch.cyberduck.core.local.LaunchServicesApplicationFinder;
import ch.cyberduck.core.local.LaunchServicesFileDescriptor;
import ch.cyberduck.core.local.LaunchServicesQuarantineService;
import ch.cyberduck.core.local.NativeLocalTrashFeature;
import ch.cyberduck.core.local.SecurityScopedFilesystemBookmarkResolver;
import ch.cyberduck.core.local.WorkspaceApplicationBadgeLabeler;
import ch.cyberduck.core.local.WorkspaceApplicationLauncher;
import ch.cyberduck.core.local.WorkspaceBrowserLauncher;
import ch.cyberduck.core.local.WorkspaceIconService;
import ch.cyberduck.core.local.WorkspaceRevealService;
import ch.cyberduck.core.local.WorkspaceSymlinkFeature;
import ch.cyberduck.core.notification.NotificationCenter;
import ch.cyberduck.core.preferences.BundleApplicationResourcesFinder;
import ch.cyberduck.core.preferences.SecurityApplicationGroupSupportDirectoryFinder;
import ch.cyberduck.core.preferences.UserDefaultsPreferences;
import ch.cyberduck.core.proxy.SystemConfigurationProxy;
import ch.cyberduck.core.resources.NSImageIconCache;
import ch.cyberduck.core.sparkle.Updater;
import ch.cyberduck.core.threading.AutoreleaseActionOperationBatcher;
import ch.cyberduck.core.threading.DispatchThreadPool;
import ch.cyberduck.core.urlhandler.LaunchServicesSchemeHandler;
import ch.cyberduck.ui.browser.Column;

public class ApplicationPreferences extends UserDefaultsPreferences {

    @Override
    protected void setFactories() {
        super.setFactories();

        defaults.put("factory.supportdirectoryfinder.class", SecurityApplicationGroupSupportDirectoryFinder.class.getName());
        defaults.put("factory.applicationresourcesfinder.class", BundleApplicationResourcesFinder.class.getName());
        defaults.put("factory.autorelease.class", AutoreleaseActionOperationBatcher.class.getName());
        defaults.put("factory.local.class", FinderLocal.class.getName());
        defaults.put("factory.locale.class", BundleLocale.class.getName());
        defaults.put("factory.passwordstore.class", Keychain.class.getName());
        defaults.put("factory.certificatestore.class", Keychain.class.getName());
        defaults.put("factory.proxy.class", SystemConfigurationProxy.class.getName());
        defaults.put("factory.sleeppreventer.class", IOKitSleepPreventer.class.getName());
        defaults.put("factory.reachability.class", SystemConfigurationReachability.class.getName());
        defaults.put("factory.rendezvous.class", RendezvousResponder.class.getName());

        defaults.put("factory.applicationfinder.class", LaunchServicesApplicationFinder.class.getName());
        defaults.put("factory.applicationlauncher.class", WorkspaceApplicationLauncher.class.getName());
        defaults.put("factory.browserlauncher.class", WorkspaceBrowserLauncher.class.getName());
        defaults.put("factory.reveal.class", WorkspaceRevealService.class.getName());
        defaults.put("factory.trash.class", NativeLocalTrashFeature.class.getName());
        defaults.put("factory.quarantine.class", LaunchServicesQuarantineService.class.getName());
        defaults.put("factory.symlink.class", WorkspaceSymlinkFeature.class.getName());
        defaults.put("factory.terminalservice.class", ApplescriptTerminalService.class.getName());
        defaults.put("factory.badgelabeler.class", WorkspaceApplicationBadgeLabeler.class.getName());
        defaults.put("factory.editorfactory.class", FSEventWatchEditorFactory.class.getName());
        if(null == Updater.getFeed()) {
            defaults.put("factory.licensefactory.class", ReceiptFactory.class.getName());
        }
        if(!Factory.Platform.osversion.matches("10\\.(5|6|7).*")) {
            defaults.put("factory.notification.class", NotificationCenter.class.getName());
        }
        defaults.put("factory.iconservice.class", WorkspaceIconService.class.getName());
        defaults.put("factory.filedescriptor.class", LaunchServicesFileDescriptor.class.getName());
        defaults.put("factory.schemehandler.class", LaunchServicesSchemeHandler.class.getName());
        defaults.put("factory.iconcache.class", NSImageIconCache.class.getName());
        defaults.put("factory.workingdirectory.class", FileManagerWorkingDirectoryFinder.class.getName());
        if(null == Updater.getFeed()) {
            // Only enable security bookmarks for Mac App Store when running in sandboxed environment
            defaults.put("factory.bookmarkresolver.class", SecurityScopedFilesystemBookmarkResolver.class.getName());
        }
        else {
            defaults.put("factory.bookmarkresolver.class", DisabledFilesystemBookmarkResolver.class.getName());
        }
        defaults.put("factory.threadpool.class", DispatchThreadPool.class.getName());
    }

    @Override
    protected void setDefaults() {
        // Parent defaults
        super.setDefaults();

        defaults.put(String.format("browser.column.%s", Column.icon.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.icon.name()), String.valueOf(20));
        defaults.put(String.format("browser.column.%s", Column.filename.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.filename.name()), String.valueOf(250));
        defaults.put(String.format("browser.column.%s", Column.kind.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.kind.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.extension.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.extension.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.size.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.size.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.modified.name()), String.valueOf(true));
        defaults.put(String.format("browser.column.%s.width", Column.modified.name()), String.valueOf(150));
        defaults.put(String.format("browser.column.%s", Column.owner.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.owner.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.group.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.group.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.permission.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.permission.name()), String.valueOf(100));
        defaults.put(String.format("browser.column.%s", Column.region.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.region.name()), String.valueOf(80));
        defaults.put(String.format("browser.column.%s", Column.version.name()), String.valueOf(false));
        defaults.put(String.format("browser.column.%s.width", Column.version.name()), String.valueOf(80));

        defaults.put("browser.sort.column", Column.filename.name());
    }
}
