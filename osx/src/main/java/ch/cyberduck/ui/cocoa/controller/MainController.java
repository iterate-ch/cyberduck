package ch.cyberduck.ui.cocoa.controller;

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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.SystemAlertController;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.application.NSWindow;
import ch.cyberduck.binding.application.NSWorkspace;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.application.WindowListener;
import ch.cyberduck.binding.foundation.NSAppleEventDescriptor;
import ch.cyberduck.binding.foundation.NSAppleEventManager;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.core.*;
import ch.cyberduck.core.aquaticprime.DonationKey;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.bonjour.NotificationRendezvousListener;
import ch.cyberduck.core.bonjour.Rendezvous;
import ch.cyberduck.core.bonjour.RendezvousFactory;
import ch.cyberduck.core.ctera.CteraProtocol;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.importer.*;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.oauth.OAuth2TokenListenerRegistry;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;
import ch.cyberduck.core.profiles.PeriodicProfilesUpdater;
import ch.cyberduck.core.profiles.ProfilesUpdater;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.sparkle.MenuItemSparkleUpdateHandler;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.threading.DefaultBackgroundExecutor;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.updater.NotificationServiceUpdateHandler;
import ch.cyberduck.core.updater.PeriodicUpdateChecker;
import ch.cyberduck.core.updater.PeriodicUpdateCheckerFactory;
import ch.cyberduck.core.urlhandler.SchemeHandler;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;
import ch.cyberduck.ui.browser.BrowserColumn;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;
import ch.cyberduck.ui.cocoa.delegate.ArchiveMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.BookmarkMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.CopyURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.EditMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.OpenURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.URLMenuDelegate;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.net.URI;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Predicate;

import com.google.common.util.concurrent.Uninterruptibles;

/**
 * Setting the main menu and implements application delegate methods
 */
public class MainController extends BundleController implements NSApplication.Delegate, NSMenu.Validation {
    private static final Logger log = LogManager.getLogger(MainController.class);

    /**
     * Apple event constants<br> **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:117</i>
     */
    public static final int kInternetEventClass = 1196773964;
    /**
     * Apple event constants<br> **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:118</i>
     */
    public static final int kAEGetURL = 1196773964;
    /**
     * Apple event constants<br> **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:119</i>
     */
    public static final int kAEFetchURL = 1179996748;

    /// 0x2d2d2d2d
    public static final int keyAEResult = 757935405;

    private final Preferences preferences = PreferencesFactory.get();

    private final PeriodicUpdateChecker updater
        = PeriodicUpdateCheckerFactory.get(this);

    private final ProfilesUpdater profiles
        = new PeriodicProfilesUpdater(this);

    private final PathKindDetector detector = new DefaultPathKindDetector();
    /**
     *
     */
    private static final List<BrowserController> browsers
        = new ArrayList<BrowserController>();

    private final NSWorkspace workspace = NSWorkspace.sharedWorkspace();

    /**
     * Display donation reminder dialog
     */
    private boolean displayDonationPrompt = true;

    @Outlet
    private SheetController donationController;
    @Outlet
    private NSMenu applicationMenu;
    @Outlet
    private NSMenu encodingMenu;
    @Outlet
    private NSMenu columnMenu;
    @Outlet
    private NSMenu editMenu;
    @Delegate
    private EditMenuDelegate editMenuDelegate;
    @Outlet
    private NSMenu urlMenu;
    @Delegate
    private URLMenuDelegate urlMenuDelegate;
    @Outlet
    private NSMenu openUrlMenu;
    @Delegate
    private URLMenuDelegate openUrlMenuDelegate;
    @Outlet
    private NSMenu archiveMenu;
    @Delegate
    private ArchiveMenuDelegate archiveMenuDelegate;
    @Outlet
    private NSMenu bookmarkMenu;
    @Delegate
    private BookmarkMenuDelegate bookmarkMenuDelegate;

    /**
     * @param frame Frame autosave name
     */
    public static BrowserController newDocument(final boolean force, final String frame) {
        final List<BrowserController> browsers = getBrowsers();
        if(!force) {
            for(BrowserController controller : browsers) {
                if(controller.isIdle()) {
                    controller.display();
                    return controller;
                }
            }
        }
        final BrowserController controller = new BrowserController();
        controller.addListener(new WindowListener() {
            @Override
            public void windowWillClose() {
                browsers.remove(controller);
            }
        });
        if(StringUtils.isNotBlank(frame)) {
            controller.window().setFrameUsingName(frame);
        }
        controller.display();
        browsers.add(controller);
        return controller;
    }

    /**
     * Makes a unmounted browser window the key window and brings it to the front
     *
     * @return A reference to a browser window
     */
    public static BrowserController newDocument() {
        return newDocument(false);
    }

    public static List<BrowserController> getBrowsers() {
        return browsers;
    }

    /**
     * Browser with key focus
     *
     * @return Null if no browser window is open
     */
    public BrowserController getBrowser() {
        for(BrowserController browser : browsers) {
            if(browser.window().isKeyWindow()) {
                return browser;
            }
        }
        return null;
    }

    /**
     * Makes a unmounted browser window the key window and brings it to the front
     *
     * @param force If true, open a new browser regardless of any unused browser window
     */
    public static BrowserController newDocument(final boolean force) {
        return newDocument(force, null);
    }

    @Override
    protected String getBundleName() {
        return "Main";
    }

    public void setApplicationMenu(NSMenu menu) {
        this.applicationMenu = menu;
        this.updateLicenseMenu();
        this.updateUpdateMenu();
    }

    /**
     * Set name of key in menu item
     */
    protected void updateLicenseMenu() {
        final License key = LicenseFactory.find();
        if(key.isReceipt()) {
            this.applicationMenu.removeItemAtIndex(new NSInteger(5));
            this.applicationMenu.removeItemAtIndex(new NSInteger(4));
        }
        else {
            this.applicationMenu.itemAtIndex(new NSInteger(5)).setAttributedTitle(
                    NSAttributedString.attributedStringWithAttributes(key.getEntitlement(), MENU_HELP_FONT_ATTRIBUTES)
            );
        }
    }

