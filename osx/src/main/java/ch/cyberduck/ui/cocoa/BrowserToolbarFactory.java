package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSButtonCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenu;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.DefaultCharsetProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;
import ch.cyberduck.ui.cocoa.quicklook.QuickLook;
import ch.cyberduck.ui.cocoa.quicklook.QuickLookFactory;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.HashMap;
import java.util.Map;

public class BrowserToolbarFactory implements ToolbarFactory {

    protected static final String TOOLBAR_NEW_CONNECTION = "New Connection";
    protected static final String TOOLBAR_BROWSER_VIEW = "Browser View";
    protected static final String TOOLBAR_TRANSFERS = "Transfers";
    protected static final String TOOLBAR_QUICK_CONNECT = "Quick Connect";
    protected static final String TOOLBAR_TOOLS = "Tools";
    protected static final String TOOLBAR_REFRESH = "Refresh";
    protected static final String TOOLBAR_ENCODING = "Encoding";
    protected static final String TOOLBAR_SYNCHRONIZE = "Synchronize";
    protected static final String TOOLBAR_DOWNLOAD = "Download";
    protected static final String TOOLBAR_UPLOAD = "Upload";
    protected static final String TOOLBAR_EDIT = "Edit";
    protected static final String TOOLBAR_DELETE = "Delete";
    protected static final String TOOLBAR_NEW_FOLDER = "New Folder";
    protected static final String TOOLBAR_NEW_BOOKMARK = "New Bookmark";
    protected static final String TOOLBAR_GET_INFO = "Get Info";
    protected static final String TOOLBAR_WEBVIEW = "Open";
    protected static final String TOOLBAR_DISCONNECT = "Disconnect";
    protected static final String TOOLBAR_TERMINAL = "Terminal";
    protected static final String TOOLBAR_ARCHIVE = "Archive";
    protected static final String TOOLBAR_QUICKLOOK = "Quick Look";
    protected static final String TOOLBAR_LOG = "Log";

    private Preferences preferences
            = PreferencesFactory.get();

    private final QuickLook quicklook = QuickLookFactory.get();

    private BrowserController controller;

    @Outlet
    private NSButton quicklookButton;

    /**
     * Keep reference to weak toolbar items. A toolbar may ask again for a kind of toolbar
     * item already supplied to it, in which case this method may return the same toolbar
     * item it returned before
     */
    private Map<String, NSToolbarItem> toolbarItems
            = new HashMap<String, NSToolbarItem>();


    public BrowserToolbarFactory(final BrowserController controller) {
        this.controller = controller;
    }

