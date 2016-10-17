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
import ch.cyberduck.binding.BundleController;
import ch.cyberduck.binding.Delegate;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.SheetController;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSColor;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.WindowListener;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSBundle;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.DefaultCharsetProvider;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.local.ApplicationLauncherFactory;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.updater.PeriodicUpdateChecker;
import ch.cyberduck.core.updater.PeriodicUpdateCheckerFactory;
import ch.cyberduck.ui.browser.Column;
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
import org.rococoa.cocoa.foundation.NSInteger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Setting the main menu and implements application delegate methods
 */
public class MainController extends BundleController {
    private static final Logger log = Logger.getLogger(MainController.class);

    private final Preferences preferences = PreferencesFactory.get();

    private final PeriodicUpdateChecker updater
            = PeriodicUpdateCheckerFactory.get();

    /**
     *
     */
    private static final List<BrowserController> browsers
            = new ArrayList<BrowserController>();

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

    public void setEncodingMenu(NSMenu encodingMenu) {
        this.encodingMenu = encodingMenu;
        for(String charset : new DefaultCharsetProvider().availableCharsets()) {
            this.encodingMenu.addItemWithTitle_action_keyEquivalent(charset, Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
        }
    }

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
        this.openDefaultBookmark(this.newDocument(true));
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
}