package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
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

import ch.cyberduck.binding.Action;
import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.*;
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
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.bonjour.NotificationRendezvousListener;
import ch.cyberduck.core.bonjour.Rendezvous;
import ch.cyberduck.core.bonjour.RendezvousFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.importer.CrossFtpBookmarkCollection;
import ch.cyberduck.core.importer.Expandrive3BookmarkCollection;
import ch.cyberduck.core.importer.Expandrive4BookmarkCollection;
import ch.cyberduck.core.importer.Expandrive5BookmarkCollection;
import ch.cyberduck.core.importer.FetchBookmarkCollection;
import ch.cyberduck.core.importer.FilezillaBookmarkCollection;
import ch.cyberduck.core.importer.FireFtpBookmarkCollection;
import ch.cyberduck.core.importer.FlowBookmarkCollection;
import ch.cyberduck.core.importer.InterarchyBookmarkCollection;
import ch.cyberduck.core.importer.ThirdpartyBookmarkCollection;
import ch.cyberduck.core.importer.Transmit4BookmarkCollection;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.notification.NotificationServiceFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.serializer.HostDictionary;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.core.updater.PeriodicUpdateChecker;
import ch.cyberduck.core.updater.PeriodicUpdateCheckerFactory;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;
import ch.cyberduck.ui.browser.Column;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;
import ch.cyberduck.ui.cocoa.delegate.ArchiveMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.BookmarkMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.CopyURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.EditMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.OpenURLMenuDelegate;
import ch.cyberduck.ui.cocoa.delegate.URLMenuDelegate;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSRect;
import org.rococoa.cocoa.foundation.NSUInteger;

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

/**
 * Setting the main menu and implements application delegate methods
 */
public class MainController extends BundleController implements NSApplication.Delegate {
    private static final Logger log = Logger.getLogger(MainController.class);

    /**
     * Apple event constants<br>
     * **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:117</i>
     */
    public static final int kInternetEventClass = 1196773964;
    /**
     * Apple event constants<br>
     * **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:118</i>
     */
    public static final int kAEGetURL = 1196773964;
    /**
     * Apple event constants<br>
     * **********************************************************************************************<br>
     * <i>native declaration : /Developer/SDKs/MacOSX10.5.sdk/usr/include/AvailabilityMacros.h:119</i>
     */
    public static final int kAEFetchURL = 1179996748;

    /// 0x2d2d2d2d
    public static final int keyAEResult = 757935405;

    private final Preferences preferences = PreferencesFactory.get();

    private final PeriodicUpdateChecker updater = PeriodicUpdateCheckerFactory.get();

    public MainController() {
        this.loadBundle();
    }

    @Override
    public void awakeFromNib() {
        NSAppleEventManager.sharedAppleEventManager().setEventHandler_andSelector_forEventClass_andEventID(
                this.id(), Foundation.selector("handleGetURLEvent:withReplyEvent:"), kInternetEventClass, kAEGetURL);

        super.awakeFromNib();
    }

    private PathKindDetector detector = new DefaultPathKindDetector();

    /**
     * Extract the URL from the Apple event and handle it here.
     */
    public void handleGetURLEvent_withReplyEvent(NSAppleEventDescriptor event, NSAppleEventDescriptor reply) {
        log.debug("Received URL from Apple Event:" + event);
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
        if("x-cyberduck-action:update".equals(url)) {
            this.updateMenuClicked(null);
        }
        else {
            final Host h = HostParser.parse(url);
            if(Path.Type.file == detector.detect(h.getDefaultPath())) {
                final Path file = new Path(h.getDefaultPath(), EnumSet.of(Path.Type.file));
                TransferControllerFactory.get().start(new DownloadTransfer(h, file,
                        LocalFactory.get(preferences.getProperty("queue.download.folder"), file.getName())));
            }
            else {
                for(BrowserController browser : MainController.getBrowsers()) {
                    if(browser.isMounted()) {
                        if(new HostUrlProvider().get(browser.getSession().getHost()).equals(
                                new HostUrlProvider().get(h))) {
                            // Handle browser window already connected to the same host. #4215
                            browser.window().makeKeyAndOrderFront(null);
                            return;
                        }
                    }
                }
                final BrowserController browser = newDocument(false);
                browser.mount(h);
            }
        }
    }

    @Action
    public void updateMenuClicked(ID sender) {
        updater.check(false);
    }

    @Outlet
    private NSMenu applicationMenu;