    @Override
    public NSToolbarItem create(final String itemIdentifier) {
        if(!toolbarItems.containsKey(itemIdentifier)) {
            toolbarItems.put(itemIdentifier, NSToolbarItem.itemWithIdentifier(itemIdentifier));
        }
        final NSToolbarItem item = toolbarItems.get(itemIdentifier);
        switch(itemIdentifier) {
            case TOOLBAR_BROWSER_VIEW:
                item.setLabel(LocaleFactory.localizedString("View"));
                item.setPaletteLabel(LocaleFactory.localizedString("View"));
                item.setToolTip(LocaleFactory.localizedString("Switch Browser View"));
                item.setView(controller.getBrowserSwitchView());
                // Add a menu representation for text mode of toolbar
                NSMenuItem viewMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString("View"), null, StringUtils.EMPTY);
                NSMenu viewSubmenu = NSMenu.menu();
                viewSubmenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("List"),
                        Foundation.selector("browserSwitchMenuClicked:"), StringUtils.EMPTY);
                viewSubmenu.itemWithTitle(LocaleFactory.localizedString("List")).setTag(0);
                viewSubmenu.addItemWithTitle_action_keyEquivalent(LocaleFactory.localizedString("Outline"),
                        Foundation.selector("browserSwitchMenuClicked:"), StringUtils.EMPTY);
                viewSubmenu.itemWithTitle(LocaleFactory.localizedString("Outline")).setTag(1);
                viewMenu.setSubmenu(viewSubmenu);
                item.setMenuFormRepresentation(viewMenu);
                return item;
            case TOOLBAR_NEW_CONNECTION:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_NEW_CONNECTION));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_NEW_CONNECTION));
                item.setToolTip(LocaleFactory.localizedString("Connect to server"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("connect.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("connectButtonClicked:"));
                return item;
            case TOOLBAR_TRANSFERS:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_TRANSFERS));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_TRANSFERS));
                item.setToolTip(LocaleFactory.localizedString("Show Transfers window"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("queue.tiff", 32));
                item.setAction(Foundation.selector("showTransferQueueClicked:"));
                return item;
            case TOOLBAR_TOOLS:
                item.setLabel(LocaleFactory.localizedString("Action"));
                item.setPaletteLabel(LocaleFactory.localizedString("Action"));
                final NSInteger index = new NSInteger(0);
                controller.getActionPopupButton().insertItemWithTitle_atIndex(StringUtils.EMPTY, index);
                controller.getActionPopupButton().itemAtIndex(index).setImage(IconCacheFactory.<NSImage>get().iconNamed("gear.tiff"));
                item.setView(controller.getActionPopupButton());
                // Add a menu representation for text mode of toolbar
                NSMenuItem toolMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString("Action"), null, StringUtils.EMPTY);
                NSMenu toolSubmenu = NSMenu.menu();
                for(int i = 1; i < controller.getActionPopupButton().menu().numberOfItems().intValue(); i++) {
                    NSMenuItem template = controller.getActionPopupButton().menu().itemAtIndex(new NSInteger(i));
                    toolSubmenu.addItem(NSMenuItem.itemWithTitle(template.title(),
                            template.action(),
                            template.keyEquivalent()));
                }
                toolMenu.setSubmenu(toolSubmenu);
                item.setMenuFormRepresentation(toolMenu);
                return item;
            case TOOLBAR_QUICK_CONNECT:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_QUICK_CONNECT));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_QUICK_CONNECT));
                item.setToolTip(LocaleFactory.localizedString("Connect to server"));
                item.setView(controller.getQuickConnectPopup());
                return item;
            case TOOLBAR_ENCODING:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_ENCODING));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_ENCODING));
                item.setToolTip(LocaleFactory.localizedString("Character Encoding"));
                item.setView(controller.getEncodingPopup());
                // Add a menu representation for text mode of toolbar
                NSMenuItem encodingMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString(TOOLBAR_ENCODING),
                        Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
                String[] charsets = new DefaultCharsetProvider().availableCharsets();
                NSMenu charsetMenu = NSMenu.menu();
                for(String charset : charsets) {
                    charsetMenu.addItemWithTitle_action_keyEquivalent(charset, Foundation.selector("encodingMenuClicked:"), StringUtils.EMPTY);
                }
                encodingMenu.setSubmenu(charsetMenu);
                item.setMenuFormRepresentation(encodingMenu);
                item.setMinSize(controller.getEncodingPopup().frame().size);
                item.setMaxSize(controller.getEncodingPopup().frame().size);
                return item;
            case TOOLBAR_REFRESH:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_REFRESH));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_REFRESH));
                item.setToolTip(LocaleFactory.localizedString("Refresh directory listing"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("reload.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("reloadButtonClicked:"));
                return item;
            case TOOLBAR_DOWNLOAD:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_DOWNLOAD));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DOWNLOAD));
                item.setToolTip(LocaleFactory.localizedString("Download file"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("download.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("downloadButtonClicked:"));
                return item;
            case TOOLBAR_UPLOAD:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_UPLOAD));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_UPLOAD));
                item.setToolTip(LocaleFactory.localizedString("Upload local file to the remote host"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("upload.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("uploadButtonClicked:"));
                return item;
            case TOOLBAR_SYNCHRONIZE:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_SYNCHRONIZE));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_SYNCHRONIZE));
                item.setToolTip(LocaleFactory.localizedString("Synchronize files"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("sync.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("syncButtonClicked:"));
                return item;
            case TOOLBAR_GET_INFO:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_GET_INFO));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_GET_INFO));
                item.setToolTip(LocaleFactory.localizedString("Show file attributes"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("info.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("infoButtonClicked:"));
                return item;
            case TOOLBAR_WEBVIEW:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_WEBVIEW));
                item.setPaletteLabel(LocaleFactory.localizedString("Open in Web Browser"));
                item.setToolTip(LocaleFactory.localizedString("Open in Web Browser"));
                final Application browser = SchemeHandlerFactory.get().getDefaultHandler(Scheme.http);
                if(Application.notfound.equals(browser)) {
                    item.setEnabled(false);
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed("notfound.tiff", 32));
                }
                else {
                    item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(browser, 32));
                }
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("openBrowserButtonClicked:"));
                return item;
            case TOOLBAR_EDIT:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_EDIT));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_EDIT));
                item.setToolTip(LocaleFactory.localizedString("Edit file in external editor"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("pencil.tiff"));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("editButtonClicked:"));
                // Add a menu representation for text mode of toolbar
                NSMenuItem toolbarMenu = NSMenuItem.itemWithTitle(LocaleFactory.localizedString(TOOLBAR_EDIT),
                        Foundation.selector("editButtonClicked:"), StringUtils.EMPTY);
                NSMenu editMenu = NSMenu.menu();
                editMenu.setAutoenablesItems(true);
                editMenu.setDelegate(controller.getEditMenuDelegate().id());
                toolbarMenu.setSubmenu(editMenu);
                item.setMenuFormRepresentation(toolbarMenu);
                return item;
            case TOOLBAR_DELETE:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_DELETE));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DELETE));
                item.setToolTip(LocaleFactory.localizedString("Delete file"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("delete.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("deleteFileButtonClicked:"));
                return item;
            case TOOLBAR_NEW_FOLDER:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_NEW_FOLDER));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_NEW_FOLDER));
                item.setToolTip(LocaleFactory.localizedString("Create New Folder"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("newfolder.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("createFolderButtonClicked:"));
                return item;
            case TOOLBAR_NEW_BOOKMARK:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_NEW_BOOKMARK));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_NEW_BOOKMARK));
                item.setToolTip(LocaleFactory.localizedString("New Bookmark"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("bookmark", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("addBookmarkButtonClicked:"));
                return item;
            case TOOLBAR_DISCONNECT:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_DISCONNECT));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_DISCONNECT));
                item.setToolTip(LocaleFactory.localizedString("Disconnect from server"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("eject.tiff", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("disconnectButtonClicked:"));
                return item;
            case TOOLBAR_TERMINAL:
                final ApplicationFinder finder = ApplicationFinderFactory.get();
                final Application application
                        = finder.getDescription(preferences.getProperty("terminal.bundle.identifier"));
                item.setLabel(application.getName());
                item.setPaletteLabel(application.getName());
                item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(application, 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("openTerminalButtonClicked:"));
                return item;
            case TOOLBAR_ARCHIVE:
                item.setLabel(LocaleFactory.localizedString("Archive", "Archive"));
                item.setPaletteLabel(LocaleFactory.localizedString("Archive", "Archive"));
                item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(new Application("com.apple.archiveutility"), 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("archiveButtonClicked:"));
                return item;
            case TOOLBAR_QUICKLOOK:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_QUICKLOOK));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_QUICKLOOK));
                if(quicklook.isAvailable()) {
                    quicklookButton = NSButton.buttonWithFrame(new NSRect(29, 23));
                    quicklookButton.setBezelStyle(NSButtonCell.NSTexturedRoundedBezelStyle);
                    quicklookButton.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSQuickLookTemplate"));
                    quicklookButton.sizeToFit();
                    quicklookButton.setTarget(controller.id());
                    quicklookButton.setAction(Foundation.selector("quicklookButtonClicked:"));
                    item.setView(quicklookButton);
                }
                else {
                    item.setEnabled(false);
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed("notfound.tiff", 32));
                }
                return item;
            case TOOLBAR_LOG:
                item.setLabel(LocaleFactory.localizedString(TOOLBAR_LOG));
                item.setPaletteLabel(LocaleFactory.localizedString(TOOLBAR_LOG));
                item.setToolTip(LocaleFactory.localizedString("Toggle Log Drawer"));
                item.setImage(IconCacheFactory.<NSImage>get().iconNamed("log", 32));
                item.setTarget(controller.id());
                item.setAction(Foundation.selector("toggleLogDrawer:"));
                return item;
        }
        // Returning null will inform the toolbar this kind of item is not supported.
        return null;
    }

    @Override
    public NSArray getDefault() {
        return NSArray.arrayWithObjects(
                TOOLBAR_NEW_CONNECTION,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                TOOLBAR_QUICK_CONNECT,
                TOOLBAR_TOOLS,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                TOOLBAR_REFRESH,
                TOOLBAR_EDIT,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier,
                TOOLBAR_DISCONNECT
        );
    }

    @Override
    public NSArray getAllowed() {
        return NSArray.arrayWithObjects(
                TOOLBAR_NEW_CONNECTION,
                TOOLBAR_BROWSER_VIEW,
                TOOLBAR_TRANSFERS,
                TOOLBAR_QUICK_CONNECT,
                TOOLBAR_TOOLS,
                TOOLBAR_REFRESH,
                TOOLBAR_ENCODING,
                TOOLBAR_SYNCHRONIZE,
                TOOLBAR_DOWNLOAD,
                TOOLBAR_UPLOAD,
                TOOLBAR_EDIT,
                TOOLBAR_DELETE,
                TOOLBAR_NEW_FOLDER,
                TOOLBAR_NEW_BOOKMARK,
                TOOLBAR_GET_INFO,
                TOOLBAR_WEBVIEW,
                TOOLBAR_TERMINAL,
                TOOLBAR_ARCHIVE,
                TOOLBAR_QUICKLOOK,
                TOOLBAR_LOG,
                TOOLBAR_DISCONNECT,
                NSToolbarItem.NSToolbarCustomizeToolbarItemIdentifier,
                NSToolbarItem.NSToolbarSpaceItemIdentifier,
                NSToolbarItem.NSToolbarSeparatorItemIdentifier,
                NSToolbarItem.NSToolbarFlexibleSpaceItemIdentifier
        );
    }
}