    /**
     * Remove software update menu item if no update feed available
     */
    private void updateUpdateMenu() {
        if(updater.hasUpdatePrivileges()) {
            final NSMenuItem item = this.applicationMenu.itemAtIndex(new NSInteger(1));
            updater.addHandler(new MenuItemSparkleUpdateHandler(item));
            if(preferences.getBoolean("update.check.auto")) {
                updater.addHandler(new NotificationServiceUpdateHandler(updater));
            }
        }
        else {
            this.applicationMenu.removeItemAtIndex(new NSInteger(1));
        }
    }

    public void setEncodingMenu(NSMenu encodingMenu) {
        this.encodingMenu = encodingMenu;
        for(String charset : new DefaultCharsetProvider().availableCharsets()) {
            final NSMenuItem item = this.encodingMenu.addItemWithTitle_action_keyEquivalent(charset, Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
            item.setRepresentedObject(charset);
        }
    }

    public void setColumnMenu(NSMenu columnMenu) {
        this.columnMenu = columnMenu;
        Map<String, String> columns = new HashMap<String, String>();
        columns.put(String.format("browser.column.%s", BrowserColumn.kind.name()), BrowserColumn.kind.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.extension.name()), BrowserColumn.extension.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.size.name()), BrowserColumn.size.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.modified.name()), BrowserColumn.modified.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.owner.name()), BrowserColumn.owner.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.group.name()), BrowserColumn.group.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.permission.name()), BrowserColumn.permission.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.region.name()), BrowserColumn.region.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.version.name()), BrowserColumn.version.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.checksum.name()), BrowserColumn.checksum.toString());
        columns.put(String.format("browser.column.%s", BrowserColumn.storageclass.name()), BrowserColumn.storageclass.toString());
        for(Map.Entry<String, String> entry : columns.entrySet()) {
            NSMenuItem item = this.columnMenu.addItemWithTitle_action_keyEquivalent(entry.getValue(),
                Foundation.selector("columnMenuClicked:"), StringUtils.EMPTY);
            final String identifier = entry.getKey();
            item.setState(preferences.getBoolean(identifier) ? NSCell.NSOnState : NSCell.NSOffState);
            item.setRepresentedObject(identifier);
        }
    }

    @Action
    public void columnMenuClicked(final NSMenuItem sender) {
        final String identifier = sender.representedObject();
        final boolean enabled = !preferences.getBoolean(identifier);
        sender.setState(enabled ? NSCell.NSOnState : NSCell.NSOffState);
        preferences.setProperty(identifier, enabled);
        BrowserController.updateBrowserTableColumns();
    }

    public void setEditMenu(NSMenu editMenu) {
        this.editMenu = editMenu;
        this.editMenuDelegate = new EditMenuDelegate() {
            @Override
            protected Path getEditable() {
                final List<BrowserController> b = MainController.getBrowsers();
                for(BrowserController controller : b) {
                    if(controller.window().isKeyWindow()) {
                        final Path selected = controller.getSelectedPath();
                        if(null == selected) {
                            return null;
                        }
                        if(controller.isEditable(selected)) {
                            return selected;
                        }
                        return null;
                    }
                }
                return null;
            }

            @Override
            protected ID getTarget() {
                return MainController.this.getBrowser().id();
            }
        };
        this.editMenu.setDelegate(editMenuDelegate.id());
    }

    public void setUrlMenu(NSMenu urlMenu) {
        this.urlMenu = urlMenu;
        this.urlMenuDelegate = new CopyURLMenuDelegate() {
            @Override
            protected SessionPool getSession() {
                final List<BrowserController> b = MainController.getBrowsers();
                for(BrowserController controller : b) {
                    if(controller.window().isKeyWindow()) {
                        if(controller.isMounted()) {
                            return controller.getSession();
                        }
                    }
                }
                return null;
            }

            @Override
            protected List<Path> getSelected() {
                final List<BrowserController> b = MainController.getBrowsers();
                for(BrowserController controller : b) {
                    if(controller.window().isKeyWindow()) {
                        final List<Path> selected = controller.getSelectedPaths();
                        if(selected.isEmpty()) {
                            if(controller.isMounted()) {
                                return Collections.singletonList(controller.workdir());
                            }
                        }
                        return selected;
                    }
                }
                return Collections.emptyList();
            }
        };
        this.urlMenu.setDelegate(urlMenuDelegate.id());
    }

    public void setOpenUrlMenu(NSMenu openUrlMenu) {
        this.openUrlMenu = openUrlMenu;
        this.openUrlMenuDelegate = new OpenURLMenuDelegate() {
            @Override
            protected SessionPool getSession() {
                final List<BrowserController> b = MainController.getBrowsers();
                for(BrowserController controller : b) {
                    if(controller.window().isKeyWindow()) {
                        if(controller.isMounted()) {
                            return controller.getSession();
                        }
                    }
                }
                return null;
            }

            @Override
            protected List<Path> getSelected() {
                final List<BrowserController> b = MainController.getBrowsers();
                for(BrowserController controller : b) {
                    if(controller.window().isKeyWindow()) {
                        final List<Path> selected = controller.getSelectedPaths();
                        if(selected.isEmpty()) {
                            if(controller.isMounted()) {
                                return Collections.singletonList(controller.workdir());
                            }
                        }
                        return selected;
                    }
                }
                return Collections.emptyList();
            }
        };
        this.openUrlMenu.setDelegate(openUrlMenuDelegate.id());
    }

    public void setArchiveMenu(NSMenu archiveMenu) {
        this.archiveMenu = archiveMenu;
        this.archiveMenuDelegate = new ArchiveMenuDelegate();
        this.archiveMenu.setDelegate(archiveMenuDelegate.id());
    }

    public void setBookmarkMenu(NSMenu bookmarkMenu) {
        this.bookmarkMenu = bookmarkMenu;
        this.bookmarkMenuDelegate = new BookmarkMenuDelegate();
        this.bookmarkMenu.setDelegate(bookmarkMenuDelegate.id());
    }

    @Action
    public void bugreportMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(
            MessageFormat.format(preferences.getProperty("website.bug"),
                preferences.getProperty("application.version")));
    }

    @Action
    public void helpMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help());
    }

    @Action
    public void licenseMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("website.license"));
    }

    @Action
    public void acknowledgmentsMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("website.acknowledgments"));
    }

    @Action
    public void websiteMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("website.home"));
    }

    @Action
    public void donateMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("website.donate"));
    }

    @Action
    public void aboutMenuClicked(final ID sender) {
        final NSDictionary dict = NSDictionary.dictionaryWithObjectsForKeys(
                NSArray.arrayWithObjects(
                        preferences.getProperty("application.name"),
                        preferences.getProperty("application.version"),
                        preferences.getProperty("application.revision"),
                        preferences.getProperty("application.copyright")),
                NSArray.arrayWithObjects(
                        "ApplicationName",
                        "ApplicationVersion",
                        "Version",
                        "Copyright")
        );
        NSApplication.sharedApplication().orderFrontStandardAboutPanelWithOptions(dict);
    }

    @Action
    public void feedbackMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("mail.feedback")
            + "?subject=" + preferences.getProperty("application.name") + "-" + preferences.getProperty("application.version"));
    }

    @Action
    public void preferencesMenuClicked(final ID sender) {
        final PreferencesController c = PreferencesControllerFactory.instance();
        c.display();
    }

    @Action
    public void newDownloadMenuClicked(final ID sender) {
        this.showTransferQueueClicked(sender);
        final DownloadController c = new DownloadController();
        TransferControllerFactory.get().alert(c);
    }

    @Action
    public void newBrowserMenuClicked(final ID sender) {
        this.openDefaultBookmark(newDocument(true));
    }

    @Action
    public void newWindowForTab(final ID sender) {
        this.openDefaultBookmark(newDocument(true));
    }

    /**
     * Mounts the default bookmark if any
     */
    protected void openDefaultBookmark(final BrowserController controller) {
        String defaultBookmark = preferences.getProperty("browser.open.bookmark.default");
        if(null == defaultBookmark) {
            log.info("No default bookmark configured");
            return; //No default bookmark given
        }
        final Host bookmark = BookmarkCollection.defaultCollection().lookup(defaultBookmark);
        if(null == bookmark) {
            log.info("Default bookmark no more available");
            return;
        }
        for(BrowserController browser : getBrowsers()) {
            if(bookmark.equals(browser.getSession().getHost())) {
                log.debug("Default bookmark already mounted");
                return;
            }
        }
        log.debug("Mounting default bookmark {}", bookmark);
        controller.mount(bookmark);
    }

    @Action
    public void showTransferQueueClicked(final ID sender) {
        final TransferController c = TransferControllerFactory.get();
        c.display();
    }

    @Action
    public void showActivityWindowClicked(final ID sender) {
        final ActivityController c = ActivityControllerFactory.get();
        if(c.isVisible()) {
            c.close();
        }
        else {
            c.display();
        }
    }

    @Override
    public boolean application_openFile(final NSApplication app, final String filename) {
        log.debug("Open file {}", filename);
        final Local f = LocalFactory.get(filename);
        if(f.exists()) {
            if("duck".equals(f.getExtension())) {
                final Host bookmark;
                try {
                    newDocument().mount(HostReaderFactory.get().read(f));
                    return true;
                }
                catch(AccessDeniedException e) {
                    log.error("Failure reading bookmark from {}. {}", f, e.getMessage());
                    return false;
                }
            }
            else if("cyberducklicense".equals(f.getExtension())) {
                final License l = new DonationKey(f);
                if(l.verify()) {
                    try {
                        f.copy(LocalFactory.get(SupportDirectoryFinderFactory.get().find(), f.getName()));
                        final NSAlert alert = NSAlert.alert(
                                l.getEntitlement(),
                            LocaleFactory.localizedString("Thanks for your support! Your contribution helps to further advance development to make Cyberduck even better.", "License")
                                + "\n\n"
                                + LocaleFactory.localizedString("Your donation key has been copied to the Application Support folder.", "License"),
                            LocaleFactory.localizedString("Continue", "License"), //default
                            null, //other
                            null
                        );
                        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                        if(this.alert(new SystemAlertController(alert)) == SheetCallback.DEFAULT_OPTION) {
                            for(BrowserController c : MainController.getBrowsers()) {
                                c.removeDonateWindowTitle();
                            }
                            this.updateLicenseMenu();
                        }
                    }
                    catch(AccessDeniedException e) {
                        log.error(e.getMessage());
                        return false;
                    }
                }
                else {
                    final NSAlert alert = NSAlert.alert(
                        LocaleFactory.localizedString("Not a valid registration key", "License"),
                        LocaleFactory.localizedString("This donation key does not appear to be valid.", "License"),
                        LocaleFactory.localizedString("Continue", "License"), //default
                        null, //other
                        null);
                    alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
                    alert.setShowsHelp(true);
                    alert.setDelegate(new ProxyController() {
                        @Action
                        public boolean alertShowHelp(NSAlert alert) {
                            BrowserLauncherFactory.get().open(ProviderHelpServiceFactory.get().help());
                            return true;
                        }

                    }.id());
                    alert.runModal();
                }
                return true;
            }
            else if("cyberduckprofile".equals(f.getExtension())) {
                try {
                    log.debug("Register profile {}", f);
                    final Local copy = ProtocolFactory.get().register(f);
                    if(copy != null) {
                        final Profile profile = ProfileReaderFactory.get().read(copy);
                        final Host host = new Host(profile, profile.getDefaultHostname(), profile.getDefaultPort());
                        newDocument().addBookmark(host);
                    }
                }
                catch(AccessDeniedException e) {
                    log.error("Failure reading profile from {}. {}", f, e);
                    return false;
                }
            }
            else {
                // Upload file
                this.background(new AbstractBackgroundAction<Void>() {
                    @Override
                    public Void run() {
                        // Wait until bookmarks are loaded
                        Uninterruptibles.awaitUninterruptibly(bookmarksSemaphore);
                        return null;
                    }

                    @Override
                    public void cleanup() {
                        upload(f);
                    }

                    @Override
                    public String getActivity() {
                        return "Open File";
                    }
                });
                return true;
            }
        }
        return false;
    }

    private boolean upload(final Local f) {
        return this.upload(Collections.singletonList(f));
    }

    private boolean upload(final List<Local> files) {
        // Selected bookmark
        Host open = null;
        Path workdir = null;
        for(BrowserController browser : MainController.getBrowsers()) {
            if(browser.isMounted()) {
                open = browser.getSession().getHost();
                workdir = browser.workdir();
                if(1 == MainController.getBrowsers().size()) {
                    // If only one browser window upload to current working directory with no bookmark selection
                    this.upload(open, files, workdir);
                    return true;
                }
                break;
            }
        }
        final BookmarkCollection bookmarks = BookmarkCollection.defaultCollection();
        if(bookmarks.isEmpty()) {
            log.warn("No bookmark for upload");
            return false;
        }
        final NSPopUpButton bookmarksPopup = NSPopUpButton.buttonPullsDown(false);
        bookmarksPopup.setToolTip(LocaleFactory.localizedString("Bookmarks", "Browser"));
        for(Host b : bookmarks) {
            String title = BookmarkNameProvider.toString(b);
            int i = 1;
            while(bookmarksPopup.itemWithTitle(title) != null) {
                title = BookmarkNameProvider.toString(b) + "-" + i;
                i++;
            }
            bookmarksPopup.addItemWithTitle(title);
            bookmarksPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed(b.getProtocol().icon(), 16));
            bookmarksPopup.lastItem().setRepresentedObject(b.getUuid());
            if(b.equals(open)) {
                bookmarksPopup.selectItemAtIndex(bookmarksPopup.indexOfItem(bookmarksPopup.lastItem()));
            }
        }
        if(null == open) {
            int i = 0;
            for(Host bookmark : bookmarks) {
                boolean found = false;
                // Pick the bookmark with the same download location
                for(Local file : files) {
                    if(file.isChild(new DownloadDirectoryFinder().find(bookmark))) {
                        bookmarksPopup.selectItemAtIndex(new NSInteger(i));
                        found = true;
                        break;
                    }
                }
                if(found) {
                    break;
                }
                i++;
            }
        }
        if(-1 == bookmarksPopup.indexOfSelectedItem().intValue()) {
            // No bookmark for current browser found
            bookmarksPopup.selectItemAtIndex(new NSInteger(0));
        }
        final Host mount = open;
        final Path destination = workdir;
        final NSAlert alert = NSAlert.alert("Select Bookmark",
            MessageFormat.format("Upload {0} to the selected bookmark.",
                files.size() == 1 ? files.iterator().next().getName()
                    : MessageFormat.format(LocaleFactory.localizedString("{0} Items"), String.valueOf(files.size()))
            ),
            LocaleFactory.localizedString("Upload", "Transfer"),
            LocaleFactory.localizedString("Cancel"),
            null
        );
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        final AlertController controller = new SystemAlertController(alert) {
            @Override
            public NSView getAccessoryView(final NSAlert alert) {
                return bookmarksPopup;
            }

            @Override
            public void callback(int returncode) {
                if(DEFAULT_OPTION == returncode) {
                    final String selected = bookmarksPopup.selectedItem().representedObject();
                    final Host bookmark = bookmarks.lookup(selected);
                    if(bookmark.equals(mount)) {
                        // Use current working directory of browser for destination
                        upload(bookmark, files, destination);
                    }
                    else {
                        // No mounted browser
                        if(StringUtils.isNotBlank(bookmark.getDefaultPath())) {
                            upload(bookmark, files, new Path(PathNormalizer.normalize(bookmark.getDefaultPath()), EnumSet.of(Path.Type.directory)));
                        }
                        else {
                            upload(bookmark, files, null == destination ? new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.directory)) : destination);
                        }
                    }
                }
            }

            @Override
            public boolean validate(final int option) {
                return StringUtils.isNotEmpty(bookmarksPopup.selectedItem().representedObject());
            }
        };
        TransferControllerFactory.get().alert(controller);
        return true;
    }

    private void upload(final Host bookmark, final List<Local> files, final Path destination) {
        final List<TransferItem> roots = new ArrayList<TransferItem>();
        for(Local file : files) {
            roots.add(new TransferItem(new Path(destination, file.getName(),
                file.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), file));
        }
        final TransferController t = TransferControllerFactory.get();
        t.start(new UploadTransfer(bookmark, roots), new TransferOptions());
    }

    /**
     * Sent directly by theApplication to the delegate. The method should attempt to open the file filename, returning
     * true if the file is successfully opened, and false otherwise. By design, a file opened through this method is
     * assumed to be temporary its the application's responsibility to remove the file at the appropriate time.
     */
    @Override
    public boolean application_openTempFile(NSApplication app, String filename) {
        log.debug("applicationOpenTempFile:{}", filename);
        return this.application_openFile(app, filename);
    }

    /**
     * Invoked immediately before opening an untitled file. Return false to prevent the application from opening an
     * untitled file; return true otherwise. Note that applicationOpenUntitledFile is invoked if this method returns
     * true.
     */
    @Override
    public boolean applicationShouldOpenUntitledFile(NSApplication sender) {
        log.debug("applicationShouldOpenUntitledFile");
        return preferences.getBoolean("browser.open.untitled");
    }

    /**
     * @return true if the file was successfully opened, false otherwise.
     */
    @Override
    public boolean applicationOpenUntitledFile(NSApplication app) {
        log.debug("applicationOpenUntitledFile");
        return false;
    }

    /**
     * These events are sent whenever the Finder reactivates an already running application because someone
     * double-clicked it again or used the dock to activate it. By default the Application Kit will handle this event by
     * checking whether there are any visible NSWindows (not NSPanels), and, if there are none, it goes through the
     * standard untitled document creation (the same as it does if theApplication is launched without any document to
     * open). For most document-based applications, an untitled document will be created. The application delegate will
     * also get a chance to respond to the normal untitled document delegations. If you implement this method in your
     * application delegate, it will be called before any of the default behavior happens. If you return true, then
     * NSApplication will go on to do its normal thing. If you return false, then NSApplication will do nothing. So, you
     * can either implement this method, do nothing, and return false if you do not want anything to happen at all (not
     * recommended), or you can implement this method, handle the event yourself in some custom way, and return false.
     */
    @Override
    public boolean applicationShouldHandleReopen_hasVisibleWindows(final NSApplication app, final boolean visibleWindowsFound) {
        // While an application is open, the Dock icon has a symbol below it.
        log.debug("Should handle reopen with windows {}", visibleWindowsFound);
        // When a user clicks an open application’s icon in the Dock, the application
        // becomes active and all open unminimized windows are brought to the front;
        // minimized document windows remain in the Dock. If there are no unminimized
        // windows when the user clicks the Dock icon, the last minimized window should
        // be expanded and made active. If no documents are open, the application should
        // open a new window. (If your application is not document-based, display the
        // application’s main window.)
        if(MainController.getBrowsers().isEmpty() && !TransferControllerFactory.get().isVisible()) {
            this.openDefaultBookmark(newDocument());
        }
        NSWindow miniaturized = null;
        for(BrowserController browser : MainController.getBrowsers()) {
            if(!browser.window().isMiniaturized()) {
                return false;
            }
            if(null == miniaturized) {
                miniaturized = browser.window();
            }
        }
        if(null == miniaturized) {
            return false;
        }
        miniaturized.deminiaturize(null);
        return false;
    }

    // User bookmarks and thirdparty applications
    private final CountDownLatch bookmarksSemaphore = new CountDownLatch(1);

    /**
     * Sent by the default notification center after the application has been launched and initialized but before it has
     * received its first event. aNotification is always an ApplicationDidFinishLaunchingNotification. You can retrieve
     * the NSApplication object in question by sending object to aNotification. The delegate can implement this method
     * to perform further initialization. If the user started up the application by double-clicking a file, the delegate
     * receives the applicationOpenFile message before receiving applicationDidFinishLaunching.
     * (applicationWillFinishLaunching is sent before applicationOpenFile.)
     */
    @Override
    public void applicationWillFinishLaunching(NSNotification notification) {
        // Opt-in of automatic window tabbing
        NSWindow.setAllowsAutomaticWindowTabbing(true);
        // Load main menu
        this.loadBundle();
        if(preferences.getBoolean("queue.window.open.default")) {
            final TransferController c = TransferControllerFactory.get();
            c.display();
        }
        final AbstractHostCollection bookmarks = BookmarkCollection.defaultCollection();
        final AbstractHostCollection sessions = SessionsCollection.defaultCollection();
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                if(preferences.getBoolean("browser.serialize")) {
                    sessions.load();
                }
                // Load all bookmarks in background
                bookmarks.load();
                bookmarksSemaphore.countDown();
                return null;
            }

            @Override
            public void cleanup() {
                for(Host host : sessions) {
                    log.info("New browser for saved session {}", host);
                    final BrowserController browser = newDocument(true, host.getUuid());
                    browser.mount(host);
                }
                if(sessions.isEmpty()) {
                    // Open default windows
                    if(preferences.getBoolean("browser.open.untitled")) {
                        if(preferences.getProperty("browser.open.bookmark.default") != null) {
                            openDefaultBookmark(newDocument());
                        }
                        else {
                            final BrowserController c = newDocument();
                            c.display();
                        }
                    }
                }
                sessions.clear();
                // Set delegate for NSService
                NSApplication.sharedApplication().setServicesProvider(MainController.this.id());
            }
        });
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                final HistoryCollection history = HistoryCollection.defaultCollection();
                history.load();
                return null;
            }
        });
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                final TransferCollection transfers = TransferCollection.defaultCollection();
                transfers.load();
                return null;
            }
        });
        final Rendezvous bonjour = RendezvousFactory.instance();
        bonjour.addListener(new NotificationRendezvousListener(bonjour));
        final SchemeHandler schemeHandler = SchemeHandlerFactory.get();
        if(preferences.getBoolean("defaulthandler.reminder") && preferences.getInteger("uses") > 0) {
            if(!schemeHandler.isDefaultHandler(Arrays.asList(Scheme.ftp.name(), Scheme.ftps.name(), Scheme.sftp.name()),
                new Application(preferences.getProperty("application.identifier")))) {
                final NSAlert alert = NSAlert.alert(
                    LocaleFactory.localizedString("Set Cyberduck as default application for FTP and SFTP locations?", "Configuration"),
                    LocaleFactory.localizedString("As the default application, Cyberduck will open when you click on FTP or SFTP links " +
                        "in other applications, such as your web browser. You can change this setting in the Preferences later.", "Configuration"),
                    LocaleFactory.localizedString("Change", "Configuration"), //default
                    null, //other
                    LocaleFactory.localizedString("Cancel", "Configuration")
                );
                alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                alert.setShowsSuppressionButton(true);
                alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
                int choice = this.alert(new SystemAlertController(alert));
                if(alert.suppressionButton().state() == NSCell.NSOnState) {
                    // Never show again.
                    preferences.setProperty("defaulthandler.reminder", false);
                }
                if(choice == SheetCallback.DEFAULT_OPTION) {
                    schemeHandler.setDefaultHandler(
                        new Application(preferences.getProperty("application.identifier")),
                        Arrays.asList(Scheme.ftp.name(), Scheme.ftps.name(), Scheme.sftp.name())
                    );
                }
            }
        }
        final ProtocolFactory protocols = ProtocolFactory.get();
        // Iterate over custom schemes and register by default
        for(Protocol protocol : protocols.find()) {
            final String[] schemes = protocol.getSchemes();
            for(String scheme : schemes) {
                if(Arrays.stream(Scheme.values()).filter(Predicate.isEqual(Scheme.s3).negate()).noneMatch(s -> s.name().equals(scheme))) {
                    log.info("Register custom scheme {}", scheme);
                    schemeHandler.setDefaultHandler(new Application(preferences.getProperty("application.identifier")), Collections.singletonList(scheme));
                }
            }
        }
        // NSWorkspace notifications are posted to a notification center provided by
        // the NSWorkspace object, instead of going through the application’s default
        // notification center as most notifications do. To receive NSWorkspace notifications,
        // your application must register an observer with the NSWorkspace notification center.
        workspace.notificationCenter().addObserver(this.id(),
            Foundation.selector("workspaceWillPowerOff:"),
            NSWorkspace.WorkspaceWillPowerOffNotification,
            null);
        workspace.notificationCenter().addObserver(this.id(),
            Foundation.selector("workspaceWillLogout:"),
            NSWorkspace.WorkspaceSessionDidResignActiveNotification,
            null);
        workspace.notificationCenter().addObserver(this.id(),
            Foundation.selector("workspaceWillSleep:"),
            NSWorkspace.WorkspaceWillSleepNotification,
            null);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
            Foundation.selector("applicationWillRestartAfterUpdate:"),
            "SUUpdaterWillRestartNotificationName",
            null);
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() {
                bonjour.init();
                return null;
            }
        });
        // Import thirdparty bookmarks.
        this.background(new ImporterBackgroundAction(bookmarks, bookmarksSemaphore));
        final CrashReporter reporter = CrashReporter.create();
        log.info("Check for crash report");
        reporter.checkForCrash(preferences.getProperty("website.crash"));
        if(updater.hasUpdatePrivileges()) {
            if(preferences.getBoolean("update.check")) {
                final long next = preferences.getLong("update.check.timestamp") + preferences.getLong("update.check.interval") * 1000;
                if(next < System.currentTimeMillis()) {
                    updater.check(true);
                }
                updater.register();
            }
        }
        if(preferences.getBoolean("profiles.discovery.updater.enable")) {
            // Synchronize and register timer
            profiles.register();
        }
        NSAppleEventManager.sharedAppleEventManager().setEventHandler_andSelector_forEventClass_andEventID(
                this.id(), Foundation.selector("handleGetURLEvent:withReplyEvent:"), kInternetEventClass, kAEGetURL);
    }

    /**
     * NSService implementation
     */
    @Action
    public void serviceUploadFileUrl_(final NSPasteboard pboard, final String userData) {
        log.debug("serviceUploadFileUrl_: with user data {}", userData);
        if(pboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            NSObject o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                if(o.isKindOfClass(NSArray.CLASS)) {
                    final NSArray elements = Rococoa.cast(o, NSArray.class);
                    List<Local> files = new ArrayList<Local>();
                    for(int i = 0; i < elements.count().intValue(); i++) {
                        files.add(LocalFactory.get(elements.objectAtIndex(new NSUInteger(i)).toString()));
                    }
                    this.upload(files);
                }
            }
        }
    }

    /**
     * Invoked from within the terminate method immediately before the application terminates. sender is the
     * NSApplication to be terminated. If this method returns false, the application is not terminated, and control
     * returns to the main event loop.
     *
     * @param app Application instance
     * @return Return true to allow the application to terminate.
     */
    @Override
    public NSUInteger applicationShouldTerminate(final NSApplication app) {
        log.debug("Application should quit with notification");
        // Determine if there are any running transfers
        final NSUInteger result = TransferControllerFactory.applicationShouldTerminate(app);
        if(!result.equals(NSApplication.NSTerminateNow)) {
            return result;
        }
        final SessionsCollection sessions = SessionsCollection.defaultCollection();
        // Determine if there are any open connections
        for(BrowserController browser : MainController.getBrowsers()) {
            if(preferences.getBoolean("browser.serialize")) {
                if(browser.isMounted()) {
                    // The workspace should be saved. Serialize all open browser sessions
                    final Host serialized
                            = new HostDictionary<>().deserialize(browser.getSession().getHost().serialize(SerializerFactory.get()));
                    serialized.setWorkdir(browser.workdir());
                    sessions.add(serialized);
                    browser.window().saveFrameUsingName(serialized.getUuid());
                }
            }
            if(browser.isConnected()) {
                if(preferences.getBoolean("browser.disconnect.confirm")) {
                    final NSAlert alert = NSAlert.alert(LocaleFactory.localizedString("Quit"),
                        LocaleFactory.localizedString("You are connected to at least one remote site. Do you want to review open browsers?"),
                        LocaleFactory.localizedString("Quit Anyway"), //default
                        LocaleFactory.localizedString("Cancel"), //other
                        LocaleFactory.localizedString("Review…"));
                    alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
                    alert.setShowsSuppressionButton(true);
                    alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
                    int choice = this.alert(new SystemAlertController(alert));
                    if(alert.suppressionButton().state() == NSCell.NSOnState) {
                        // Never show again.
                        preferences.setProperty("browser.disconnect.confirm", false);
                    }
                    if(choice == SheetCallback.ALTERNATE_OPTION) {
                        // Cancel. Quit has been interrupted. Delete any saved sessions so far.
                        sessions.clear();
                        return NSApplication.NSTerminateCancel;
                    }
                    if(choice == SheetCallback.CANCEL_OPTION) {
                        // Review if at least one window requested to terminate later, we shall wait.
                        // This will iterate over all mounted browsers.
                        if(NSApplication.NSTerminateNow.equals(BrowserController.applicationShouldTerminate(app))) {
                            return this.applicationShouldTerminateAfterDonationPrompt(app);
                        }
                        return NSApplication.NSTerminateLater;
                    }
                    if(choice == SheetCallback.DEFAULT_OPTION) {
                        // Quit immediatly
                        return this.applicationShouldTerminateAfterDonationPrompt(app);
                    }
                }
                else {
                    browser.windowShouldClose(browser.window());
                }
            }
        }
        return this.applicationShouldTerminateAfterDonationPrompt(app);
    }

    public NSUInteger applicationShouldTerminateAfterDonationPrompt(final NSApplication app) {
        log.debug("applicationShouldTerminateAfterDonationPrompt");
        if(!displayDonationPrompt) {
            // Already displayed
            return NSApplication.NSTerminateNow;
        }
        final License key = LicenseFactory.find();
        if(!key.verify()) {
            final String lastversion = preferences.getProperty("donate.reminder");
            if(NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleShortVersionString").toString().equals(lastversion)) {
                // Do not display if same version is installed
                return NSApplication.NSTerminateNow;
            }
            // Display after upgrade
            final Calendar nextreminder = Calendar.getInstance();
            nextreminder.setTimeInMillis(preferences.getLong("donate.reminder.date"));
            // Display prompt every n days
            nextreminder.add(Calendar.DAY_OF_YEAR, preferences.getInteger("donate.reminder.interval"));
            log.debug("Next reminder {}", nextreminder.getTime().toString());
            if(nextreminder.getTime().after(new Date(System.currentTimeMillis()))) {
                // Do not display if shown in the reminder interval
                return NSApplication.NSTerminateNow;
            }
            // Make sure prompt is not loaded twice upon next quit event
            displayDonationPrompt = false;
            donationController = new DonateAlertController(app);
            this.alert(donationController);
            // Delay application termination. Dismissing the donation dialog will reply to quit.
            return NSApplication.NSTerminateLater;
        }
        return NSApplication.NSTerminateNow;
    }

    /**
     * Quits the Rendezvous daemon and saves all preferences
     *
     * @param notification Notification name
     */
    @Override
    public void applicationWillTerminate(NSNotification notification) {
        log.debug("Application will quit with notification {}", notification);
        this.invalidate();
        OAuth2TokenListenerRegistry.get().shutdown();
        // Clear temporary files
        TemporaryFileServiceFactory.get().shutdown();
        //Terminating rendezvous discovery
        RendezvousFactory.instance().quit();
        // Remove notifications from center
        NotificationServiceFactory.get().unregister();
        // Disable update
        updater.unregister();
        profiles.unregister();
        //Writing usage info
        preferences.setProperty("uses", preferences.getInteger("uses") + 1);
        preferences.save();
        DefaultBackgroundExecutor.get().shutdown();
    }

    @Action
    public void applicationWillRestartAfterUpdate(ID updater) {
        // Disable donation prompt after udpate install
        displayDonationPrompt = false;
    }

    /**
     * We are not a Windows application. Long live the application wide menu bar.
     */
    @Override
    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
        return false;
    }

    @Action
    public void updateMenuClicked(ID sender) {
        updater.check(false);
    }

    /**
     * Extract the URL from the Apple event and handle it here.
     */
    @Action
    public void handleGetURLEvent_withReplyEvent(NSAppleEventDescriptor event, NSAppleEventDescriptor reply) {
        log.debug("Received URL from event {}", event);
        final NSAppleEventDescriptor param = event.paramDescriptorForKeyword(keyAEResult);
        if(null == param) {
            log.error("No URL parameter");
            return;
        }
        final String url = param.stringValue();
        if(StringUtils.isEmpty(url)) {
            log.error("URL parameter is empty");
            return;
        }
        switch(url) {
            case "x-cyberduck-action:update":
                updater.check(false);
                break;
            default:
                String action = null;
                if(StringUtils.contains(url, ":oauth")) {
                    action = StringUtils.substringAfter(url, ":oauth");
                }
                if(StringUtils.contains(url, "://oauth")) {
                    action = StringUtils.substringAfter(url, "://oauth");
                }
                if(null != action) {
                    log.debug("Handle {} as OAuth callback", url);
                    final List<NameValuePair> pairs = URLEncodedUtils.parse(URI.create(action), Charset.defaultCharset());
                    String state = StringUtils.EMPTY;
                    String code = StringUtils.EMPTY;
                    for(NameValuePair pair : pairs) {
                        if(StringUtils.equals(pair.getName(), "state")) {
                            state = StringUtils.equals(pair.getName(), "state") ? pair.getValue() : StringUtils.EMPTY;
                        }
                        if(StringUtils.equals(pair.getName(), "code")) {
                            code = StringUtils.equals(pair.getName(), "code") ? pair.getValue() : StringUtils.EMPTY;
                        }
                    }
                    final OAuth2TokenListenerRegistry oauth = OAuth2TokenListenerRegistry.get();
                    oauth.notify(state, code);
                }
                else if(StringUtils.startsWith(url, CteraProtocol.CTERA_REDIRECT_URI)) {
                    action = StringUtils.removeStart(url, String.format("%s:", preferences.getProperty("oauth.handler.scheme")));
                    final List<NameValuePair> pairs = URLEncodedUtils.parse(URI.create(action), Charset.defaultCharset());
                    String code = StringUtils.EMPTY;
                    for(NameValuePair pair : pairs) {
                        if(StringUtils.equals(pair.getName(), "ActivationCode")) {
                            code = StringUtils.equals(pair.getName(), "ActivationCode") ? pair.getValue() : StringUtils.EMPTY;
                        }
                    }
                    final OAuth2TokenListenerRegistry oauth = OAuth2TokenListenerRegistry.get();
                    oauth.notify(StringUtils.EMPTY, code);
                }
                else {
                    try {
                        final Host h = HostParser.parse(url);
                        h.setCredentials(CredentialsConfiguratorFactory.get(h.getProtocol()).configure(h));
                        if(Path.Type.file == detector.detect(h.getDefaultPath())) {
                            final Path file = new Path(PathNormalizer.normalize(h.getDefaultPath()), EnumSet.of(Path.Type.file));
                            TransferControllerFactory.get().start(new DownloadTransfer(h, file,
                                    LocalFactory.get(LocalFactory.get(preferences.getProperty("queue.download.folder")).setBookmark(
                                            preferences.getProperty("queue.download.folder.bookmark")), file.getName())), new TransferOptions());
                        }
                        else {
                            for(BrowserController browser : MainController.getBrowsers()) {
                                if(browser.isMounted()) {
                                    if(new HostUrlProvider().get(browser.getSession().getHost()).equals(new HostUrlProvider().get(h))) {
                                        // Handle browser window already connected to the same host. #4215
                                        browser.display();
                                        if(Path.Type.directory == detector.detect(h.getDefaultPath())) {
                                            browser.setWorkdir(new Path(PathNormalizer.normalize(h.getDefaultPath()), EnumSet.of(Path.Type.directory)));
                                        }
                                        return;
                                    }
                                }
                            }
                            final BrowserController browser = newDocument(false);
                            browser.mount(h);
                        }
                    }
                    catch(HostParserException e) {
                        log.warn(e);
                    }
                }
        }
    }

    /**
     * Posted when the user has requested a logout or that the machine be powered off.
     *
     * @param notification Notification name
     */
    @Action
    public void workspaceWillPowerOff(NSNotification notification) {
        log.debug("Workspace will power off with notification {}", notification);
    }

    /**
     * Posted before a user session is switched out. This allows an application to disable some processing when its user
     * session is switched out, and reenable when that session gets switched back in, for example.
     *
     * @param notification Notification name
     */
    @Action
    public void workspaceWillLogout(NSNotification notification) {
        log.debug("Workspace will logout with notification {}", notification);
    }

    @Action
    public void workspaceWillSleep(NSNotification notification) {
        log.debug("Workspace will sleep with notification {}", notification);
    }

    private final class ImporterBackgroundAction extends AbstractBackgroundAction<Void> {
        private final Preferences preferences = PreferencesFactory.get();

        private final AbstractHostCollection bookmarks;
        private final List<ThirdpartyBookmarkCollection> collections;
        private final CountDownLatch lock;

        public ImporterBackgroundAction(final AbstractHostCollection bookmarks, final CountDownLatch lock) {
            this(bookmarks, lock, Arrays.asList(
                new Transmit5BookmarkCollection(), new Transmit4BookmarkCollection(), new FilezillaBookmarkCollection(), new FetchBookmarkCollection(),
                new FlowBookmarkCollection(), new InterarchyBookmarkCollection(), new CrossFtpBookmarkCollection(), new FireFtpBookmarkCollection(),
                new Expandrive3BookmarkCollection(), new Expandrive4BookmarkCollection(), new Expandrive5BookmarkCollection(), new Expandrive6BookmarkCollection(),
                new Expandrive7BookmarkCollection(), new CloudMounterBookmarkCollection()));
        }

        public ImporterBackgroundAction(final AbstractHostCollection bookmarks, final CountDownLatch lock, final List<ThirdpartyBookmarkCollection> collections) {
            this.bookmarks = bookmarks;
            this.lock = lock;
            this.collections = collections;
        }

        @Override
        public Void run() {
            for(ThirdpartyBookmarkCollection t : collections) {
                if(!t.isInstalled()) {
                    log.info("No application installed for {}", t.getBundleIdentifier());
                    continue;
                }
                try {
                    t.load();
                }
                catch(AccessDeniedException e) {
                    log.warn("Failure {} loading bookmarks from {}", e, t);
                }
                if(t.isEmpty()) {
                    // Flag as imported
                    preferences.setProperty(t.getConfiguration(), true);
                }
            }
            Uninterruptibles.awaitUninterruptibly(lock);
            return null;
        }

        @Override
        public void cleanup() {
            for(ThirdpartyBookmarkCollection t : collections) {
                t.filter(bookmarks);
                if(t.isEmpty()) {
                    preferences.setProperty(t.getConfiguration(), true);
                    continue;
                }
                final NSAlert alert = NSAlert.alert(
                    MessageFormat.format(LocaleFactory.localizedString("Import {0} Bookmarks", "Configuration"), t.getName()),
                    MessageFormat.format(LocaleFactory.localizedString("{0} bookmarks found. Do you want to add these to your bookmarks?", "Configuration"), t.size()),
                    LocaleFactory.localizedString("Import", "Configuration"), //default
                    null, //other
                    LocaleFactory.localizedString("Cancel", "Configuration"));
                alert.setShowsSuppressionButton(true);
                alert.suppressionButton().setTitle(LocaleFactory.localizedString("Don't ask again", "Configuration"));
                alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                int choice = MainController.this.alert(new SystemAlertController(alert)); //alternate
                if(alert.suppressionButton().state() == NSCell.NSOnState) {
                    // Never show again.
                    preferences.setProperty(t.getConfiguration(), true);
                }
                if(choice == SheetCallback.DEFAULT_OPTION) {
                    bookmarks.addAll(t);
                    // Flag as imported
                    preferences.setProperty(t.getConfiguration(), true);
                }
            }
        }
    }

    @Override
    public boolean validateMenuItem(final NSMenuItem item) {
        final Selector action = item.action();
        if(action.equals(Foundation.selector("updateMenuClicked:"))) {
            if(updater.hasUpdatePrivileges()) {
                if(item.representedObject() != null) {
                    // Allow to run update when found
                    return true;
                }
                return !updater.isUpdateInProgress();
            }
            return false;
        }
        return true;
    }
}