    public void setApplicationMenu(NSMenu menu) {
        this.applicationMenu = menu;
        this.updateLicenseMenu();
        this.updateUpdateMenu();
    }

    /**
     * Set name of key in menu item
     */
    private void updateLicenseMenu() {
        final License key = LicenseFactory.find();
        if(key.isReceipt()) {
            this.applicationMenu.removeItemAtIndex(new NSInteger(5));
            this.applicationMenu.removeItemAtIndex(new NSInteger(4));
        }
        else {
            NSDictionary KEY_FONT_ATTRIBUTES = NSDictionary.dictionaryWithObjectsForKeys(
                    NSArray.arrayWithObjects(NSFont.userFontOfSize(NSFont.smallSystemFontSize()), NSColor.darkGrayColor()),
                    NSArray.arrayWithObjects(NSAttributedString.FontAttributeName, NSAttributedString.ForegroundColorAttributeName)
            );
            this.applicationMenu.itemAtIndex(new NSInteger(5)).setAttributedTitle(
                    NSAttributedString.attributedStringWithAttributes(key.toString(), KEY_FONT_ATTRIBUTES)
            );
        }
    }

    /**
     * Remove software update menu item if no update feed available
     */
    private void updateUpdateMenu() {
        if(!updater.hasUpdatePrivileges()) {
            this.applicationMenu.removeItemAtIndex(new NSInteger(1));
        }
    }

    @Outlet
    private NSMenu encodingMenu;

