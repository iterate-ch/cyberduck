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
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.ToolbarWindowController;
import ch.cyberduck.binding.application.*;
import ch.cyberduck.binding.foundation.NSAppleScript;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.binding.foundation.NSAttributedString;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSNotificationCenter;
import ch.cyberduck.binding.foundation.NSRange;
import ch.cyberduck.core.*;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.formatter.SizeFormatterFactory;
import ch.cyberduck.core.kms.KMSEncryptionFeature;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.s3.S3EncryptionFeature;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sparkle.Updater;
import ch.cyberduck.core.threading.WindowMainAction;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.urlhandler.SchemeHandlerFactory;
import ch.cyberduck.ui.cocoa.view.BookmarkCell;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.model.S3Object;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;
import org.rococoa.cocoa.foundation.NSUInteger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class PreferencesController extends ToolbarWindowController {
    private static final Logger log = Logger.getLogger(PreferencesController.class);

    private final NSNotificationCenter notificationCenter
        = NSNotificationCenter.defaultCenter();

    private final Preferences preferences
        = PreferencesFactory.get();

    @Override
    protected String getBundleName() {
        return "Preferences";
    }

    @Outlet
    private NSView panelGeneral;
    @Outlet
    private NSView panelEditor;
    @Outlet
    private NSView panelBrowser;
    @Outlet
    private NSView panelTransfer;
    @Outlet
    private NSView panelFTP;
    @Outlet
    private NSView panelSFTP;
    @Outlet
    private NSView panelS3;
    @Outlet
    private NSView panelBandwidth;
    @Outlet
    private NSView panelAdvanced;
    @Outlet
    private NSView panelUpdate;
    @Outlet
    private NSView panelLanguage;
    @Outlet
    private NSView panelCryptomator;

    public void setPanelUpdate(NSView v) {
        this.panelUpdate = v;
    }

    public void setPanelAdvanced(NSView v) {
        this.panelAdvanced = v;
    }

    public void setPanelBandwidth(NSView v) {
        this.panelBandwidth = v;
    }

    public void setPanelSFTP(NSView v) {
        this.panelSFTP = v;
    }

    public void setPanelFTP(NSView v) {
        this.panelFTP = v;
    }

    public void setPanelS3(NSView v) {
        this.panelS3 = v;
    }

    public void setPanelTransfer(NSView v) {
        this.panelTransfer = v;
    }

    public void setPanelBrowser(NSView v) {
        this.panelBrowser = v;
    }

    public void setPanelGeneral(NSView v) {
        this.panelGeneral = v;
    }

    public void setPanelEditor(NSView v) {
        this.panelEditor = v;
    }

    public void setPanelLanguage(NSView v) {
        this.panelLanguage = v;
    }

    public void setPanelCryptomator(final NSView v) {
        this.panelCryptomator = v;
    }

    @Override
    public NSToolbarItem toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(final NSToolbar toolbar, final String itemIdentifier, final boolean flag) {
        final NSToolbarItem item = super.toolbar_itemForItemIdentifier_willBeInsertedIntoToolbar(toolbar, itemIdentifier, flag);
        if(itemIdentifier.equals("sftp")) {
            item.setImage(IconCacheFactory.<NSImage>get().iconNamed("ftp"));
        }
        return item;
    }

    @Override
    protected List<NSView> getPanels() {
        final List<String> identifiers = this.getPanelIdentifiers();
        final List<NSView> views = new ArrayList<NSView>();
        if(identifiers.contains(PreferencesToolbarItem.general.name())) {
            views.add(panelGeneral);
        }
        if(identifiers.contains(PreferencesToolbarItem.browser.name())) {
            views.add(panelBrowser);
        }
        if(identifiers.contains(PreferencesToolbarItem.queue.name())) {
            views.add(panelTransfer);
        }
        if(identifiers.contains(PreferencesToolbarItem.pencil.name())) {
            views.add(panelEditor);
        }
        if(identifiers.contains(PreferencesToolbarItem.ftp.name())) {
            views.add(panelFTP);
        }
        if(identifiers.contains(PreferencesToolbarItem.sftp.name())) {
            views.add(panelSFTP);
        }
        if(identifiers.contains(PreferencesToolbarItem.s3.name())) {
            views.add(panelS3);
        }
        if(identifiers.contains(PreferencesToolbarItem.bandwidth.name())) {
            views.add(panelBandwidth);
        }
        if(identifiers.contains(PreferencesToolbarItem.connection.name())) {
            views.add(panelAdvanced);
        }
        if(identifiers.contains(PreferencesToolbarItem.cryptomator.name())) {
            views.add(panelCryptomator);
        }
        if(identifiers.contains(PreferencesToolbarItem.update.name())) {
            if(null != Updater.getFeed()) {
                views.add(panelUpdate);
            }
        }
        if(identifiers.contains(PreferencesToolbarItem.language.name())) {
            views.add(panelLanguage);
        }
        return views;
    }

    @Override
    protected List<String> getPanelIdentifiers() {
        final List<String> views = new ArrayList<>(Arrays.asList(
            PreferencesToolbarItem.general.name(),
            PreferencesToolbarItem.browser.name(),
            PreferencesToolbarItem.queue.name(),
            PreferencesToolbarItem.pencil.name(),
            PreferencesToolbarItem.ftp.name(),
            PreferencesToolbarItem.sftp.name(),
            PreferencesToolbarItem.s3.name(),
            PreferencesToolbarItem.bandwidth.name(),
            PreferencesToolbarItem.connection.name(),
            PreferencesToolbarItem.cryptomator.name())
        );
        if(null != Updater.getFeed()) {
            views.add(PreferencesToolbarItem.update.name());
        }
        views.add(PreferencesToolbarItem.language.name());
        return views;
    }

    protected enum PreferencesToolbarItem {
        general,
        browser,
        queue,
        pencil,
        ftp,
        sftp,
        s3,
        bandwidth,
        connection,
        cryptomator,
        update,
        language
    }

    @Override
    protected void initializePanel(final String identifier) {
        PreferencesToolbarItem item;
        try {
            item = PreferencesToolbarItem.valueOf(identifier);
        }
        catch(IllegalArgumentException e) {
            item = PreferencesToolbarItem.general;
        }
        switch(item) {
            case general:
                break;
            case queue:
                this.chmodDownloadTypePopupChanged(this.chmodDownloadTypePopup);
                this.chmodUploadTypePopupChanged(this.chmodUploadTypePopup);
                break;
            case browser:
                break;
            case pencil:
                this.updateEditorCombobox();
                break;
            case ftp:
                this.configureDefaultProtocolHandlerCombobox(this.defaultFTPHandlerCombobox, Scheme.ftp);
                break;
            case sftp:
                this.configureDefaultProtocolHandlerCombobox(this.defaultSFTPHandlerCombobox, Scheme.sftp);
                break;
        }
    }

    @Override
    public void invalidate() {
        FolderBookmarkCollection.favoritesCollection().removeListener(bookmarkCollectionListener);
        super.invalidate();
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setExcludedFromWindowsMenu(true);
        window.setFrameAutosaveName("Preferences");
        window.setContentMinSize(window.frame().size);
        window.setContentMaxSize(new NSSize(800, window.frame().size.height.doubleValue()));
        super.setWindow(window);
    }

    @Override
    public void awakeFromNib() {
        this.window.center();

        super.awakeFromNib();
    }

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    @Outlet
    private NSPopUpButton editorCombobox;

    public void setEditorCombobox(NSPopUpButton b) {
        this.editorCombobox = b;
        this.editorCombobox.setAutoenablesItems(false);
    }

    private void updateEditorCombobox() {
        editorCombobox.removeAllItems();
        for(Application editor : EditorFactory.instance().getEditors()) {
            editorCombobox.addItemWithTitle(editor.getName());
            editorCombobox.lastItem().setRepresentedObject(editor.getIdentifier());
            final boolean enabled = ApplicationFinderFactory.get().isInstalled(editor);
            editorCombobox.lastItem().setEnabled(enabled);
            if(enabled) {
                editorCombobox.lastItem().setImage(
                    IconCacheFactory.<NSImage>get().applicationIcon(editor, 16));
            }
            if(editor.equals(EditorFactory.instance().getDefaultEditor())) {
                editorCombobox.selectItem(editorCombobox.lastItem());
            }
        }
        editorCombobox.setTarget(this.id());
        editorCombobox.setAction(Foundation.selector("editorComboboxClicked:"));
        editorCombobox.menu().addItem(NSMenuItem.separatorItem());
        editorCombobox.addItemWithTitle(String.format("%s…", LocaleFactory.localizedString("Choose")));
    }

    @Outlet
    private NSOpenPanel editorPathPanel;
    private final ProxyController editorPathPanelDelegate = new EditorOpenPanelDelegate();

    @Action
    public void editorComboboxClicked(NSPopUpButton sender) {
        if(null == sender.selectedItem().representedObject()) {
            editorPathPanel = NSOpenPanel.openPanel();
            editorPathPanel.setDelegate(editorPathPanelDelegate.id());
            editorPathPanel.setAllowsMultipleSelection(false);
            editorPathPanel.setCanCreateDirectories(false);
            editorPathPanel.beginSheetForDirectory("/Applications", null, this.window, this.id(),
                Foundation.selector("editorPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            preferences.setProperty("editor.bundleIdentifier", sender.selectedItem().representedObject());
            for(BrowserController controller : MainController.getBrowsers()) {
                controller.validateToolbar();
            }
        }
    }

    private static class EditorOpenPanelDelegate extends ProxyController {
        public boolean panel_shouldShowFilename(ID panel, String path) {
            final Local f = LocalFactory.get(path);
            if(f.isDirectory()) {
                return true;
            }
            final String extension = f.getExtension();
            if(StringUtils.isEmpty(extension)) {
                return false;
            }
            return extension.equals("app");
        }
    }

    public void editorPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            NSArray selected = sheet.filenames();
            String filename;
            if((filename = selected.lastObject().toString()) != null) {
                final String path = LocalFactory.get(filename).getAbsolute();
                final ApplicationFinder finder = ApplicationFinderFactory.get();
                final Application application = finder.getDescription(path);
                if(finder.isInstalled(application)) {
                    preferences.setProperty("editor.bundleIdentifier", application.getIdentifier());
                    for(BrowserController controller : MainController.getBrowsers()) {
                        controller.validateToolbar();
                    }
                }
                else {
                    log.error(String.format("Loading bundle %s failed", path));
                }
            }
        }
        this.updateEditorCombobox();
    }

    @Outlet
    private NSButton defaultEditorCheckbox;

    public void setDefaultEditorCheckbox(NSButton b) {
        this.defaultEditorCheckbox = b;
        this.defaultEditorCheckbox.setTarget(this.id());
        this.defaultEditorCheckbox.setAction(Foundation.selector("defaultEditorCheckboxClicked:"));
        this.defaultEditorCheckbox.setState(preferences.getBoolean("editor.alwaysUseDefault") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    public void defaultEditorCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("editor.alwaysUseDefault", enabled);
    }

    @Outlet
    private NSPopUpButton bookmarkSizePopup;

    public void setBookmarkSizePopup(NSPopUpButton b) {
        this.bookmarkSizePopup = b;
        this.bookmarkSizePopup.setTarget(this.id());
        this.bookmarkSizePopup.setAction(Foundation.selector("bookmarkSizePopupClicked:"));
        final int size = preferences.getInteger("bookmark.icon.size");
        for(int i = 0; i < this.bookmarkSizePopup.numberOfItems().intValue(); i++) {
            this.bookmarkSizePopup.itemAtIndex(new NSInteger(i)).setState(NSCell.NSOffState);
        }
        if(BookmarkCell.SMALL_BOOKMARK_SIZE == size) {
            this.bookmarkSizePopup.selectItemAtIndex(new NSInteger(0));
        }
        if(BookmarkCell.MEDIUM_BOOKMARK_SIZE == size) {
            this.bookmarkSizePopup.selectItemAtIndex(new NSInteger(1));
        }
        if(BookmarkCell.LARGE_BOOKMARK_SIZE == size) {
            this.bookmarkSizePopup.selectItemAtIndex(new NSInteger(2));
        }
    }

    @Action
    public void bookmarkSizePopupClicked(NSPopUpButton sender) {
        if(sender.indexOfSelectedItem().intValue() == 0) {
            preferences.setProperty("bookmark.icon.size", BookmarkCell.SMALL_BOOKMARK_SIZE);
        }
        if(sender.indexOfSelectedItem().intValue() == 1) {
            preferences.setProperty("bookmark.icon.size", BookmarkCell.MEDIUM_BOOKMARK_SIZE);
        }
        if(sender.indexOfSelectedItem().intValue() == 2) {
            preferences.setProperty("bookmark.icon.size", BookmarkCell.LARGE_BOOKMARK_SIZE);
        }
        BrowserController.updateBookmarkTableRowHeight();
    }

    @Outlet
    private NSButton openUntitledBrowserCheckbox;

    public void setOpenUntitledBrowserCheckbox(NSButton b) {
        this.openUntitledBrowserCheckbox = b;
        this.openUntitledBrowserCheckbox.setTarget(this.id());
        this.openUntitledBrowserCheckbox.setAction(Foundation.selector("openUntitledBrowserCheckboxClicked:"));
        this.openUntitledBrowserCheckbox.setState(preferences.getBoolean("browser.open.untitled") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void openUntitledBrowserCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.open.untitled", enabled);
    }

    @Outlet
    private NSButton browserSerializeCheckbox;

    public void setBrowserSerializeCheckbox(NSButton b) {
        this.browserSerializeCheckbox = b;
        this.browserSerializeCheckbox.setTarget(this.id());
        this.browserSerializeCheckbox.setAction(Foundation.selector("browserSerializeCheckboxClicked:"));
        this.browserSerializeCheckbox.setState(preferences.getBoolean("browser.serialize") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void browserSerializeCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.serialize", enabled);
    }

    @Outlet
    private NSPopUpButton defaultBookmarkCombobox;

    private final CollectionListener<Host> bookmarkCollectionListener = new AbstractCollectionListener<Host>() {
        @Override
        public void collectionItemAdded(final Host bookmark) {
            invoke(new WindowMainAction(PreferencesController.this) {
                @Override
                public void run() {
                    defaultBookmarkCombobox.addItemWithTitle(BookmarkNameProvider.toString(bookmark));
                    defaultBookmarkCombobox.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed("cyberduck-document.icns", 16));
                    defaultBookmarkCombobox.lastItem().setRepresentedObject(bookmark.getUuid());
                }
            });
        }

        @Override
        public void collectionItemRemoved(final Host bookmark) {
            invoke(new WindowMainAction(PreferencesController.this) {
                @Override
                public void run() {
                    String selected = defaultBookmarkCombobox.selectedItem().representedObject();
                    if(StringUtils.isNotEmpty(selected)) {
                        if(selected.equals(bookmark.getUuid())) {
                            preferences.deleteProperty("browser.open.bookmark.default");
                        }
                    }
                    NSInteger i = defaultBookmarkCombobox.menu().indexOfItemWithRepresentedObject(bookmark.getUuid());
                    if(i.intValue() == -1) {
                        return;
                    }
                    defaultBookmarkCombobox.removeItemAtIndex(i);
                }
            });
        }
    };

    public void setDefaultBookmarkCombobox(NSPopUpButton b) {
        this.defaultBookmarkCombobox = b;
        this.defaultBookmarkCombobox.setToolTip(LocaleFactory.localizedString("Bookmarks", "Preferences"));
        this.defaultBookmarkCombobox.removeAllItems();
        this.defaultBookmarkCombobox.addItemWithTitle(LocaleFactory.localizedString("None"));
        this.defaultBookmarkCombobox.selectItem(this.defaultBookmarkCombobox.lastItem());
        this.defaultBookmarkCombobox.menu().addItem(NSMenuItem.separatorItem());
        for(Host bookmark : FolderBookmarkCollection.favoritesCollection()) {
            this.defaultBookmarkCombobox.addItemWithTitle(BookmarkNameProvider.toString(bookmark));
            this.defaultBookmarkCombobox.lastItem().setImage(
                IconCacheFactory.<NSImage>get().iconNamed(bookmark.getProtocol().icon(), 16));
            this.defaultBookmarkCombobox.lastItem().setRepresentedObject(bookmark.getUuid());
            if(bookmark.getUuid().equals(preferences.getProperty("browser.open.bookmark.default"))) {
                this.defaultBookmarkCombobox.selectItem(this.defaultBookmarkCombobox.lastItem());
            }
        }
        FolderBookmarkCollection.favoritesCollection().addListener(bookmarkCollectionListener);
        this.defaultBookmarkCombobox.setTarget(this.id());
        final Selector action = Foundation.selector("defaultBookmarkComboboxClicked:");
        this.defaultBookmarkCombobox.setAction(action);
    }

    @Action
    public void defaultBookmarkComboboxClicked(NSPopUpButton sender) {
        final String selected = sender.selectedItem().representedObject();
        if(null == selected) {
            preferences.deleteProperty("browser.open.bookmark.default");
        }
        preferences.setProperty("browser.open.bookmark.default", selected);
    }

    @Outlet
    private NSPopUpButton encodingCombobox;

    public void setEncodingCombobox(NSPopUpButton b) {
        this.encodingCombobox = b;
        this.encodingCombobox.setTarget(this.id());
        this.encodingCombobox.setAction(Foundation.selector("encodingComboboxClicked:"));
        this.encodingCombobox.removeAllItems();
        this.encodingCombobox.addItemsWithTitles(NSArray.arrayWithObjects(new DefaultCharsetProvider().availableCharsets()));
        this.encodingCombobox.selectItemWithTitle(preferences.getProperty("browser.charset.encoding"));
    }

    @Action
    public void encodingComboboxClicked(NSPopUpButton sender) {
        preferences.setProperty("browser.charset.encoding", sender.titleOfSelectedItem());
    }

    @Outlet
    private NSButton connectionRetryCheckbox;

    public void setConnectionRetryCheckbox(NSButton b) {
        this.connectionRetryCheckbox = b;
        this.connectionRetryCheckbox.setTarget(this.id());
        this.connectionRetryCheckbox.setAction(Foundation.selector("connectionRetryCheckboxClicked:"));
        this.connectionRetryCheckbox.setState(preferences.getInteger("connection.retry") > 0 ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void connectionRetryCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("connection.retry", enabled ? 1 : 0);
        connectionRetryNumberStepper.setEnabled(enabled);
        connectionRetryNumberField.setIntValue(enabled ? 1 : 0);
        connectionRetryDelayStepper.setEnabled(enabled);
    }

    @Outlet
    private NSTextField connectionTimeoutField;

    public void setConnectionTimeoutField(NSTextField b) {
        this.connectionTimeoutField = b;
        this.connectionTimeoutField.setIntValue(preferences.getInteger("connection.timeout.seconds"));
    }

    @Outlet
    private NSStepper connectionTimeoutStepper;

    public void setConnectionTimeoutStepper(NSStepper b) {
        this.connectionTimeoutStepper = b;
        this.connectionTimeoutStepper.setTarget(this.id());
        this.connectionTimeoutStepper.setAction(Foundation.selector("connectionTimeoutStepperClicked:"));
        this.connectionTimeoutStepper.setIntValue(preferences.getInteger("connection.retry"));
    }

    @Action
    public void connectionTimeoutStepperClicked(final NSStepper sender) {
        preferences.setProperty("connection.timeout.seconds", sender.intValue());
        connectionTimeoutField.setIntValue(sender.intValue());
    }

    @Outlet
    private NSTextField connectionRetryNumberField;

    public void setConnectionRetryNumberField(NSTextField b) {
        this.connectionRetryNumberField = b;
        this.connectionRetryNumberField.setIntValue(preferences.getInteger("connection.retry"));
    }

    @Outlet
    private NSStepper connectionRetryNumberStepper;

    public void setConnectionRetryNumberStepper(NSStepper b) {
        this.connectionRetryNumberStepper = b;
        this.connectionRetryNumberStepper.setTarget(this.id());
        this.connectionRetryNumberStepper.setAction(Foundation.selector("connectionRetryNumberStepperClicked:"));
        this.connectionRetryNumberStepper.setIntValue(preferences.getInteger("connection.retry"));
    }

    @Action
    public void connectionRetryNumberStepperClicked(final NSStepper sender) {
        preferences.setProperty("connection.retry", sender.intValue());
        connectionRetryNumberField.setIntValue(sender.intValue());
    }

    @Outlet
    private NSTextField connectionRetryDelayField;

    public void setConnectionRetryDelayField(NSTextField b) {
        this.connectionRetryDelayField = b;
        this.connectionRetryDelayField.setIntValue(preferences.getInteger("connection.retry.delay"));
    }

    @Outlet
    private NSStepper connectionRetryDelayStepper;

    public void setConnectionRetryDelayStepper(NSStepper b) {
        this.connectionRetryDelayStepper = b;
        this.connectionRetryDelayStepper.setTarget(this.id());
        this.connectionRetryDelayStepper.setAction(Foundation.selector("connectionRetryDelayStepperClicked:"));
        this.connectionRetryDelayStepper.setIntValue(preferences.getInteger("connection.retry.delay"));
    }

    @Action
    public void connectionRetryDelayStepperClicked(final NSStepper sender) {
        preferences.setProperty("connection.retry.delay", sender.intValue());
        connectionRetryDelayField.setIntValue(sender.intValue());
    }

    @Outlet
    private NSPopUpButton chmodUploadTypePopup;

    public void setChmodUploadTypePopup(NSPopUpButton b) {
        this.chmodUploadTypePopup = b;
        this.chmodUploadTypePopup.selectItemAtIndex(new NSInteger(0));
        this.chmodUploadTypePopup.setTarget(this.id());
        this.chmodUploadTypePopup.setAction(Foundation.selector("chmodUploadTypePopupChanged:"));
    }

    @Action
    public void chmodUploadTypePopupChanged(NSPopUpButton sender) {
        Permission p = null;
        if(sender.selectedItem().tag() == 0) {
            p = new Permission(preferences.getInteger("queue.upload.permissions.file.default"));
        }
        if(sender.selectedItem().tag() == 1) {
            p = new Permission(preferences.getInteger("queue.upload.permissions.folder.default"));
        }
        if(null == p) {
            log.error("No selected item for:" + sender);
            return;
        }
        Permission.Action ownerPerm = p.getUser();
        Permission.Action groupPerm = p.getGroup();
        Permission.Action otherPerm = p.getOther();

        uownerr.setState(ownerPerm.implies(Permission.Action.read) ? NSCell.NSOnState : NSCell.NSOffState);
        uownerw.setState(ownerPerm.implies(Permission.Action.write) ? NSCell.NSOnState : NSCell.NSOffState);
        uownerx.setState(ownerPerm.implies(Permission.Action.execute) ? NSCell.NSOnState : NSCell.NSOffState);

        ugroupr.setState(groupPerm.implies(Permission.Action.read) ? NSCell.NSOnState : NSCell.NSOffState);
        ugroupw.setState(groupPerm.implies(Permission.Action.write) ? NSCell.NSOnState : NSCell.NSOffState);
        ugroupx.setState(groupPerm.implies(Permission.Action.execute) ? NSCell.NSOnState : NSCell.NSOffState);

        uotherr.setState(otherPerm.implies(Permission.Action.read) ? NSCell.NSOnState : NSCell.NSOffState);
        uotherw.setState(otherPerm.implies(Permission.Action.write) ? NSCell.NSOnState : NSCell.NSOffState);
        uotherx.setState(otherPerm.implies(Permission.Action.execute) ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSPopUpButton chmodDownloadTypePopup;

    public void setChmodDownloadTypePopup(NSPopUpButton b) {
        this.chmodDownloadTypePopup = b;
        this.chmodDownloadTypePopup.selectItemAtIndex(new NSInteger(0));
        this.chmodDownloadTypePopup.setTarget(this.id());
        this.chmodDownloadTypePopup.setAction(Foundation.selector("chmodDownloadTypePopupChanged:"));
    }

    @Action
    public void chmodDownloadTypePopupChanged(NSPopUpButton sender) {
        Permission p = null;
        if(sender.selectedItem().tag() == 0) {
            p = new Permission(preferences.getInteger("queue.download.permissions.file.default"));
        }
        if(sender.selectedItem().tag() == 1) {
            p = new Permission(preferences.getInteger("queue.download.permissions.folder.default"));
        }
        if(null == p) {
            log.error("No selected item for:" + sender);
            return;
        }
        Permission.Action ownerPerm = p.getUser();
        Permission.Action groupPerm = p.getGroup();
        Permission.Action otherPerm = p.getOther();

        downerr.setState(ownerPerm.implies(Permission.Action.read) ? NSCell.NSOnState : NSCell.NSOffState);
        downerw.setState(ownerPerm.implies(Permission.Action.write) ? NSCell.NSOnState : NSCell.NSOffState);
        downerx.setState(ownerPerm.implies(Permission.Action.execute) ? NSCell.NSOnState : NSCell.NSOffState);

        dgroupr.setState(groupPerm.implies(Permission.Action.read) ? NSCell.NSOnState : NSCell.NSOffState);
        dgroupw.setState(groupPerm.implies(Permission.Action.write) ? NSCell.NSOnState : NSCell.NSOffState);
        dgroupx.setState(groupPerm.implies(Permission.Action.execute) ? NSCell.NSOnState : NSCell.NSOffState);

        dotherr.setState(otherPerm.implies(Permission.Action.read) ? NSCell.NSOnState : NSCell.NSOffState);
        dotherw.setState(otherPerm.implies(Permission.Action.write) ? NSCell.NSOnState : NSCell.NSOffState);
        dotherx.setState(otherPerm.implies(Permission.Action.execute) ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSButton chmodUploadCheckbox;

    public void setChmodUploadCheckbox(NSButton b) {
        this.chmodUploadCheckbox = b;
        this.chmodUploadCheckbox.setTarget(this.id());
        this.chmodUploadCheckbox.setAction(Foundation.selector("chmodUploadCheckboxClicked:"));
        this.chmodUploadCheckbox.setState(preferences.getBoolean("queue.upload.permissions.change") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void chmodUploadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.upload.permissions.change", enabled);
        this.chmodUploadDefaultCheckbox.setEnabled(enabled);
        this.chmodUploadCustomCheckbox.setEnabled(enabled);
        boolean chmodUploadDefaultChecked = this.chmodUploadDefaultCheckbox.state() == NSCell.NSOnState;
        this.uownerr.setEnabled(enabled && chmodUploadDefaultChecked);
        this.uownerw.setEnabled(enabled && chmodUploadDefaultChecked);
        this.uownerx.setEnabled(enabled && chmodUploadDefaultChecked);
        this.ugroupr.setEnabled(enabled && chmodUploadDefaultChecked);
        this.ugroupw.setEnabled(enabled && chmodUploadDefaultChecked);
        this.ugroupx.setEnabled(enabled && chmodUploadDefaultChecked);
        this.uotherr.setEnabled(enabled && chmodUploadDefaultChecked);
        this.uotherw.setEnabled(enabled && chmodUploadDefaultChecked);
        this.uotherx.setEnabled(enabled && chmodUploadDefaultChecked);
    }

    @Outlet
    private NSButton chmodUploadDefaultCheckbox;

    public void setChmodUploadDefaultCheckbox(NSButton b) {
        this.chmodUploadDefaultCheckbox = b;
        this.chmodUploadDefaultCheckbox.setTarget(this.id());
        this.chmodUploadDefaultCheckbox.setAction(Foundation.selector("chmodUploadDefaultCheckboxClicked:"));
        this.chmodUploadDefaultCheckbox.setState(preferences.getBoolean("queue.upload.permissions.default") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodUploadDefaultCheckbox.setEnabled(preferences.getBoolean("queue.upload.permissions.change"));
    }

    @Action
    public void chmodUploadDefaultCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.upload.permissions.default", enabled);
        this.uownerr.setEnabled(enabled);
        this.uownerw.setEnabled(enabled);
        this.uownerx.setEnabled(enabled);
        this.ugroupr.setEnabled(enabled);
        this.ugroupw.setEnabled(enabled);
        this.ugroupx.setEnabled(enabled);
        this.uotherr.setEnabled(enabled);
        this.uotherw.setEnabled(enabled);
        this.uotherx.setEnabled(enabled);
        this.chmodUploadCustomCheckbox.setState(!enabled ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSButton chmodUploadCustomCheckbox;

    public void setChmodUploadCustomCheckbox(NSButton b) {
        this.chmodUploadCustomCheckbox = b;
        this.chmodUploadCustomCheckbox.setTarget(this.id());
        this.chmodUploadCustomCheckbox.setAction(Foundation.selector("chmodUploadCustomCheckboxClicked:"));
        this.chmodUploadCustomCheckbox.setState(!preferences.getBoolean("queue.upload.permissions.default") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodUploadCustomCheckbox.setEnabled(preferences.getBoolean("queue.upload.permissions.change"));
    }

    @Action
    public void chmodUploadCustomCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.upload.permissions.default", !enabled);
        this.uownerr.setEnabled(!enabled);
        this.uownerw.setEnabled(!enabled);
        this.uownerx.setEnabled(!enabled);
        this.ugroupr.setEnabled(!enabled);
        this.ugroupw.setEnabled(!enabled);
        this.ugroupx.setEnabled(!enabled);
        this.uotherr.setEnabled(!enabled);
        this.uotherw.setEnabled(!enabled);
        this.uotherx.setEnabled(!enabled);
        this.chmodUploadDefaultCheckbox.setState(!enabled ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSButton chmodDownloadCheckbox;

    public void setChmodDownloadCheckbox(NSButton b) {
        this.chmodDownloadCheckbox = b;
        this.chmodDownloadCheckbox.setTarget(this.id());
        this.chmodDownloadCheckbox.setAction(Foundation.selector("chmodDownloadCheckboxClicked:"));
        this.chmodDownloadCheckbox.setState(preferences.getBoolean("queue.download.permissions.change") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void chmodDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.download.permissions.change", enabled);
        this.chmodDownloadDefaultCheckbox.setEnabled(enabled);
        this.chmodDownloadCustomCheckbox.setEnabled(enabled);
        boolean chmodDownloadDefaultChecked = this.chmodDownloadDefaultCheckbox.state() == NSCell.NSOnState;
        this.downerr.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.downerw.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.downerx.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.dgroupr.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.dgroupw.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.dgroupx.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.dotherr.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.dotherw.setEnabled(enabled && chmodDownloadDefaultChecked);
        this.dotherx.setEnabled(enabled && chmodDownloadDefaultChecked);
    }

    @Outlet
    private NSButton chmodDownloadDefaultCheckbox;

    public void setChmodDownloadDefaultCheckbox(NSButton b) {
        this.chmodDownloadDefaultCheckbox = b;
        this.chmodDownloadDefaultCheckbox.setTarget(this.id());
        this.chmodDownloadDefaultCheckbox.setAction(Foundation.selector("chmodDownloadDefaultCheckboxClicked:"));
        this.chmodDownloadDefaultCheckbox.setState(preferences.getBoolean("queue.download.permissions.default") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodDownloadDefaultCheckbox.setEnabled(preferences.getBoolean("queue.download.permissions.change"));
    }

    @Action
    public void chmodDownloadDefaultCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.download.permissions.default", enabled);
        this.downerr.setEnabled(enabled);
        this.downerw.setEnabled(enabled);
        this.downerx.setEnabled(enabled);
        this.dgroupr.setEnabled(enabled);
        this.dgroupw.setEnabled(enabled);
        this.dgroupx.setEnabled(enabled);
        this.dotherr.setEnabled(enabled);
        this.dotherw.setEnabled(enabled);
        this.dotherx.setEnabled(enabled);
        this.chmodDownloadCustomCheckbox.setState(!enabled ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSButton chmodDownloadCustomCheckbox;

    public void setChmodDownloadCustomCheckbox(NSButton b) {
        this.chmodDownloadCustomCheckbox = b;
        this.chmodDownloadCustomCheckbox.setTarget(this.id());
        this.chmodDownloadCustomCheckbox.setAction(Foundation.selector("chmodDownloadCustomCheckboxClicked:"));
        this.chmodDownloadCustomCheckbox.setState(!preferences.getBoolean("queue.download.permissions.default") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodDownloadCustomCheckbox.setEnabled(preferences.getBoolean("queue.download.permissions.change"));
    }

    @Action
    public void chmodDownloadCustomCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.download.permissions.default", !enabled);
        this.downerr.setEnabled(!enabled);
        this.downerw.setEnabled(!enabled);
        this.downerx.setEnabled(!enabled);
        this.dgroupr.setEnabled(!enabled);
        this.dgroupw.setEnabled(!enabled);
        this.dgroupx.setEnabled(!enabled);
        this.dotherr.setEnabled(!enabled);
        this.dotherw.setEnabled(!enabled);
        this.dotherx.setEnabled(!enabled);
        this.chmodDownloadDefaultCheckbox.setState(!enabled ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSButton downerr;
    @Outlet
    private NSButton downerw;
    @Outlet
    private NSButton downerx;
    @Outlet
    private NSButton dgroupr;
    @Outlet
    private NSButton dgroupw;
    @Outlet
    private NSButton dgroupx;
    @Outlet
    private NSButton dotherr;
    @Outlet
    private NSButton dotherw;
    @Outlet
    private NSButton dotherx;

    public void setDownerr(NSButton downerr) {
        this.downerr = downerr;
        this.downerr.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.downerr.setTarget(this.id());
        this.downerr.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDownerw(NSButton downerw) {
        this.downerw = downerw;
        this.downerw.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.downerw.setTarget(this.id());
        this.downerw.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDownerx(NSButton downerx) {
        this.downerx = downerx;
        this.downerx.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.downerx.setTarget(this.id());
        this.downerx.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDgroupr(NSButton dgroupr) {
        this.dgroupr = dgroupr;
        this.dgroupr.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.dgroupr.setTarget(this.id());
        this.dgroupr.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDgroupw(NSButton dgroupw) {
        this.dgroupw = dgroupw;
        this.dgroupw.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.dgroupw.setTarget(this.id());
        this.dgroupw.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDgroupx(NSButton dgroupx) {
        this.dgroupx = dgroupx;
        this.dgroupx.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.dgroupx.setTarget(this.id());
        this.dgroupx.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDotherr(NSButton dotherr) {
        this.dotherr = dotherr;
        this.dotherr.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.dotherr.setTarget(this.id());
        this.dotherr.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDotherw(NSButton dotherw) {
        this.dotherw = dotherw;
        this.dotherw.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.dotherw.setTarget(this.id());
        this.dotherw.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void setDotherx(NSButton dotherx) {
        this.dotherx = dotherx;
        this.dotherx.setEnabled(preferences.getBoolean("queue.download.permissions.change")
            && preferences.getBoolean("queue.download.permissions.default"));
        this.dotherx.setTarget(this.id());
        this.dotherx.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
    }

    public void defaultPermissionsDownloadChanged(final ID sender) {
        Permission.Action u = Permission.Action.none;
        if(downerr.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.read);
        }
        if(downerw.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.write);
        }
        if(downerx.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.execute);
        }
        Permission.Action g = Permission.Action.none;
        if(dgroupr.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.read);
        }
        if(dgroupw.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.write);
        }
        if(dgroupx.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.execute);
        }
        Permission.Action o = Permission.Action.none;
        if(dotherr.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.read);
        }
        if(dotherw.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.write);
        }
        if(dotherx.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.execute);
        }
        final Permission permission = new Permission(u, g, o);
        if(chmodDownloadTypePopup.selectedItem().tag() == 0) {
            preferences.setProperty("queue.download.permissions.file.default", permission.getMode());
        }
        if(chmodDownloadTypePopup.selectedItem().tag() == 1) {
            preferences.setProperty("queue.download.permissions.folder.default", permission.getMode());
        }
    }

    @Outlet
    private NSButton uownerr;
    @Outlet
    private NSButton uownerw;
    @Outlet
    private NSButton uownerx;
    @Outlet
    private NSButton ugroupr;
    @Outlet
    private NSButton ugroupw;
    @Outlet
    private NSButton ugroupx;
    @Outlet
    private NSButton uotherr;
    @Outlet
    private NSButton uotherw;
    @Outlet
    private NSButton uotherx;

    public void setUownerr(NSButton uownerr) {
        this.uownerr = uownerr;
        this.uownerr.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.uownerr.setTarget(this.id());
        this.uownerr.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUownerw(NSButton uownerw) {
        this.uownerw = uownerw;
        this.uownerw.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.uownerw.setTarget(this.id());
        this.uownerw.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUownerx(NSButton uownerx) {
        this.uownerx = uownerx;
        this.uownerx.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.uownerx.setTarget(this.id());
        this.uownerx.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));

    }

    public void setUgroupr(NSButton ugroupr) {
        this.ugroupr = ugroupr;
        this.ugroupr.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.ugroupr.setTarget(this.id());
        this.ugroupr.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUgroupw(NSButton ugroupw) {
        this.ugroupw = ugroupw;
        this.ugroupw.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.ugroupw.setTarget(this.id());
        this.ugroupw.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUgroupx(NSButton ugroupx) {
        this.ugroupx = ugroupx;
        this.ugroupx.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.ugroupx.setTarget(this.id());
        this.ugroupx.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUotherr(NSButton uotherr) {
        this.uotherr = uotherr;
        this.uotherr.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.uotherr.setTarget(this.id());
        this.uotherr.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUotherw(NSButton uotherw) {
        this.uotherw = uotherw;
        this.uotherw.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.uotherw.setTarget(this.id());
        this.uotherw.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
    }

    public void setUotherx(NSButton uotherx) {
        this.uotherx = uotherx;
        this.uotherx.setEnabled(preferences.getBoolean("queue.upload.permissions.change")
            && preferences.getBoolean("queue.upload.permissions.default"));
        this.uotherx.setTarget(this.id());
        this.uotherx.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));

    }

    @Action
    public void defaultPermissionsUploadChanged(final NSButton sender) {
        Permission.Action u = Permission.Action.none;
        if(uownerr.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.read);
        }
        if(uownerw.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.write);
        }
        if(uownerx.state() == NSCell.NSOnState) {
            u = u.or(Permission.Action.execute);
        }
        Permission.Action g = Permission.Action.none;
        if(ugroupr.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.read);
        }
        if(ugroupw.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.write);
        }
        if(ugroupx.state() == NSCell.NSOnState) {
            g = g.or(Permission.Action.execute);
        }
        Permission.Action o = Permission.Action.none;
        if(uotherr.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.read);
        }
        if(uotherw.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.write);
        }
        if(uotherx.state() == NSCell.NSOnState) {
            o = o.or(Permission.Action.execute);
        }
        final Permission permission = new Permission(u, g, o);
        if(chmodUploadTypePopup.selectedItem().tag() == 0) {
            preferences.setProperty("queue.upload.permissions.file.default", permission.getMode());
        }
        if(chmodUploadTypePopup.selectedItem().tag() == 1) {
            preferences.setProperty("queue.upload.permissions.folder.default", permission.getMode());
        }
    }

    @Outlet
    private NSButton preserveModificationDownloadCheckbox;

    public void setPreserveModificationDownloadCheckbox(NSButton b) {
        this.preserveModificationDownloadCheckbox = b;
        this.preserveModificationDownloadCheckbox.setTarget(this.id());
        this.preserveModificationDownloadCheckbox.setAction(Foundation.selector("preserveModificationDownloadCheckboxClicked:"));
        this.preserveModificationDownloadCheckbox.setState(preferences.getBoolean("queue.download.timestamp.change") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void preserveModificationDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.download.timestamp.change", enabled);
    }

    @Outlet
    private NSButton preserveModificationUploadCheckbox;

    public void setPreserveModificationUploadCheckbox(NSButton b) {
        this.preserveModificationUploadCheckbox = b;
        this.preserveModificationUploadCheckbox.setTarget(this.id());
        this.preserveModificationUploadCheckbox.setAction(Foundation.selector("preserveModificationUploadCheckboxClicked:"));
        this.preserveModificationUploadCheckbox.setState(preferences.getBoolean("queue.upload.timestamp.change") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void preserveModificationUploadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.upload.timestamp.change", enabled);
    }

    @Outlet
    private NSButton checksumDownloadCheckbox;

    public void setChecksumDownloadCheckbox(NSButton b) {
        this.checksumDownloadCheckbox = b;
        this.checksumDownloadCheckbox.setTarget(this.id());
        this.checksumDownloadCheckbox.setAction(Foundation.selector("checksumDownloadCheckboxClicked:"));
        this.checksumDownloadCheckbox.setState(preferences.getBoolean("queue.download.checksum.calculate") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void checksumDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.download.checksum.calculate", enabled);
    }

    @Outlet
    private NSButton checksumUploadCheckbox;

    public void setChecksumUploadCheckbox(NSButton b) {
        this.checksumUploadCheckbox = b;
        this.checksumUploadCheckbox.setTarget(this.id());
        this.checksumUploadCheckbox.setAction(Foundation.selector("checksumUploadCheckboxClicked:"));
        this.checksumUploadCheckbox.setState(preferences.getBoolean("queue.upload.checksum.calculate") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void checksumUploadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.upload.checksum.calculate", enabled);
    }

    @Outlet
    private NSButton horizontalLinesCheckbox;

    public void setHorizontalLinesCheckbox(NSButton b) {
        this.horizontalLinesCheckbox = b;
        this.horizontalLinesCheckbox.setTarget(this.id());
        this.horizontalLinesCheckbox.setAction(Foundation.selector("horizontalLinesCheckboxClicked:"));
        this.horizontalLinesCheckbox.setState(preferences.getBoolean("browser.horizontalLines") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void horizontalLinesCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.horizontalLines", enabled);
        BrowserController.updateBrowserTableAttributes();
    }

    @Outlet
    private NSButton verticalLinesCheckbox;

    public void setVerticalLinesCheckbox(NSButton b) {
        this.verticalLinesCheckbox = b;
        this.verticalLinesCheckbox.setTarget(this.id());
        this.verticalLinesCheckbox.setAction(Foundation.selector("verticalLinesCheckboxClicked:"));
        this.verticalLinesCheckbox.setState(preferences.getBoolean("browser.verticalLines") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void verticalLinesCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.verticalLines", enabled);
        BrowserController.updateBrowserTableAttributes();
    }

    @Outlet
    private NSButton alternatingRowBackgroundCheckbox;

    public void setAlternatingRowBackgroundCheckbox(NSButton b) {
        this.alternatingRowBackgroundCheckbox = b;
        this.alternatingRowBackgroundCheckbox.setTarget(this.id());
        this.alternatingRowBackgroundCheckbox.setAction(Foundation.selector("alternatingRowBackgroundCheckboxClicked:"));
        this.alternatingRowBackgroundCheckbox.setState(preferences.getBoolean("browser.alternatingRows") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void alternatingRowBackgroundCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.alternatingRows", enabled);
        BrowserController.updateBrowserTableAttributes();
    }

    @Outlet
    private NSButton infoWindowAsInspectorCheckbox;

    public void setInfoWindowAsInspectorCheckbox(NSButton b) {
        this.infoWindowAsInspectorCheckbox = b;
        this.infoWindowAsInspectorCheckbox.setTarget(this.id());
        this.infoWindowAsInspectorCheckbox.setAction(Foundation.selector("infoWindowAsInspectorCheckboxClicked:"));
        this.infoWindowAsInspectorCheckbox.setState(preferences.getBoolean("browser.info.inspector") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void infoWindowAsInspectorCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.info.inspector", enabled);
    }

    @Outlet
    private NSPopUpButton downloadPathPopup;

    // The currently set download folder
    private final Local DEFAULT_DOWNLOAD_FOLDER = LocalFactory.get(preferences.getProperty("queue.download.folder"));

    public void setDownloadPathPopup(NSPopUpButton b) {
        this.downloadPathPopup = b;
        this.downloadPathPopup.setTarget(this.id());
        this.downloadPathPopup.setAction(Foundation.selector("downloadPathPopupClicked:"));
        this.downloadPathPopup.removeAllItems();
        // Default download folder
        this.addDownloadPath(DEFAULT_DOWNLOAD_FOLDER);
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        // Shortcut to the Desktop
        this.addDownloadPath(LocalFactory.get("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(LocalFactory.get("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(LocalFactory.get("~/Downloads"));
        // Choose another folder
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.downloadPathPopup.addItemWithTitle(String.format("%s…", LocaleFactory.localizedString("Choose")));
    }

    private void addDownloadPath(final Local f) {
        this.downloadPathPopup.addItemWithTitle(f.getDisplayName());
        this.downloadPathPopup.lastItem().setImage(
            IconCacheFactory.<NSImage>get().fileIcon(f, 16)
        );
        this.downloadPathPopup.lastItem().setRepresentedObject(f.getAbbreviatedPath());
        if(DEFAULT_DOWNLOAD_FOLDER.equals(f)) {
            this.downloadPathPopup.selectItem(this.downloadPathPopup.lastItem());
        }
    }

    private NSOpenPanel downloadPathPanel;

    @Action
    public void downloadPathPopupClicked(final NSPopUpButton sender) {
        if(sender.title().equals(String.format("%s…", LocaleFactory.localizedString("Choose")))) {
            downloadPathPanel = NSOpenPanel.openPanel();
            downloadPathPanel.setCanChooseFiles(false);
            downloadPathPanel.setCanChooseDirectories(true);
            downloadPathPanel.setAllowsMultipleSelection(false);
            downloadPathPanel.setCanCreateDirectories(true);
            downloadPathPanel.beginSheetForDirectory(null, null, this.window, this.id(),
                Foundation.selector("downloadPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            preferences.setProperty("queue.download.folder", sender.selectedItem().representedObject());
        }
    }

    public void downloadPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            NSArray selected = sheet.filenames();
            String filename;
            if((filename = selected.lastObject().toString()) != null) {
                final Local folder = LocalFactory.get(filename);
                preferences.setProperty("queue.download.folder", folder.getAbbreviatedPath());
                preferences.setProperty("queue.download.folder.bookmark", folder.getBookmark());
            }
        }
        final Local custom = LocalFactory.get(preferences.getProperty("queue.download.folder"));
        final NSMenuItem item = downloadPathPopup.itemAtIndex(new NSInteger(0));
        item.setTitle(custom.getDisplayName());
        item.setRepresentedObject(custom.getAbsolute());
        item.setImage(IconCacheFactory.<NSImage>get().fileIcon(custom, 16));
        downloadPathPopup.selectItem(item);
        downloadPathPanel = null;
    }

    @Outlet
    private NSPopUpButton transferPopup;

    public void setTransferPopup(NSPopUpButton b) {
        this.transferPopup = b;
        this.transferPopup.setTarget(this.id());
        this.transferPopup.setAction(Foundation.selector("transferPopupClicked:"));
        this.transferPopup.removeAllItems();
        for(String name : preferences.getList("queue.transfer.type.enabled")) {
            final Host.TransferType t = Host.TransferType.valueOf(name);
            this.transferPopup.addItemWithTitle(t.toString());
            this.transferPopup.lastItem().setRepresentedObject(t.name());
        }
        this.transferPopup.selectItemAtIndex(this.transferPopup.indexOfItemWithRepresentedObject(
            preferences.getProperty("queue.transfer.type")
        ));
    }

    @Action
    public void transferPopupClicked(final NSPopUpButton sender) {
        preferences.setProperty("queue.transfer.type", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSButton downloadSkipButton;

    public void setDownloadSkipButton(NSButton b) {
        this.downloadSkipButton = b;
        this.downloadSkipButton.setTarget(this.id());
        this.downloadSkipButton.setAction(Foundation.selector("downloadSkipButtonClicked:"));
        this.downloadSkipButton.setState(preferences.getBoolean("queue.download.skip.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void downloadSkipButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        downloadSkipRegexField.setSelectable(enabled);
        downloadSkipRegexField.setEditable(enabled);
        downloadSkipRegexField.setTextColor(enabled ? NSColor.controlTextColor() : NSColor.disabledControlTextColor());
        preferences.setProperty("queue.download.skip.enable", enabled);
    }

    @Outlet
    private NSButton downloadSkipRegexDefaultButton;

    public void setDownloadSkipRegexDefaultButton(NSButton b) {
        this.downloadSkipRegexDefaultButton = b;
        this.downloadSkipRegexDefaultButton.setTarget(this.id());
        this.downloadSkipRegexDefaultButton.setAction(Foundation.selector("downloadSkipRegexDefaultButtonClicked:"));
    }

    @Action
    public void downloadSkipRegexDefaultButtonClicked(final NSButton sender) {
        final String regex = preferences.getProperty("queue.download.skip.regex.default");
        this.downloadSkipRegexField.setString(regex);
        preferences.setProperty("queue.download.skip.regex", regex);
    }

    private NSTextView downloadSkipRegexField;

    public void setDownloadSkipRegexField(NSTextView t) {
        this.downloadSkipRegexField = t;
        this.downloadSkipRegexField.setFont(NSFont.userFixedPitchFontOfSize(9.0f));
        this.downloadSkipRegexField.setString(preferences.getProperty("queue.download.skip.regex"));
        notificationCenter.addObserver(this.id(),
            Foundation.selector("downloadSkipRegexFieldDidChange:"),
            NSText.TextDidChangeNotification,
            this.downloadSkipRegexField.id());
    }

    public void downloadSkipRegexFieldDidChange(NSNotification sender) {
        String value = this.downloadSkipRegexField.string().trim();
        if(StringUtils.EMPTY.equals(value)) {
            preferences.setProperty("queue.download.skip.enable", false);
            preferences.setProperty("queue.download.skip.regex", value);
            this.downloadSkipButton.setState(NSCell.NSOffState);
        }
        try {
            Pattern compiled = Pattern.compile(value);
            preferences.setProperty("queue.download.skip.regex",
                compiled.pattern());
            this.mark(this.downloadSkipRegexField.textStorage(), null);
        }
        catch(PatternSyntaxException e) {
            this.mark(this.downloadSkipRegexField.textStorage(), e);
        }
    }

    @Outlet
    private NSButton uploadSkipButton;

    public void setUploadSkipButton(NSButton b) {
        this.uploadSkipButton = b;
        this.uploadSkipButton.setTarget(this.id());
        this.uploadSkipButton.setAction(Foundation.selector("uploadSkipButtonClicked:"));
        this.uploadSkipButton.setState(preferences.getBoolean("queue.upload.skip.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void uploadSkipButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        uploadSkipRegexField.setSelectable(enabled);
        uploadSkipRegexField.setEditable(enabled);
        uploadSkipRegexField.setTextColor(enabled ? NSColor.controlTextColor() : NSColor.disabledControlTextColor());
        preferences.setProperty("queue.upload.skip.enable", enabled);
    }

    @Outlet
    private NSButton uploadSkipRegexDefaultButton;

    public void setUploadSkipRegexDefaultButton(NSButton b) {
        this.uploadSkipRegexDefaultButton = b;
        this.uploadSkipRegexDefaultButton.setTarget(this.id());
        this.uploadSkipRegexDefaultButton.setAction(Foundation.selector("uploadSkipRegexDefaultButtonClicked:"));
    }

    @Action
    public void uploadSkipRegexDefaultButtonClicked(final NSButton sender) {
        final String regex = preferences.getProperty("queue.upload.skip.regex.default");
        this.uploadSkipRegexField.setString(regex);
        preferences.setProperty("queue.upload.skip.regex", regex);
    }

    private NSTextView uploadSkipRegexField;

    public void setUploadSkipRegexField(NSTextView b) {
        this.uploadSkipRegexField = b;
        this.uploadSkipRegexField.setFont(NSFont.userFixedPitchFontOfSize(9.0f));
        this.uploadSkipRegexField.setString(preferences.getProperty("queue.upload.skip.regex"));
        notificationCenter.addObserver(this.id(),
            Foundation.selector("uploadSkipRegexFieldDidChange:"),
            NSText.TextDidChangeNotification,
            this.uploadSkipRegexField.id());
    }

    public void uploadSkipRegexFieldDidChange(NSNotification sender) {
        String value = this.uploadSkipRegexField.string().trim();
        if(StringUtils.EMPTY.equals(value)) {
            preferences.setProperty("queue.upload.skip.enable", false);
            preferences.setProperty("queue.upload.skip.regex", value);
            this.uploadSkipButton.setState(NSCell.NSOffState);
        }
        try {
            Pattern compiled = Pattern.compile(value);
            preferences.setProperty("queue.upload.skip.regex",
                compiled.pattern());
            this.mark(this.uploadSkipRegexField.textStorage(), null);
        }
        catch(PatternSyntaxException e) {
            this.mark(this.uploadSkipRegexField.textStorage(), e);
        }
    }

    protected static final NSDictionary RED_FONT = NSDictionary.dictionaryWithObjectsForKeys(
        NSArray.arrayWithObjects(NSColor.redColor()),
        NSArray.arrayWithObjects(NSAttributedString.ForegroundColorAttributeName)
    );

    private void mark(NSMutableAttributedString text, PatternSyntaxException e) {
        if(null == e) {
            text.removeAttributeInRange(
                NSAttributedString.ForegroundColorAttributeName,
                NSRange.NSMakeRange(new NSUInteger(0), text.length()));
            return;
        }
        int index = e.getIndex(); //The approximate index in the pattern of the error
        NSRange range = null;
        if(-1 == index) {
            range = NSRange.NSMakeRange(new NSUInteger(0), text.length());
        }
        if(index < text.length().intValue()) {
            //Initializes the NSRange with the range elements of location and length;
            range = NSRange.NSMakeRange(new NSUInteger(index), new NSUInteger(1));
        }
        text.addAttributesInRange(RED_FONT, range);
    }

    @Outlet
    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton b) {
        this.keychainCheckbox = b;
        this.keychainCheckbox.setTarget(this.id());
        this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
        this.keychainCheckbox.setState(preferences.getBoolean("connection.login.keychain") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void keychainCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("connection.login.keychain", enabled);
    }

    @Outlet
    private NSButton doubleClickCheckbox;

    public void setDoubleClickCheckbox(NSButton b) {
        this.doubleClickCheckbox = b;
        this.doubleClickCheckbox.setTarget(this.id());
        this.doubleClickCheckbox.setAction(Foundation.selector("doubleClickCheckboxClicked:"));
        this.doubleClickCheckbox.setState(preferences.getBoolean("browser.doubleclick.edit") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void doubleClickCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.doubleclick.edit", enabled);
    }

    @Outlet
    private NSButton returnKeyCheckbox;

    public void setReturnKeyCheckbox(NSButton b) {
        this.returnKeyCheckbox = b;
        this.returnKeyCheckbox.setTarget(this.id());
        this.returnKeyCheckbox.setAction(Foundation.selector("returnKeyCheckboxClicked:"));
        this.returnKeyCheckbox.setState(preferences.getBoolean("browser.enterkey.rename") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void returnKeyCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.enterkey.rename", enabled);
    }

    @Outlet
    private NSButton autoExpandCheckbox;

    public void setAutoExpandCheckbox(NSButton b) {
        this.autoExpandCheckbox = b;
        this.autoExpandCheckbox.setTarget(this.id());
        this.autoExpandCheckbox.setAction(Foundation.selector("autoExpandCheckboxClicked:"));
        this.autoExpandCheckbox.setState(preferences.getBoolean("browser.view.autoexpand") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void autoExpandCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.view.autoexpand", enabled);
        autoExpandDelayCheckbox.setEnabled(enabled);
        autoExpandDelaySlider.setEnabled(enabled);
    }

    @Outlet
    private NSButton autoExpandDelayCheckbox;

    public void setAutoExpandDelayCheckbox(NSButton b) {
        this.autoExpandDelayCheckbox = b;
        this.autoExpandDelayCheckbox.setTarget(this.id());
        this.autoExpandDelayCheckbox.setAction(Foundation.selector("autoExpandDelayCheckboxClicked:"));
        this.autoExpandDelayCheckbox.setState(preferences.getBoolean("browser.view.autoexpand.delay.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void autoExpandDelayCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.view.autoexpand.delay.enable", enabled);
        autoExpandDelaySlider.setEnabled(enabled);
    }

    @Outlet
    private NSSlider autoExpandDelaySlider;

    public void setAutoExpandDelaySlider(NSSlider b) {
        this.autoExpandDelaySlider = b;
        this.autoExpandDelaySlider.setTarget(this.id());
        this.autoExpandDelaySlider.setAction(Foundation.selector("autoExpandDelaySliderClicked:"));
        this.autoExpandDelaySlider.setDoubleValue(preferences.getDouble("browser.view.autoexpand.delay"));
    }

    @Action
    public void autoExpandDelaySliderClicked(final NSSlider sender) {
        preferences.setProperty("browser.view.autoexpand.delay", sender.doubleValue());
    }

    @Outlet
    private NSButton showHiddenCheckbox;

    public void setShowHiddenCheckbox(NSButton b) {
        this.showHiddenCheckbox = b;
        this.showHiddenCheckbox.setTarget(this.id());
        this.showHiddenCheckbox.setAction(Foundation.selector("showHiddenCheckboxClicked:"));
        this.showHiddenCheckbox.setState(preferences.getBoolean("browser.showHidden") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void showHiddenCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.showHidden", enabled);
    }

    @Outlet
    private NSButton bringQueueToFrontCheckbox;

    public void setBringQueueToFrontCheckbox(NSButton b) {
        this.bringQueueToFrontCheckbox = b;
        this.bringQueueToFrontCheckbox.setTarget(this.id());
        this.bringQueueToFrontCheckbox.setAction(Foundation.selector("bringQueueToFrontCheckboxClicked:"));
        this.bringQueueToFrontCheckbox.setState(preferences.getBoolean("queue.window.open.transfer.start") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void bringQueueToFrontCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.window.open.transfer.start", enabled);
    }

    @Outlet
    private NSButton bringQueueToBackCheckbox;

    public void setBringQueueToBackCheckbox(NSButton b) {
        this.bringQueueToBackCheckbox = b;
        this.bringQueueToBackCheckbox.setTarget(this.id());
        this.bringQueueToBackCheckbox.setAction(Foundation.selector("bringQueueToBackCheckboxClicked:"));
        this.bringQueueToBackCheckbox.setState(preferences.getBoolean("queue.window.open.transfer.stop") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void bringQueueToBackCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.window.open.transfer.stop", enabled);
    }

    @Outlet
    private NSButton removeFromQueueCheckbox;

    public void setRemoveFromQueueCheckbox(NSButton b) {
        this.removeFromQueueCheckbox = b;
        this.removeFromQueueCheckbox.setTarget(this.id());
        this.removeFromQueueCheckbox.setAction(Foundation.selector("removeFromQueueCheckboxClicked:"));
        this.removeFromQueueCheckbox.setState(preferences.getBoolean("queue.removeItemWhenComplete") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void removeFromQueueCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.removeItemWhenComplete", enabled);
    }

    @Outlet
    private NSButton openAfterDownloadCheckbox;

    public void setOpenAfterDownloadCheckbox(NSButton b) {
        this.openAfterDownloadCheckbox = b;
        this.openAfterDownloadCheckbox.setTarget(this.id());
        this.openAfterDownloadCheckbox.setAction(Foundation.selector("openAfterDownloadCheckboxClicked:"));
        this.openAfterDownloadCheckbox.setState(preferences.getBoolean("queue.download.complete.open") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void openAfterDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.download.complete.open", enabled);
    }

    @Outlet
    private NSPopUpButton duplicateDownloadCombobox;

    public void setDuplicateDownloadCombobox(NSPopUpButton b) {
        this.duplicateDownloadCombobox = b;
        this.duplicateDownloadCombobox.setAutoenablesItems(false);
        this.duplicateDownloadCombobox.setTarget(this.id());
        this.duplicateDownloadCombobox.setAction(Foundation.selector("duplicateDownloadComboboxClicked:"));
        this.duplicateDownloadCombobox.removeAllItems();
        for(TransferAction action : new TransferAction[]{
            TransferAction.callback,
            TransferAction.overwrite,
            TransferAction.resume,
            TransferAction.rename,
            TransferAction.renameexisting,
            TransferAction.comparison,
            TransferAction.skip}) {
            this.duplicateDownloadCombobox.addItemWithTitle(action.getTitle());
            this.duplicateDownloadCombobox.lastItem().setRepresentedObject(action.name());
            this.duplicateDownloadCombobox.addItemWithTitle(action.getDescription());
            this.duplicateDownloadCombobox.lastItem().setAttributedTitle(NSAttributedString.attributedStringWithAttributes(action.getDescription(),
                MENU_HELP_FONT_ATTRIBUTES));
            this.duplicateDownloadCombobox.lastItem().setEnabled(false);
        }
        this.duplicateDownloadCombobox.selectItemWithTitle(
            TransferAction.forName(preferences.getProperty("queue.download.action")).getTitle());
    }

    @Action
    public void duplicateDownloadComboboxClicked(NSPopUpButton sender) {
        preferences.setProperty("queue.download.action",
            TransferAction.forName(sender.selectedItem().representedObject()).name());
        this.duplicateDownloadOverwriteButtonClicked(duplicateDownloadOverwriteButton);
    }

    @Outlet
    private NSButton duplicateDownloadOverwriteButton;

    public void setDuplicateDownloadOverwriteButton(NSButton b) {
        this.duplicateDownloadOverwriteButton = b;
        this.duplicateDownloadOverwriteButton.setTarget(this.id());
        this.duplicateDownloadOverwriteButton.setAction(Foundation.selector("duplicateDownloadOverwriteButtonClicked:"));
        this.duplicateDownloadOverwriteButton.setState(
            preferences.getProperty("queue.download.reload.action").equals(
                TransferAction.overwrite.name()) ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void duplicateDownloadOverwriteButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        if(enabled) {
            preferences.setProperty("queue.download.reload.action", TransferAction.overwrite.name());
        }
        else {
            preferences.setProperty("queue.download.reload.action",
                preferences.getProperty("queue.download.action"));
        }
    }

    @Outlet
    private NSPopUpButton duplicateUploadCombobox;

    public void setDuplicateUploadCombobox(NSPopUpButton b) {
        this.duplicateUploadCombobox = b;
        this.duplicateUploadCombobox.setAutoenablesItems(false);
        this.duplicateUploadCombobox.setTarget(this.id());
        this.duplicateUploadCombobox.setAction(Foundation.selector("duplicateUploadComboboxClicked:"));
        this.duplicateUploadCombobox.removeAllItems();
        for(TransferAction action : new TransferAction[]{
            TransferAction.callback,
            TransferAction.overwrite,
            TransferAction.resume,
            TransferAction.rename,
            TransferAction.renameexisting,
            TransferAction.comparison,
            TransferAction.skip}) {
            this.duplicateUploadCombobox.addItemWithTitle(action.getTitle());
            this.duplicateUploadCombobox.lastItem().setRepresentedObject(action.name());
            this.duplicateUploadCombobox.addItemWithTitle(action.getDescription());
            this.duplicateUploadCombobox.lastItem().setAttributedTitle(NSAttributedString.attributedStringWithAttributes(action.getDescription(),
                MENU_HELP_FONT_ATTRIBUTES));
            this.duplicateUploadCombobox.lastItem().setEnabled(false);
        }
        this.duplicateUploadCombobox.selectItemWithTitle(
            TransferAction.forName(preferences.getProperty("queue.upload.action")).getTitle());
    }

    @Action
    public void duplicateUploadComboboxClicked(NSPopUpButton sender) {
        preferences.setProperty("queue.upload.action",
            TransferAction.forName(sender.selectedItem().representedObject()).name());
        this.duplicateUploadOverwriteButtonClicked(duplicateUploadOverwriteButton);
    }

    @Outlet
    private NSButton duplicateUploadOverwriteButton;

    public void setDuplicateUploadOverwriteButton(NSButton b) {
        this.duplicateUploadOverwriteButton = b;
        this.duplicateUploadOverwriteButton.setTarget(this.id());
        this.duplicateUploadOverwriteButton.setAction(Foundation.selector("duplicateUploadOverwriteButtonClicked:"));
        this.duplicateUploadOverwriteButton.setState(
            preferences.getProperty("queue.upload.reload.action").equals(
                TransferAction.overwrite.name()) ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void duplicateUploadOverwriteButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        if(enabled) {
            preferences.setProperty("queue.upload.reload.action", TransferAction.overwrite.name());
        }
        else {
            preferences.setProperty("queue.upload.reload.action",
                preferences.getProperty("queue.upload.action"));
        }
    }

    @Outlet
    private NSButton uploadTemporaryFilenameButton;

    public void setUploadTemporaryFilenameButton(NSButton b) {
        this.uploadTemporaryFilenameButton = b;
        this.uploadTemporaryFilenameButton.setTarget(this.id());
        this.uploadTemporaryFilenameButton.setAction(Foundation.selector("uploadTemporaryFilenameButtonClicked:"));
        this.uploadTemporaryFilenameButton.setState(
            preferences.getBoolean("queue.upload.file.temporary") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void uploadTemporaryFilenameButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("queue.upload.file.temporary", enabled);
    }

    @Outlet
    private NSPopUpButton protocolCombobox;

    public void setProtocolCombobox(NSPopUpButton b) {
        this.protocolCombobox = b;
        this.protocolCombobox.setTarget(this.id());
        this.protocolCombobox.setAction(Foundation.selector("protocolComboboxClicked:"));
        this.protocolCombobox.removeAllItems();
        final ProtocolFactory protocols = ProtocolFactory.get();
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(
            EnumSet.of(Protocol.Type.ftp, Protocol.Type.sftp, Protocol.Type.dav)))) {
            this.addProtocol(protocol);
        }
        this.protocolCombobox.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(
            EnumSet.of(Protocol.Type.s3, Protocol.Type.swift, Protocol.Type.azure, Protocol.Type.b2, Protocol.Type.dracoon, Protocol.Type.googlestorage)))) {
            this.addProtocol(protocol);
        }
        this.protocolCombobox.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(
            EnumSet.of(Protocol.Type.dropbox, Protocol.Type.onedrive, Protocol.Type.googledrive)))) {
            this.addProtocol(protocol);
        }
        this.protocolCombobox.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new DefaultProtocolPredicate(
            EnumSet.of(Protocol.Type.file)))) {
            this.addProtocol(protocol);
        }
        this.protocolCombobox.menu().addItem(NSMenuItem.separatorItem());
        for(Protocol protocol : protocols.find(new ProfileProtocolPredicate())) {
            this.addProtocol(protocol);
        }
        final Protocol defaultProtocol
            = ProtocolFactory.get().forName(preferences.getProperty("connection.protocol.default"));
        this.protocolCombobox.selectItemAtIndex(this.protocolCombobox.indexOfItemWithRepresentedObject(String.valueOf(defaultProtocol.hashCode())));
    }

    private void addProtocol(final Protocol protocol) {
        final String title = protocol.getDescription();
        protocolCombobox.addItemWithTitle(title);
        protocolCombobox.lastItem().setRepresentedObject(String.valueOf(protocol.hashCode()));
        protocolCombobox.lastItem().setImage(IconCacheFactory.<NSImage>get().iconNamed(protocol.icon(), 16));
    }

    @Action
    public void protocolComboboxClicked(NSPopUpButton sender) {
        final Protocol selected = ProtocolFactory.get().forName(sender.selectedItem().representedObject());
        preferences.setProperty("connection.protocol.default", selected.getIdentifier());
        preferences.setProperty("connection.port.default", selected.getDefaultPort());
    }

    @Outlet
    private NSButton confirmDisconnectCheckbox;

    public void setConfirmDisconnectCheckbox(NSButton b) {
        this.confirmDisconnectCheckbox = b;
        this.confirmDisconnectCheckbox.setTarget(this.id());
        this.confirmDisconnectCheckbox.setAction(Foundation.selector("confirmDisconnectCheckboxClicked:"));
        this.confirmDisconnectCheckbox.setState(preferences.getBoolean("browser.disconnect.confirm") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void confirmDisconnectCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("browser.disconnect.confirm", enabled);
    }

    private void configureDefaultProtocolHandlerCombobox(final NSPopUpButton defaultProtocolHandlerCombobox, final Scheme protocol) {
        final Application defaultHandler = SchemeHandlerFactory.get().getDefaultHandler(protocol);
        if(Application.notfound.equals(defaultHandler)) {
            defaultProtocolHandlerCombobox.addItemWithTitle(LocaleFactory.localizedString("Unknown"));
            defaultProtocolHandlerCombobox.setEnabled(false);
        }
        else {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Default Protocol Handler for %s:%s", protocol, defaultHandler));
            }
            for(Application handler : SchemeHandlerFactory.get().getAllHandlers(protocol)) {
                defaultProtocolHandlerCombobox.addItemWithTitle(handler.getName());
                final NSMenuItem item = defaultProtocolHandlerCombobox.lastItem();
                item.setImage(IconCacheFactory.<NSImage>get().applicationIcon(handler, 16));
                item.setRepresentedObject(handler.getIdentifier());
                if(handler.getIdentifier().equals(defaultHandler.getIdentifier())) {
                    defaultProtocolHandlerCombobox.selectItem(item);
                }
            }
        }
    }

    @Outlet
    private NSPopUpButton defaultFTPHandlerCombobox;

    public void setDefaultFTPHandlerCombobox(NSPopUpButton b) {
        this.defaultFTPHandlerCombobox = b;
        this.defaultFTPHandlerCombobox.setTarget(this.id());
        this.defaultFTPHandlerCombobox.setAction(Foundation.selector("defaultFTPHandlerComboboxClicked:"));
        this.defaultFTPHandlerCombobox.removeAllItems();
    }

    @Action
    public void defaultFTPHandlerComboboxClicked(NSPopUpButton sender) {
        String bundle = sender.selectedItem().representedObject();
        SchemeHandlerFactory.get().setDefaultHandler(
            Arrays.asList(Scheme.ftp, Scheme.ftps), new Application(bundle)
        );
    }

    @Outlet
    private NSPopUpButton defaultSFTPHandlerCombobox;

    public void setDefaultSFTPHandlerCombobox(NSPopUpButton b) {
        this.defaultSFTPHandlerCombobox = b;
        this.defaultSFTPHandlerCombobox.setTarget(this.id());
        this.defaultSFTPHandlerCombobox.setAction(Foundation.selector("defaultSFTPHandlerComboboxClicked:"));
        this.defaultSFTPHandlerCombobox.removeAllItems();
    }

    @Action
    public void defaultSFTPHandlerComboboxClicked(NSPopUpButton sender) {
        String bundle = sender.selectedItem().representedObject();
        SchemeHandlerFactory.get().setDefaultHandler(
            Collections.singletonList(Scheme.sftp), new Application(bundle)
        );
    }

    @Outlet
    private NSPopUpButton defaultDownloadThrottleCombobox;

    public void setDefaultDownloadThrottleCombobox(NSPopUpButton b) {
        this.defaultDownloadThrottleCombobox = b;
        this.defaultDownloadThrottleCombobox.setTarget(this.id());
        this.defaultDownloadThrottleCombobox.setAction(Foundation.selector("defaultDownloadThrottleComboboxClicked:"));
        int bandwidth = (int) preferences.getDouble("queue.download.bandwidth.bytes");
        final StringTokenizer options = new StringTokenizer(preferences.getProperty("queue.bandwidth.options"), ",");
        while(options.hasMoreTokens()) {
            final String bytes = options.nextToken();
            this.defaultDownloadThrottleCombobox.addItemWithTitle(SizeFormatterFactory.get().format(Integer.parseInt(bytes)) + "/s");
            this.defaultDownloadThrottleCombobox.lastItem().setRepresentedObject(bytes);
        }
        if(-1 == bandwidth) {
            this.defaultDownloadThrottleCombobox.selectItemWithTag(new NSInteger(-1));
        }
        else {
            this.defaultDownloadThrottleCombobox.selectItemAtIndex(
                this.defaultDownloadThrottleCombobox.menu().indexOfItemWithRepresentedObject(String.valueOf(bandwidth)));
        }
    }

    @Action
    public void defaultDownloadThrottleComboboxClicked(NSPopUpButton sender) {
        String bytes = sender.selectedItem().representedObject();
        if(null == bytes) {
            preferences.setProperty("queue.download.bandwidth.bytes", -1);
        }
        else {
            preferences.setProperty("queue.download.bandwidth.bytes", Integer.parseInt(bytes));
        }
    }

    @Outlet
    private NSPopUpButton defaultUploadThrottleCombobox;

    public void setDefaultUploadThrottleCombobox(NSPopUpButton b) {
        this.defaultUploadThrottleCombobox = b;
        this.defaultUploadThrottleCombobox.setTarget(this.id());
        this.defaultUploadThrottleCombobox.setAction(Foundation.selector("defaultUploadThrottleComboboxClicked:"));
        int bandwidth = (int) preferences.getDouble("queue.upload.bandwidth.bytes");
        final StringTokenizer options = new StringTokenizer(preferences.getProperty("queue.bandwidth.options"), ",");
        while(options.hasMoreTokens()) {
            final String bytes = options.nextToken();
            this.defaultUploadThrottleCombobox.addItemWithTitle(SizeFormatterFactory.get().format(Integer.parseInt(bytes)) + "/s");
            this.defaultUploadThrottleCombobox.lastItem().setRepresentedObject(bytes);
        }
        if(-1 == bandwidth) {
            this.defaultUploadThrottleCombobox.selectItemWithTag(new NSInteger(-1));
        }
        else {
            this.defaultUploadThrottleCombobox.selectItemAtIndex(
                this.defaultUploadThrottleCombobox.menu().indexOfItemWithRepresentedObject(String.valueOf(bandwidth)));
        }
    }

    public void defaultUploadThrottleComboboxClicked(NSPopUpButton sender) {
        String bytes = sender.selectedItem().representedObject();
        if(null == bytes) {
            preferences.setProperty("queue.upload.bandwidth.bytes", -1);
        }
        else {
            preferences.setProperty("queue.upload.bandwidth.bytes", Integer.parseInt(bytes));
        }
    }

    @Outlet
    private NSButton updateCheckbox;

    public void setUpdateCheckbox(NSButton b) {
        this.updateCheckbox = b;
        this.updateCheckbox.setTarget(this.id());
        this.updateCheckbox.setAction(Foundation.selector("updateCheckboxClicked:"));
        this.updateCheckbox.setState(preferences.getBoolean("update.check") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void updateCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("update.check", enabled);
    }

    @Outlet
    private NSButton updateCheckButton;

    public void setUpdateCheckButton(NSButton b) {
        this.updateCheckButton = b;
        this.updateCheckButton.setTarget(NSApplication.sharedApplication().delegate());
        this.updateCheckButton.setAction(Foundation.selector("updateMenuClicked:"));
    }

    @Outlet
    private NSPopUpButton updateFeedPopup;

    public void setUpdateFeedPopup(NSPopUpButton b) {
        this.updateFeedPopup = b;
        this.updateFeedPopup.removeAllItems();
        this.updateFeedPopup.setAction(Foundation.selector("updateFeedPopupClicked:"));
        this.updateFeedPopup.addItemWithTitle(LocaleFactory.localizedString("Release"));
        this.updateFeedPopup.lastItem().setRepresentedObject(preferences.getProperty("update.feed.release"));
        this.updateFeedPopup.addItemWithTitle(LocaleFactory.localizedString("Beta"));
        this.updateFeedPopup.lastItem().setRepresentedObject(preferences.getProperty("update.feed.beta"));
        this.updateFeedPopup.addItemWithTitle(LocaleFactory.localizedString("Snapshot Builds"));
        this.updateFeedPopup.lastItem().setRepresentedObject(preferences.getProperty("update.feed.nightly"));
        final String feed = preferences.getProperty(Updater.PROPERTY_FEED_URL);
        NSInteger selected = this.updateFeedPopup.menu().indexOfItemWithRepresentedObject(feed);
        if(-1 == selected.intValue()) {
            log.warn(String.format("Invalid feed setting:%s", feed));
            this.updateFeedPopup.selectItemAtIndex(this.updateFeedPopup.menu().indexOfItemWithRepresentedObject(
                preferences.getProperty("update.feed.release")));
        }
        else {
            this.updateFeedPopup.selectItemAtIndex(selected);
        }
    }

    @Action
    public void updateFeedPopupClicked(NSPopUpButton sender) {
        // Update sparkle feed property. Default is in Info.plist
        String selected = sender.selectedItem().representedObject();
        if(null == selected || preferences.getDefault(Updater.PROPERTY_FEED_URL).equals(selected)) {
            // Remove custom value
            preferences.deleteProperty(Updater.PROPERTY_FEED_URL);
        }
        else {
            preferences.setProperty(Updater.PROPERTY_FEED_URL, selected);
        }
    }

    @Outlet
    private NSPopUpButton defaultBucketLocation;

    public void setDefaultBucketLocation(NSPopUpButton b) {
        this.defaultBucketLocation = b;
        this.defaultBucketLocation.setAutoenablesItems(false);
        this.defaultBucketLocation.removeAllItems();
        for(Location.Name location : new S3Protocol().getRegions()) {
            this.defaultBucketLocation.addItemWithTitle(location.toString());
            this.defaultBucketLocation.lastItem().setRepresentedObject(location.getIdentifier());
        }
        this.defaultBucketLocation.setTarget(this.id());
        this.defaultBucketLocation.setAction(Foundation.selector("defaultBucketLocationClicked:"));
        this.defaultBucketLocation.selectItemWithTitle(LocaleFactory.localizedString(preferences.getProperty("s3.location"), "S3"));
    }

    @Action
    public void defaultBucketLocationClicked(NSPopUpButton sender) {
        preferences.setProperty("s3.location", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton defaultStorageClassPopup;

    public void setDefaultStorageClassPopup(NSPopUpButton b) {
        this.defaultStorageClassPopup = b;
        this.defaultStorageClassPopup.setAutoenablesItems(false);
        this.defaultStorageClassPopup.removeAllItems();
        this.defaultStorageClassPopup.addItemWithTitle(LocaleFactory.localizedString(S3Object.STORAGE_CLASS_STANDARD, "S3"));
        this.defaultStorageClassPopup.lastItem().setRepresentedObject(S3Object.STORAGE_CLASS_STANDARD);
        this.defaultStorageClassPopup.addItemWithTitle(LocaleFactory.localizedString(S3Object.STORAGE_CLASS_INFREQUENT_ACCESS, "S3"));
        this.defaultStorageClassPopup.lastItem().setRepresentedObject(S3Object.STORAGE_CLASS_INFREQUENT_ACCESS);
        this.defaultStorageClassPopup.addItemWithTitle(LocaleFactory.localizedString("ONEZONE_IA", "S3"));
        this.defaultStorageClassPopup.lastItem().setRepresentedObject("ONEZONE_IA");
        this.defaultStorageClassPopup.addItemWithTitle(LocaleFactory.localizedString(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, "S3"));
        this.defaultStorageClassPopup.lastItem().setRepresentedObject(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        this.defaultStorageClassPopup.setTarget(this.id());
        this.defaultStorageClassPopup.setAction(Foundation.selector("defaultStorageClassPopupClicked:"));
        this.defaultStorageClassPopup.selectItemWithTitle(LocaleFactory.localizedString(preferences.getProperty("s3.storage.class"), "S3"));
    }

    @Action
    public void defaultStorageClassPopupClicked(NSPopUpButton sender) {
        preferences.setProperty("s3.storage.class", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton defaultEncryptionPopup;

    public void setDefaultEncryptionPopup(NSPopUpButton b) {
        this.defaultEncryptionPopup = b;
        this.defaultEncryptionPopup.setAutoenablesItems(false);
        this.defaultEncryptionPopup.removeAllItems();
        this.defaultEncryptionPopup.addItemWithTitle(LocaleFactory.localizedString("None"));
        this.defaultEncryptionPopup.lastItem().setRepresentedObject(S3EncryptionFeature.Algorithm.NONE.toString());
        this.defaultEncryptionPopup.addItemWithTitle(LocaleFactory.localizedString(S3EncryptionFeature.SSE_AES256.getDescription(), "S3"));
        this.defaultEncryptionPopup.lastItem().setRepresentedObject(S3EncryptionFeature.SSE_AES256.toString());
        this.defaultEncryptionPopup.addItemWithTitle(LocaleFactory.localizedString(KMSEncryptionFeature.SSE_KMS_DEFAULT.getDescription(), "S3"));
        this.defaultEncryptionPopup.lastItem().setRepresentedObject(KMSEncryptionFeature.SSE_KMS_DEFAULT.toString());
        this.defaultEncryptionPopup.setTarget(this.id());
        this.defaultEncryptionPopup.setAction(Foundation.selector("defaultEncryptionPopupClicked:"));
        if(StringUtils.isEmpty(preferences.getProperty("s3.encryption.algorithm"))) {
            this.defaultEncryptionPopup.selectItemWithTitle(LocaleFactory.localizedString("None"));
        }
        else {
            this.defaultEncryptionPopup.selectItemAtIndex(this.defaultEncryptionPopup.indexOfItemWithRepresentedObject(preferences.getProperty("s3.encryption.algorithm")));
        }
    }

    @Action
    public void defaultEncryptionPopupClicked(NSPopUpButton sender) {
        preferences.setProperty("s3.encryption.algorithm", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton languagePopup;

    public void setLanguagePopup(NSPopUpButton b) {
        this.languagePopup = b;
        this.languagePopup.removeAllItems();
        this.languagePopup.setTarget(this.id());
        this.languagePopup.setAction(Foundation.selector("languagePopupClicked:"));
        this.languagePopup.addItemWithTitle(LocaleFactory.localizedString("Default"));
        this.languagePopup.menu().addItem(NSMenuItem.separatorItem());
        String custom = null;
        if(preferences.systemLocales().size() > 1) {
            // No user default application scope single value of AppleLanguages property is set but a list
            // of preferred languages from system preferences is returned.
            this.languagePopup.selectItemWithTitle(LocaleFactory.localizedString("Default"));
        }
        else {
            // Custom language set for this application identifier
            custom = preferences.locale();
        }
        for(String identifier : preferences.applicationLocales()) {
            this.languagePopup.addItemWithTitle(preferences.getDisplayName(identifier));
            this.languagePopup.lastItem().setRepresentedObject(identifier);
            if(identifier.equals(custom)) {
                this.languagePopup.selectItem(this.languagePopup.lastItem());
            }
        }
    }

    @Action
    public void languagePopupClicked(NSPopUpButton sender) {
        LocaleFactory.get().setDefault(sender.selectedItem().representedObject());
    }

    @Outlet
    private NSButton useProxiesButton;

    public void setUseProxiesButton(NSButton b) {
        this.useProxiesButton = b;
        this.useProxiesButton.setTarget(this.id());
        this.useProxiesButton.setAction(Foundation.selector("useProxiesButtonClicked:"));
        this.useProxiesButton.setState(preferences.getBoolean("connection.proxy.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void useProxiesButtonClicked(NSButton sender) {
        final boolean enabled = sender.state() == NSCell.NSOnState;
        preferences.setProperty("connection.proxy.enable", enabled);
        this.configureProxiesButton.setEnabled(enabled);
    }

    @Outlet
    private NSButton configureProxiesButton;

    public void setConfigureProxiesButton(NSButton b) {
        this.configureProxiesButton = b;
        this.configureProxiesButton.setTarget(this.id());
        this.configureProxiesButton.setAction(Foundation.selector("configureProxiesButtonClicked:"));
        this.configureProxiesButton.setEnabled(preferences.getBoolean("connection.proxy.enable"));
    }

    @Action
    public void configureProxiesButtonClicked(NSButton sender) {
        final String script = "tell application \"System Preferences\"\n" +
            "activate\n" +
            "reveal anchor \"Proxies\" of pane \"com.apple.preference.network\"\n" +
            "end tell";
        NSAppleScript open = NSAppleScript.createWithSource(script);
        open.executeAndReturnError(null);
    }

    @Outlet
    private NSButton cryptomatorCheckbox;

    public void setCryptomatorCheckbox(final NSButton cryptomatorCheckbox) {
        this.cryptomatorCheckbox = cryptomatorCheckbox;
        this.cryptomatorCheckbox.setTarget(this.id());
        this.cryptomatorCheckbox.setAction(Foundation.selector("cryptomatorCheckboxClicked:"));
        this.cryptomatorCheckbox.setState(preferences.getBoolean("cryptomator.vault.autodetect") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void cryptomatorCheckboxClicked(NSButton sender) {
        preferences.setProperty("cryptomator.vault.autodetect", sender.state() == NSCell.NSOnState);
    }
}