    public void setEncodingMenu(NSMenu encodingMenu) {
        this.encodingMenu = encodingMenu;
        for(String charset : new DefaultCharsetProvider().availableCharsets()) {
            this.encodingMenu.addItemWithTitle_action_keyEquivalent(charset, Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
        }
    }

    @Outlet
    private NSMenu columnMenu;

    public void setColumnMenu(NSMenu columnMenu) {
        this.columnMenu = columnMenu;
        Map<String, String> columns = new HashMap<String, String>();
        columns.put(String.format("browser.column.%s", Column.kind.name()), LocaleFactory.localizedString("Kind"));
        columns.put(String.format("browser.column.%s", Column.extension.name()), LocaleFactory.localizedString("Extension"));
        columns.put(String.format("browser.column.%s", Column.size.name()), LocaleFactory.localizedString("Size"));
        columns.put(String.format("browser.column.%s", Column.modified.name()), LocaleFactory.localizedString("Modified"));
        columns.put(String.format("browser.column.%s", Column.owner.name()), LocaleFactory.localizedString("Owner"));
        columns.put(String.format("browser.column.%s", Column.group.name()), LocaleFactory.localizedString("Group"));
        columns.put(String.format("browser.column.%s", Column.permission.name()), LocaleFactory.localizedString("Permissions"));
        columns.put(String.format("browser.column.%s", Column.region.name()), LocaleFactory.localizedString("Region"));
        columns.put(String.format("browser.column.%s", Column.version.name()), LocaleFactory.localizedString("Version"));
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

    @Outlet
    private NSMenu editMenu;

    @Delegate
    private EditMenuDelegate editMenuDelegate;

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
                return MainController.getBrowser().id();
            }
        };
        this.editMenu.setDelegate(editMenuDelegate.id());
    }

    @Outlet
    private NSMenu urlMenu;

    @Delegate
    private URLMenuDelegate urlMenuDelegate;

    public void setUrlMenu(NSMenu urlMenu) {
        this.urlMenu = urlMenu;
        this.urlMenuDelegate = new CopyURLMenuDelegate() {
            @Override
            protected Session<?> getSession() {
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
                        List<Path> selected = controller.getSelectedPaths();
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

    @Outlet
    private NSMenu openUrlMenu;

    @Delegate
    private URLMenuDelegate openUrlMenuDelegate;

    public void setOpenUrlMenu(NSMenu openUrlMenu) {
        this.openUrlMenu = openUrlMenu;
        this.openUrlMenuDelegate = new OpenURLMenuDelegate() {
            @Override
            protected Session<?> getSession() {
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
                        List<Path> selected = controller.getSelectedPaths();
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

    @Outlet
    private NSMenu archiveMenu;

    @Delegate
    private ArchiveMenuDelegate archiveMenuDelegate;

    public void setArchiveMenu(NSMenu archiveMenu) {
        this.archiveMenu = archiveMenu;
        this.archiveMenuDelegate = new ArchiveMenuDelegate();
        this.archiveMenu.setDelegate(archiveMenuDelegate.id());
    }

    @Outlet
    private NSMenu bookmarkMenu;

    @Delegate
    private BookmarkMenuDelegate bookmarkMenuDelegate;

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
        BrowserLauncherFactory.get().open(preferences.getProperty("website.help"));
    }

    @Action
    public void licenseMenuClicked(final ID sender) {
        ApplicationLauncherFactory.get().open(
                LocalFactory.get(NSBundle.mainBundle().pathForResource_ofType("License", "txt")));
    }

    @Action
    public void acknowledgmentsMenuClicked(final ID sender) {
        ApplicationLauncherFactory.get().open(
                LocalFactory.get(NSBundle.mainBundle().pathForResource_ofType("Acknowledgments", "rtf")));
    }

    @Action
    public void websiteMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("website.home"));
    }

    @Action
    public void forumMenuClicked(final ID sender) {
        BrowserLauncherFactory.get().open(preferences.getProperty("website.forum"));
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
                        preferences.getProperty("application.revision")),
                NSArray.arrayWithObjects(
                        "ApplicationName",
                        "ApplicationVersion",
                        "Version")
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
        PreferencesController controller = PreferencesControllerFactory.instance();
        controller.window().makeKeyAndOrderFront(null);
    }

    @Action
    public void newDownloadMenuClicked(final ID sender) {
        this.showTransferQueueClicked(sender);
        SheetController c = new DownloadController(TransferControllerFactory.get());
        c.beginSheet();
    }

    @Action
    public void newBrowserMenuClicked(final ID sender) {
        this.openDefaultBookmark(MainController.newDocument(true));
    }

    @Action
    public void showTransferQueueClicked(final ID sender) {
        TransferController c = TransferControllerFactory.get();
        c.window().makeKeyAndOrderFront(null);
    }

    @Action
    public void showActivityWindowClicked(final ID sender) {
        ActivityController c = ActivityControllerFactory.get();
        if(c.isVisible()) {
            c.window().close();
        }
        else {
            c.window().orderFront(null);
        }
    }

    @Override
    public boolean application_openFile(final NSApplication app, final String filename) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Open file %s", filename));
        }
        final Local f = LocalFactory.get(filename);
        if(f.exists()) {
            if("duck".equals(f.getExtension())) {
                final Host bookmark;
                try {
                    bookmark = HostReaderFactory.get().read(f);
                    if(null == bookmark) {
                        return false;
                    }
                    MainController.newDocument().mount(bookmark);
                    return true;
                }
                catch(AccessDeniedException e) {
                    log.error(e.getMessage());
                    return false;
                }
            }
            else if("cyberducklicense".equals(f.getExtension())) {
                final License l = LicenseFactory.get(f);
                if(l.verify()) {
                    try {
                        f.copy(LocalFactory.get(preferences.getProperty("application.support.path"), f.getName()));
                        final NSAlert alert = NSAlert.alert(
                                l.toString(),
                                LocaleFactory.localizedString("Thanks for your support! Your contribution helps to further advance development to make Cyberduck even better.", "License")
                                        + "\n\n"
                                        + LocaleFactory.localizedString("Your donation key has been copied to the Application Support folder.", "License"),
                                LocaleFactory.localizedString("Continue", "License"), //default
                                null, //other
                                null
                        );
                        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
                        if(this.alert(alert) == SheetCallback.DEFAULT_OPTION) {
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
                            LocaleFactory.localizedString("Not a valid donation key", "License"),
                            LocaleFactory.localizedString("This donation key does not appear to be valid.", "License"),
                            LocaleFactory.localizedString("Continue", "License"), //default
                            null, //other
                            null);
                    alert.setAlertStyle(NSAlert.NSWarningAlertStyle);
                    alert.setShowsHelp(true);
                    alert.setDelegate(new ProxyController() {
                        @Action
                        public boolean alertShowHelp(NSAlert alert) {
                            StringBuilder site = new StringBuilder(preferences.getProperty("website.help"));
                            site.append("/").append("faq");
                            BrowserLauncherFactory.get().open(site.toString());
                            return true;
                        }

                    }.id());
                    this.alert(alert);
                }
                return true;
            }
            else if("cyberduckprofile".equals(f.getExtension())) {
                try {
                    final Protocol profile = ProfileReaderFactory.get().read(f);
                    if(null == profile) {
                        return false;
                    }
                    if(profile.isEnabled()) {
                        if(log.isDebugEnabled()) {
                            log.debug(String.format("Register profile %s", profile));
                        }
                        ProtocolFactory.register(profile);
                        final Host host = new Host(profile, profile.getDefaultHostname(), profile.getDefaultPort());
                        MainController.newDocument().addBookmark(host);
                        // Register in application support
                        final Local profiles = LocalFactory.get(preferences.getProperty("application.support.path"),
                                PreferencesFactory.get().getProperty("profiles.folder.name"));
                        profiles.mkdir();
                        f.copy(LocalFactory.get(profiles, f.getName()));
                    }
                }
                catch(AccessDeniedException e) {
                    log.error(e.getMessage());
                    return false;
                }
            }
            else {
                // Upload file
                this.background(new AbstractBackgroundAction<Void>() {
                    @Override
                    public Void run() throws BackgroundException {
                        // Wait until bookmarks are loaded
                        try {
                            bookmarksSemaphore.await();
                        }
                        catch(InterruptedException e) {
                            log.error(String.format("Error awaiting bookmarks to load %s", e.getMessage()));
                        }
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
        for(BrowserController controller : MainController.getBrowsers()) {
            if(controller.isMounted()) {
                open = controller.getSession().getHost();
                workdir = controller.workdir();
                if(1 == MainController.getBrowsers().size()) {
                    // If only one browser window upload to current working directory with no bookmark selection
                    this.upload(open, files, workdir);
                    return true;
                }
                break;
            }
        }
        if(BookmarkCollection.defaultCollection().isEmpty()) {
            log.warn("No bookmark for upload");
            return false;
        }
        final NSPopUpButton bookmarksPopup = NSPopUpButton.buttonWithFrame(new NSRect(0, 26));
        bookmarksPopup.setToolTip(LocaleFactory.localizedString("Bookmarks"));
        for(Host b : BookmarkCollection.defaultCollection()) {
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
            for(Host bookmark : BookmarkCollection.defaultCollection()) {
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
        final TransferController t = TransferControllerFactory.get();
        final Host mount = open;
        final Path destination = workdir;
        AlertController alert = new AlertController(t, NSAlert.alert("Select Bookmark",
                MessageFormat.format("Upload {0} to the selected bookmark.",
                        files.size() == 1 ? files.iterator().next().getName()
                                : MessageFormat.format(LocaleFactory.localizedString("{0} Files"), String.valueOf(files.size()))
                ),
                LocaleFactory.localizedString("Upload", "Transfer"),
                LocaleFactory.localizedString("Cancel"),
                null
        )) {
            @Override
            public void callback(int returncode) {
                if(DEFAULT_OPTION == returncode) {
                    final String selected = bookmarksPopup.selectedItem().representedObject();
                    for(Host bookmark : BookmarkCollection.defaultCollection()) {
                        // Determine selected bookmark
                        if(bookmark.getUuid().equals(selected)) {
                            if(bookmark.equals(mount)) {
                                // Use current working directory of browser for destination
                                upload(bookmark, files, destination);
                            }
                            else {
                                // No mounted browser
                                if(StringUtils.isNotBlank(bookmark.getDefaultPath())) {
                                    upload(bookmark, files, new Path(bookmark.getDefaultPath(), EnumSet.of(Path.Type.directory)));
                                }
                                else {
                                    upload(bookmark, files, destination);
                                }
                            }
                            break;
                        }
                    }
                }
            }

            @Override
            protected boolean validateInput() {
                return StringUtils.isNotEmpty(bookmarksPopup.selectedItem().representedObject());
            }
        };
        alert.setAccessoryView(bookmarksPopup);
        alert.beginSheet();
        return true;
    }

    private void upload(final Host bookmark, final List<Local> files, final Path destination) {
        final List<TransferItem> roots = new ArrayList<TransferItem>();
        for(Local file : files) {
            roots.add(new TransferItem(new Path(destination, file.getName(),
                    file.isDirectory() ? EnumSet.of(Path.Type.directory) : EnumSet.of(Path.Type.file)), file));
        }
        final TransferController t = TransferControllerFactory.get();
        t.start(new UploadTransfer(bookmark, roots));
    }

    /**
     * Sent directly by theApplication to the delegate. The method should attempt to open the file filename,
     * returning true if the file is successfully opened, and false otherwise. By design, a
     * file opened through this method is assumed to be temporary its the application's
     * responsibility to remove the file at the appropriate time.
     */
    @Override
    public boolean application_openTempFile(NSApplication app, String filename) {
        if(log.isDebugEnabled()) {
            log.debug("applicationOpenTempFile:" + filename);
        }
        return this.application_openFile(app, filename);
    }

    /**
     * Invoked immediately before opening an untitled file. Return false to prevent
     * the application from opening an untitled file; return true otherwise.
     * Note that applicationOpenUntitledFile is invoked if this method returns true.
     */
    @Override
    public boolean applicationShouldOpenUntitledFile(NSApplication sender) {
        if(log.isDebugEnabled()) {
            log.debug("applicationShouldOpenUntitledFile");
        }
        return preferences.getBoolean("browser.open.untitled");
    }

    /**
     * @return true if the file was successfully opened, false otherwise.
     */
    @Override
    public boolean applicationOpenUntitledFile(NSApplication app) {
        if(log.isDebugEnabled()) {
            log.debug("applicationOpenUntitledFile");
        }
        return false;
    }

    /**
     * Mounts the default bookmark if any
     */
    private void openDefaultBookmark(BrowserController controller) {
        String defaultBookmark = preferences.getProperty("browser.open.bookmark.default");
        if(null == defaultBookmark) {
            log.info("No default bookmark configured");
            return; //No default bookmark given
        }
        Host bookmark = BookmarkCollection.defaultCollection().lookup(defaultBookmark);
        if(null == bookmark) {
            log.info("Default bookmark no more available");
            return;
        }
        for(BrowserController browser : getBrowsers()) {
            if(browser.getSession() != null) {
                if(browser.getSession().getHost().equals(bookmark)) {
                    log.debug("Default bookmark already mounted");
                    return;
                }
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Mounting default bookmark %s", bookmark));
        }
        controller.mount(bookmark);
    }

    /**
     * These events are sent whenever the Finder reactivates an already running application
     * because someone double-clicked it again or used the dock to activate it. By default
     * the Application Kit will handle this event by checking whether there are any visible
     * NSWindows (not NSPanels), and, if there are none, it goes through the standard untitled
     * document creation (the same as it does if theApplication is launched without any document
     * to open). For most document-based applications, an untitled document will be created.
     * The application delegate will also get a chance to respond to the normal untitled document
     * delegations. If you implement this method in your application delegate, it will be called
     * before any of the default behavior happens. If you return true, then NSApplication will
     * go on to do its normal thing. If you return false, then NSApplication will do nothing.
     * So, you can either implement this method, do nothing, and return false if you do not
     * want anything to happen at all (not recommended), or you can implement this method,
     * handle the event yourself in some custom way, and return false.
     */
    @Override
    public boolean applicationShouldHandleReopen_hasVisibleWindows(final NSApplication app, final boolean visibleWindowsFound) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Should handle reopen with windows %s", visibleWindowsFound));
        }        // While an application is open, the Dock icon has a symbol below it.
        // When a user clicks an open application’s icon in the Dock, the application
        // becomes active and all open unminimized windows are brought to the front;
        // minimized document windows remain in the Dock. If there are no unminimized
        // windows when the user clicks the Dock icon, the last minimized window should
        // be expanded and made active. If no documents are open, the application should
        // open a new window. (If your application is not document-based, display the
        // application’s main window.)
        if(MainController.getBrowsers().isEmpty() && !TransferControllerFactory.get().isVisible()) {
            this.openDefaultBookmark(MainController.newDocument());
        }
        NSWindow miniaturized = null;
        for(BrowserController controller : MainController.getBrowsers()) {
            if(!controller.window().isMiniaturized()) {
                return false;
            }
            if(null == miniaturized) {
                miniaturized = controller.window();
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
    private final CountDownLatch thirdpartySemaphore = new CountDownLatch(1);

    /**
     * Sent by the default notification center after the application has been launched and initialized but
     * before it has received its first event. aNotification is always an
     * ApplicationDidFinishLaunchingNotification. You can retrieve the NSApplication
     * object in question by sending object to aNotification. The delegate can implement
     * this method to perform further initialization. If the user started up the application
     * by double-clicking a file, the delegate receives the applicationOpenFile message before receiving
     * applicationDidFinishLaunching. (applicationWillFinishLaunching is sent before applicationOpenFile.)
     */
    @Override
    public void applicationDidFinishLaunching(NSNotification notification) {
        if(preferences.getBoolean("browser.open.untitled")) {
            MainController.newDocument();
        }
        if(preferences.getBoolean("queue.window.open.default")) {
            this.showTransferQueueClicked(null);
        }
        if(preferences.getBoolean("browser.serialize")) {
            this.background(new AbstractBackgroundAction<Void>() {
                @Override
                public Void run() throws BackgroundException {
                    sessions.load();
                    return null;
                }

                @Override
                public void cleanup() {
                    for(Host host : sessions) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("New browser for saved session %s", host));
                        }
                        final BrowserController browser = MainController.newDocument(false, host.getUuid());
                        browser.mount(host);
                    }
                    sessions.clear();
                }
            });
        }
        // Load all bookmarks in background
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                final BookmarkCollection c = BookmarkCollection.defaultCollection();
                c.load();
                bookmarksSemaphore.countDown();
                return null;
            }

            @Override
            public void cleanup() {
                if(preferences.getBoolean("browser.open.untitled")) {
                    if(preferences.getProperty("browser.open.bookmark.default") != null) {
                        openDefaultBookmark(MainController.newDocument());
                    }
                }
                // Set delegate for NSService
                NSApplication.sharedApplication().setServicesProvider(MainController.this.id());
            }

            @Override
            public String getActivity() {
                return "Loading Bookmarks";
            }
        });
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                HistoryCollection.defaultCollection().load();
                return null;
            }

            @Override
            public String getActivity() {
                return "Loading History";
            }
        });
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                TransferCollection.defaultCollection().load();
                return null;
            }

            @Override
            public String getActivity() {
                return "Loading Transfers";
            }
        });
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                // Make sure we register to Growl first
                NotificationServiceFactory.get().setup();
                return null;
            }

            @Override
            public String getActivity() {
                return "Registering Growl";
            }
        });
        final Rendezvous bonjour = RendezvousFactory.instance();

        bonjour.addListener(new NotificationRendezvousListener(bonjour));
        if(preferences.getBoolean("defaulthandler.reminder")
                && preferences.getInteger("uses") > 0) {
            if(!SchemeHandlerFactory.get().isDefaultHandler(
                    Arrays.asList(Scheme.ftp, Scheme.ftps, Scheme.sftp),
                    new Application(NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleIdentifier").toString()))) {
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
                int choice = alert.runModal(); //alternate
                if(alert.suppressionButton().state() == NSCell.NSOnState) {
                    // Never show again.
                    preferences.setProperty("defaulthandler.reminder", false);
                }
                if(choice == SheetCallback.DEFAULT_OPTION) {
                    SchemeHandlerFactory.get().setDefaultHandler(
                            Arrays.asList(Scheme.ftp, Scheme.ftps, Scheme.sftp),
                            new Application(NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleIdentifier").toString())
                    );
                }
            }
        }
        // NSWorkspace notifications are posted to a notification center provided by
        // the NSWorkspace object, instead of going through the application’s default
        // notification center as most notifications do. To receive NSWorkspace notifications,
        // your application must register an observer with the NSWorkspace notification center.
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(this.id(),
                Foundation.selector("workspaceWillPowerOff:"),
                NSWorkspace.WorkspaceWillPowerOffNotification,
                null);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(this.id(),
                Foundation.selector("workspaceWillLogout:"),
                NSWorkspace.WorkspaceSessionDidResignActiveNotification,
                null);
        NSWorkspace.sharedWorkspace().notificationCenter().addObserver(this.id(),
                Foundation.selector("workspaceWillSleep:"),
                NSWorkspace.WorkspaceWillSleepNotification,
                null);
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("applicationWillRestartAfterUpdate:"),
                "SUUpdaterWillRestartNotificationName",
                null);
        this.background(new AbstractBackgroundAction<Void>() {
            @Override
            public Void run() throws BackgroundException {
                bonjour.init();
                return null;
            }
        });
        // Import thirdparty bookmarks.
        this.background(new AbstractBackgroundAction<Void>() {
            private List<ThirdpartyBookmarkCollection> thirdpartyBookmarkCollections = Collections.emptyList();

            @Override
            public Void run() {
                thirdpartyBookmarkCollections = this.getThirdpartyBookmarks();
                for(ThirdpartyBookmarkCollection t : thirdpartyBookmarkCollections) {
                    if(!t.isInstalled()) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("No application installed for %s", t.getBundleIdentifier()));
                        }
                        continue;
                    }
                    try {
                        t.load();
                    }
                    catch(AccessDeniedException e) {
                        log.warn(String.format("Failure %s loading bookmarks from %s", e, t));
                    }
                    if(t.isEmpty()) {
                        // Flag as imported
                        preferences.setProperty(t.getConfiguration(), true);
                    }
                }
                try {
                    bookmarksSemaphore.await();
                }
                catch(InterruptedException e) {
                    log.error(String.format("Error awaiting bookmarks to load %s", e.getMessage()));
                }
                return null;
            }

            @Override
            public void cleanup() {
                for(ThirdpartyBookmarkCollection t : thirdpartyBookmarkCollections) {
                    final BookmarkCollection bookmarks = BookmarkCollection.defaultCollection();
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
                    int choice = alert.runModal(); //alternate
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
                thirdpartySemaphore.countDown();
            }

            @Override
            public String getActivity() {
                return "Loading thirdparty bookmarks";
            }

            private List<ThirdpartyBookmarkCollection> getThirdpartyBookmarks() {
                return Arrays.asList(new Transmit4BookmarkCollection(), new FilezillaBookmarkCollection(), new FetchBookmarkCollection(),
                        new FlowBookmarkCollection(), new InterarchyBookmarkCollection(), new CrossFtpBookmarkCollection(), new FireFtpBookmarkCollection(),
                        new Expandrive3BookmarkCollection(), new Expandrive4BookmarkCollection(), new Expandrive5BookmarkCollection());
            }
        });
        if(updater.hasUpdatePrivileges()) {
            final long next = preferences.getLong("update.check.timestamp") + preferences.getLong("update.check.interval") * 1000;
            if(next < System.currentTimeMillis()) {
                updater.check(true);
            }
            updater.register();
        }
    }

    /**
     * NSService implementation
     */
    public void serviceUploadFileUrl_(final NSPasteboard pboard, final String userData) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("serviceUploadFileUrl_: with user data %s", userData));
        }
        if(pboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
            NSObject o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
            if(o != null) {
                if(o.isKindOfClass(Rococoa.createClass("NSArray", NSArray._Class.class))) {
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
     * Saved browsers
     */
    private AbstractHostCollection sessions = new FolderBookmarkCollection(
            LocalFactory.get(preferences.getProperty("application.support.path"), "Sessions"), "session");

    /**
     * Display donation reminder dialog
     */
    private boolean displayDonationPrompt = true;

    @Outlet
    private WindowController donationController;

    /**
     * Invoked from within the terminate method immediately before the
     * application terminates. sender is the NSApplication to be terminated.
     * If this method returns false, the application is not terminated,
     * and control returns to the main event loop.
     *
     * @param app Application instance
     * @return Return true to allow the application to terminate.
     */
    @Override
    public NSUInteger applicationShouldTerminate(final NSApplication app) {
        if(log.isDebugEnabled()) {
            log.debug("Application should quit with notification");
        }
        // Determine if there are any running transfers
        final NSUInteger result = TransferControllerFactory.applicationShouldTerminate(app);
        if(!result.equals(NSApplication.NSTerminateNow)) {
            return result;
        }
        // Determine if there are any open connections
        for(BrowserController browser : MainController.getBrowsers()) {
            if(preferences.getBoolean("browser.serialize")) {
                if(browser.isMounted()) {
                    // The workspace should be saved. Serialize all open browser sessions
                    final Host serialized
                            = new HostDictionary().deserialize(browser.getSession().getHost().serialize(SerializerFactory.get()));
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
                    int choice = alert.runModal(); //alternate
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
        if(log.isDebugEnabled()) {
            log.debug("applicationShouldTerminateAfterDonationPrompt");
        }
        if(!displayDonationPrompt) {
            // Already displayed
            return NSApplication.NSTerminateNow;
        }
        final License l = LicenseFactory.find();
        if(!l.verify()) {
            final String lastversion = preferences.getProperty("donate.reminder");
            if(NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleShortVersionString").toString().equals(lastversion)) {
                // Do not display if same version is installed
                return NSApplication.NSTerminateNow;
            }
            final Calendar nextreminder = Calendar.getInstance();
            nextreminder.setTimeInMillis(preferences.getLong("donate.reminder.date"));
            // Display donationPrompt every n days
            nextreminder.add(Calendar.DAY_OF_YEAR, preferences.getInteger("y"));
            if(log.isDebugEnabled()) {
                log.debug(String.format("Next reminder %s", nextreminder.getTime().toString()));
            }
            // Display after upgrade
            if(nextreminder.getTime().after(new Date(System.currentTimeMillis()))) {
                // Do not display if shown in the reminder interval
                return NSApplication.NSTerminateNow;
            }
            // Make sure prompt is not loaded twice upon next quit event
            displayDonationPrompt = false;
            final int uses = preferences.getInteger("uses");
            donationController = new WindowController() {
                @Override
                protected String getBundleName() {
                    return "Donate";
                }

                @Outlet
                private NSButton neverShowDonationCheckbox;

                public void setNeverShowDonationCheckbox(NSButton neverShowDonationCheckbox) {
                    this.neverShowDonationCheckbox = neverShowDonationCheckbox;
                    this.neverShowDonationCheckbox.setTarget(this.id());
                    this.neverShowDonationCheckbox.setState(
                            preferences.getProperty("donate.reminder").equals(
                                    NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleShortVersionString").toString())
                                    ? NSCell.NSOnState : NSCell.NSOffState
                    );
                }

                @Override
                public void awakeFromNib() {
                    this.window().setTitle(this.window().title() + " (" + uses + ")");
                    this.window().center();
                    this.window().makeKeyAndOrderFront(null);

                    super.awakeFromNib();
                }

                public void closeDonationSheet(final NSButton sender) {
                    if(sender.tag() == SheetCallback.DEFAULT_OPTION) {
                        BrowserLauncherFactory.get().open(preferences.getProperty("website.donate"));
                    }
                    this.terminate();
                }

                @Override
                public void windowWillClose(NSNotification notification) {
                    this.terminate();
                    super.windowWillClose(notification);
                }

                private void terminate() {
                    if(neverShowDonationCheckbox.state() == NSCell.NSOnState) {
                        preferences.setProperty("donate.reminder",
                                NSBundle.mainBundle().infoDictionary().objectForKey("CFBundleShortVersionString").toString());
                    }
                    // Remember this reminder date
                    preferences.setProperty("donate.reminder.date", System.currentTimeMillis());
                    // Quit again
                    app.replyToApplicationShouldTerminate(true);
                }
            };
            donationController.loadBundle();
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
        if(log.isDebugEnabled()) {
            log.debug(String.format("Application will quit with notification %s", notification));
        }
        this.invalidate();

        // Clear temporary files
        TemporaryFileServiceFactory.get().shutdown();

        //Terminating rendezvous discovery
        RendezvousFactory.instance().quit();

        // Disable update
        updater.unregister();

        //Writing usage info
        preferences.setProperty("uses", preferences.getInteger("uses") + 1);
        preferences.save();
    }

    public void applicationWillRestartAfterUpdate(ID updater) {
        // Disable donation prompt after udpate install
        displayDonationPrompt = false;
    }

    /**
     * Posted when the user has requested a logout or that the machine be powered off.
     *
     * @param notification Notification name
     */
    public void workspaceWillPowerOff(NSNotification notification) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Workspace will power off with notification %s", notification));
        }
    }

    /**
     * Posted before a user session is switched out. This allows an application to
     * disable some processing when its user session is switched out, and reenable when that
     * session gets switched back in, for example.
     *
     * @param notification Notification name
     */
    public void workspaceWillLogout(NSNotification notification) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Workspace will logout with notification %s", notification));
        }
    }

    public void workspaceWillSleep(NSNotification notification) {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Workspace will sleep with notification %s", notification));
        }
    }

    /**
     * Makes a unmounted browser window the key window and brings it to the front
     *
     * @return A reference to a browser window
     */
    public static BrowserController newDocument() {
        return MainController.newDocument(false);
    }

    /**
     *
     */
    private static List<BrowserController> browsers
            = new ArrayList<BrowserController>();

    public static List<BrowserController> getBrowsers() {
        return browsers;
    }

    /**
     * Browser with key focus
     *
     * @return Null if no browser window is open
     */
    public static BrowserController getBrowser() {
        for(BrowserController browser : MainController.getBrowsers()) {
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

    /**
     * @param frame Frame autosave name
     */
    public static BrowserController newDocument(final boolean force, final String frame) {
        final List<BrowserController> browsers = MainController.getBrowsers();
        if(!force) {
            for(BrowserController controller : browsers) {
                if(controller.getSession() == null) {
                    controller.window().makeKeyAndOrderFront(null);
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
        controller.window().makeKeyAndOrderFront(null);
        browsers.add(controller);
        return controller;
    }

    /**
     * We are not a Windows application. Long live the application wide menu bar.
     */
    @Override
    public boolean applicationShouldTerminateAfterLastWindowClosed(NSApplication app) {
        return false;
    }

    @Override
    protected String getBundleName() {
        return "Main";
    }

}