package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.i18n.Locale;
import ch.cyberduck.core.s3.S3Session;
import ch.cyberduck.ui.cocoa.application.*;
import ch.cyberduck.ui.cocoa.foundation.*;
import ch.cyberduck.ui.cocoa.model.FinderLocal;
import ch.cyberduck.ui.cocoa.odb.EditorFactory;
import ch.cyberduck.ui.cocoa.odb.WatchEditor;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;
import ch.cyberduck.ui.cocoa.urlhandler.URLSchemeHandlerConfiguration;
import ch.cyberduck.ui.cocoa.view.BookmarkCell;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSUInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jets3t.service.model.S3Object;

import com.enterprisedt.net.ftp.FTPTransferType;

import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * @version $Id$
 */
public class PreferencesController extends ToolbarWindowController {
    private static Logger log = Logger.getLogger(PreferencesController.class);

    private static PreferencesController instance = null;

    public static PreferencesController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == instance) {
                instance = new PreferencesController();
            }
            return instance;
        }
    }

    private PreferencesController() {
        this.loadBundle();
    }

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
    private NSView panelGoogle;
    @Outlet
    private NSView panelBandwidth;
    @Outlet
    private NSView panelAdvanced;
    @Outlet
    private NSView panelUpdate;
    @Outlet
    private NSView panelLanguage;

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

    public void setPanelGoogle(NSView v) {
        this.panelGoogle = v;
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

    @Override
    protected List<NSView> getPanels() {
        return Arrays.asList(panelGeneral, panelBrowser, panelTransfer, panelEditor, panelFTP, panelSFTP, panelS3,
                panelGoogle, panelBandwidth, panelAdvanced, panelUpdate, panelLanguage);
    }

    @Override
    protected List<String> getPanelIdentifiers() {
        return Arrays.asList("general", "browser", "queue", "pencil", "ftp", "sftp", "s3", "google", "bandwidth",
                "connection", "update", "language");
    }

    @Override
    protected void invalidate() {
        BookmarkCollection.defaultCollection().removeListener(bookmarkCollectionListener);
        super.invalidate();
        instance = null;
    }

    @Override
    public void setWindow(NSWindow window) {
        window.setExcludedFromWindowsMenu(true);
        window.setFrameAutosaveName("Preferences");
        super.setWindow(window);
    }

    @Override
    protected double getMaxWindowWidth() {
        return 800;
    }

    @Override
    public void awakeFromNib() {
        this.window.center();

        this.chmodDownloadTypePopupChanged(this.chmodDownloadTypePopup);
        this.chmodUploadTypePopupChanged(this.chmodUploadTypePopup);

        boolean chmodDownloadDefaultEnabled = Preferences.instance().getBoolean("queue.download.changePermissions")
                && Preferences.instance().getBoolean("queue.download.permissions.useDefault");
        this.downerr.setEnabled(chmodDownloadDefaultEnabled);
        this.downerr.setTarget(this.id());
        this.downerr.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
        this.downerw.setEnabled(chmodDownloadDefaultEnabled);
        this.downerw.setTarget(this.id());
        this.downerw.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
        this.downerx.setEnabled(chmodDownloadDefaultEnabled);
        this.downerx.setTarget(this.id());
        this.downerx.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));

        this.dgroupr.setEnabled(chmodDownloadDefaultEnabled);
        this.dgroupr.setTarget(this.id());
        this.dgroupr.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
        this.dgroupw.setEnabled(chmodDownloadDefaultEnabled);
        this.dgroupw.setTarget(this.id());
        this.dgroupw.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
        this.dgroupx.setEnabled(chmodDownloadDefaultEnabled);
        this.dgroupx.setTarget(this.id());
        this.dgroupx.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));

        this.dotherr.setEnabled(chmodDownloadDefaultEnabled);
        this.dotherr.setTarget(this.id());
        this.dotherr.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
        this.dotherw.setEnabled(chmodDownloadDefaultEnabled);
        this.dotherw.setTarget(this.id());
        this.dotherw.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));
        this.dotherx.setEnabled(chmodDownloadDefaultEnabled);
        this.dotherx.setTarget(this.id());
        this.dotherx.setAction(Foundation.selector("defaultPermissionsDownloadChanged:"));

        boolean chmodUploadDefaultEnabled = Preferences.instance().getBoolean("queue.upload.changePermissions")
                && Preferences.instance().getBoolean("queue.upload.permissions.useDefault");
        this.uownerr.setEnabled(chmodUploadDefaultEnabled);
        this.uownerr.setTarget(this.id());
        this.uownerr.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
        this.uownerw.setEnabled(chmodUploadDefaultEnabled);
        this.uownerw.setTarget(this.id());
        this.uownerw.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
        this.uownerx.setEnabled(chmodUploadDefaultEnabled);
        this.uownerx.setTarget(this.id());
        this.uownerx.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));

        this.ugroupr.setEnabled(chmodUploadDefaultEnabled);
        this.ugroupr.setTarget(this.id());
        this.ugroupr.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
        this.ugroupw.setEnabled(chmodUploadDefaultEnabled);
        this.ugroupw.setTarget(this.id());
        this.ugroupw.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
        this.ugroupx.setEnabled(chmodUploadDefaultEnabled);
        this.ugroupx.setTarget(this.id());
        this.ugroupx.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));

        this.uotherr.setEnabled(chmodUploadDefaultEnabled);
        this.uotherr.setTarget(this.id());
        this.uotherr.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
        this.uotherw.setEnabled(chmodUploadDefaultEnabled);
        this.uotherw.setTarget(this.id());
        this.uotherw.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));
        this.uotherx.setEnabled(chmodUploadDefaultEnabled);
        this.uotherx.setTarget(this.id());
        this.uotherx.setAction(Foundation.selector("defaultPermissionsUploadChanged:"));

        super.awakeFromNib();
    }

    private static final String TRANSFERMODE_AUTO = Locale.localizedString("Auto");
    private static final String TRANSFERMODE_BINARY = Locale.localizedString("Binary");
    private static final String TRANSFERMODE_ASCII = Locale.localizedString("ASCII");

    private static final String UNIX_LINE_ENDINGS = Locale.localizedString("Unix Line Endings (LF)");
    private static final String MAC_LINE_ENDINGS = Locale.localizedString("Mac Line Endings (CR)");
    private static final String WINDOWS_LINE_ENDINGS = Locale.localizedString("Windows Line Endings (CRLF)");

    // ----------------------------------------------------------
    // Outlets
    // ----------------------------------------------------------

    @Outlet
    private NSPopUpButton editorCombobox;

    public void setEditorCombobox(NSPopUpButton b) {
        this.editorCombobox = b;
        this.editorCombobox.setAutoenablesItems(false);
        this.updateEditorCombobox();
    }

    private void updateEditorCombobox() {
        editorCombobox.removeAllItems();
        Map<String, String> editors = EditorFactory.getSupportedEditors();
        Iterator<String> editorNames = editors.keySet().iterator();
        Iterator<String> editorIdentifiers = editors.values().iterator();
        while(editorNames.hasNext()) {
            this.addEditor(editorNames.next(), editorIdentifiers.next());
        }
        editorCombobox.setTarget(this.id());
        final Selector action = Foundation.selector("editorComboboxClicked:");
        editorCombobox.setAction(action);
        if(Preferences.instance().getBoolean("editor.kqueue.enable")) {
            editorCombobox.menu().addItem(NSMenuItem.separatorItem());
            editorCombobox.menu().addItemWithTitle_action_keyEquivalent(CHOOSE, action, "");
            editorCombobox.lastItem().setTarget(this.id());
        }
        editorCombobox.selectItemWithTitle(
                EditorFactory.getApplicationName(EditorFactory.defaultEditor()));
    }

    private void addEditor(String editor, String identifier) {
        editorCombobox.addItemWithTitle(editor);
        final boolean enabled = EditorFactory.getInstalledEditors().containsValue(identifier);
        editorCombobox.itemWithTitle(editor).setEnabled(enabled);
        if(enabled) {
            editorCombobox.itemWithTitle(editor).setImage(IconCache.instance().iconForApplication(identifier, 16));
        }
    }

    private NSOpenPanel editorPathPanel;
    private ProxyController editorPathPanelDelegate = new EditorOpenPanelDelegate();

    @Action
    public void editorComboboxClicked(NSPopUpButton sender) {
        if(sender.title().equals(CHOOSE)) {
            editorPathPanel = NSOpenPanel.openPanel();
            editorPathPanel.setDelegate(editorPathPanelDelegate.id());
            editorPathPanel.setAllowsMultipleSelection(false);
            editorPathPanel.setCanCreateDirectories(false);
            editorPathPanel.beginSheetForDirectory("/Applications", null, this.window, this.id(),
                    Foundation.selector("editorPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            final String selected = EditorFactory.getSupportedEditors().get(sender.titleOfSelectedItem());
            Preferences.instance().setProperty("editor.bundleIdentifier", selected);
            BrowserController.validateToolbarItems();
        }
    }

    private static class EditorOpenPanelDelegate extends ProxyController {
        public boolean panel_shouldShowFilename(ID panel, String path) {
            final Local f = LocalFactory.createLocal(path);
            if(f.attributes().isDirectory()) {
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
                String path = LocalFactory.createLocal(filename).getAbsolute();
                NSBundle app = NSBundle.bundleWithPath(path);
                if(null == app) {
                    log.error("Loading bundle failed:" + path);
                }
                else {
                    final String bundleIdentifier = app.bundleIdentifier();
                    if(!EditorFactory.getInstalledEditors().values().contains(bundleIdentifier)) {
                        WatchEditor.addInstalledEditor(EditorFactory.getApplicationName(bundleIdentifier), app.bundleIdentifier());
                    }
                    Preferences.instance().setProperty("editor.bundleIdentifier", bundleIdentifier);
                    BrowserController.validateToolbarItems();
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
        this.defaultEditorCheckbox.setState(Preferences.instance().getBoolean("editor.alwaysUseDefault") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    public void defaultEditorCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("editor.alwaysUseDefault", enabled);
    }

    @Outlet
    private NSPopUpButton bookmarkSizePopup;

    public void setBookmarkSizePopup(NSPopUpButton b) {
        this.bookmarkSizePopup = b;
        this.bookmarkSizePopup.setTarget(this.id());
        this.bookmarkSizePopup.setAction(Foundation.selector("bookmarkSizePopupClicked:"));
        final int size = Preferences.instance().getInteger("bookmark.icon.size");
//        this.bookmarkSizePopup.itemAtIndex(0).setImage(IconCache.iconNamed("ftp", CDBookmarkCell.SMALL_BOOKMARK_SIZE));
//        this.bookmarkSizePopup.itemAtIndex(1).setImage(IconCache.iconNamed("ftp", CDBookmarkCell.MEDIUM_BOOKMARK_SIZE));
//        this.bookmarkSizePopup.itemAtIndex(2).setImage(IconCache.iconNamed("ftp", CDBookmarkCell.LARGE_BOOKMARK_SIZE));
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
            Preferences.instance().setProperty("bookmark.icon.size", BookmarkCell.SMALL_BOOKMARK_SIZE);
        }
        if(sender.indexOfSelectedItem().intValue() == 1) {
            Preferences.instance().setProperty("bookmark.icon.size", BookmarkCell.MEDIUM_BOOKMARK_SIZE);
        }
        if(sender.indexOfSelectedItem().intValue() == 2) {
            Preferences.instance().setProperty("bookmark.icon.size", BookmarkCell.LARGE_BOOKMARK_SIZE);
        }
        BrowserController.updateBookmarkTableRowHeight();
    }

    @Outlet
    private NSButton openUntitledBrowserCheckbox;

    public void setOpenUntitledBrowserCheckbox(NSButton b) {
        this.defaultEditorCheckbox = b;
        this.defaultEditorCheckbox.setTarget(this.id());
        this.defaultEditorCheckbox.setAction(Foundation.selector("openUntitledBrowserCheckboxClicked:"));
        this.defaultEditorCheckbox.setState(Preferences.instance().getBoolean("browser.openUntitled") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void openUntitledBrowserCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.openUntitled", enabled);
    }

    @Outlet
    private NSButton browserSerializeCheckbox;

    public void setBrowserSerializeCheckbox(NSButton b) {
        this.browserSerializeCheckbox = b;
        this.browserSerializeCheckbox.setTarget(this.id());
        this.browserSerializeCheckbox.setAction(Foundation.selector("browserSerializeCheckboxClicked:"));
        this.browserSerializeCheckbox.setState(Preferences.instance().getBoolean("browser.serialize") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void browserSerializeCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.serialize", enabled);
    }

    @Outlet
    private NSPopUpButton defaultBookmarkCombobox;

    private final CollectionListener<Host> bookmarkCollectionListener = new AbstractCollectionListener<Host>() {
        @Override
        public void collectionItemAdded(final Host bookmark) {
            invoke(new WindowMainAction(PreferencesController.this) {
                public void run() {
                    defaultBookmarkCombobox.addItemWithTitle(bookmark.getNickname());
                    defaultBookmarkCombobox.lastItem().setImage(IconCache.iconNamed("cyberduck-document", 16));
                    defaultBookmarkCombobox.lastItem().setRepresentedObject(bookmark.getUuid());
                }
            });
        }

        @Override
        public void collectionItemRemoved(final Host bookmark) {
            invoke(new WindowMainAction(PreferencesController.this) {
                public void run() {
                    if(defaultBookmarkCombobox.selectedItem().representedObject().equals(bookmark.getUuid())) {
                        Preferences.instance().deleteProperty("browser.defaultBookmark");
                    }
                    NSInteger i = defaultBookmarkCombobox.menu().indexOfItemWithRepresentedObject(bookmark.getUuid());
                    if(i.intValue() > -1) {
                        defaultBookmarkCombobox.removeItemAtIndex(i);
                    }
                }
            });
        }
    };

    public void setDefaultBookmarkCombobox(NSPopUpButton b) {
        this.defaultBookmarkCombobox = b;
        this.defaultBookmarkCombobox.setToolTip(Locale.localizedString("Bookmarks"));
        this.defaultBookmarkCombobox.removeAllItems();
        this.defaultBookmarkCombobox.addItemWithTitle(Locale.localizedString("None"));
        this.defaultBookmarkCombobox.selectItem(this.defaultBookmarkCombobox.lastItem());
        this.defaultBookmarkCombobox.menu().addItem(NSMenuItem.separatorItem());
        for(Host bookmark : BookmarkCollection.defaultCollection()) {
            this.defaultBookmarkCombobox.addItemWithTitle(bookmark.getNickname());
            this.defaultBookmarkCombobox.lastItem().setImage(
                    IconCache.iconNamed(bookmark.getProtocol().icon(), 16));
            this.defaultBookmarkCombobox.lastItem().setRepresentedObject(bookmark.getUuid());
            if(bookmark.getUuid().equals(Preferences.instance().getProperty("browser.defaultBookmark"))) {
                this.defaultBookmarkCombobox.selectItem(this.defaultBookmarkCombobox.lastItem());
            }
        }
        BookmarkCollection.defaultCollection().addListener(bookmarkCollectionListener);
        this.defaultBookmarkCombobox.setTarget(this.id());
        final Selector action = Foundation.selector("defaultBookmarkComboboxClicked:");
        this.defaultBookmarkCombobox.setAction(action);
    }

    @Action
    public void defaultBookmarkComboboxClicked(NSPopUpButton sender) {
        final String selected = sender.selectedItem().representedObject();
        if(null == selected) {
            Preferences.instance().deleteProperty("browser.defaultBookmark");
        }
        Preferences.instance().setProperty("browser.defaultBookmark", selected);
    }

    @Outlet
    private NSPopUpButton encodingCombobox;

    public void setEncodingCombobox(NSPopUpButton b) {
        this.encodingCombobox = b;
        this.encodingCombobox.setTarget(this.id());
        this.encodingCombobox.setAction(Foundation.selector("encodingComboboxClicked:"));
        this.encodingCombobox.removeAllItems();
        this.encodingCombobox.addItemsWithTitles(NSArray.arrayWithObjects(MainController.availableCharsets()));
        this.encodingCombobox.selectItemWithTitle(Preferences.instance().getProperty("browser.charset.encoding"));
    }

    @Action
    public void encodingComboboxClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("browser.charset.encoding", sender.titleOfSelectedItem());
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
            p = new Permission(Preferences.instance().getInteger("queue.upload.permissions.file.default"));
        }
        if(sender.selectedItem().tag() == 1) {
            p = new Permission(Preferences.instance().getInteger("queue.upload.permissions.folder.default"));
        }
        if(null == p) {
            log.error("No selected item for:" + sender);
            return;
        }
        boolean[] ownerPerm = p.getOwnerPermissions();
        boolean[] groupPerm = p.getGroupPermissions();
        boolean[] otherPerm = p.getOtherPermissions();

        uownerr.setState(ownerPerm[Permission.READ] ? NSCell.NSOnState : NSCell.NSOffState);
        uownerw.setState(ownerPerm[Permission.WRITE] ? NSCell.NSOnState : NSCell.NSOffState);
        uownerx.setState(ownerPerm[Permission.EXECUTE] ? NSCell.NSOnState : NSCell.NSOffState);

        ugroupr.setState(groupPerm[Permission.READ] ? NSCell.NSOnState : NSCell.NSOffState);
        ugroupw.setState(groupPerm[Permission.WRITE] ? NSCell.NSOnState : NSCell.NSOffState);
        ugroupx.setState(groupPerm[Permission.EXECUTE] ? NSCell.NSOnState : NSCell.NSOffState);

        uotherr.setState(otherPerm[Permission.READ] ? NSCell.NSOnState : NSCell.NSOffState);
        uotherw.setState(otherPerm[Permission.WRITE] ? NSCell.NSOnState : NSCell.NSOffState);
        uotherx.setState(otherPerm[Permission.EXECUTE] ? NSCell.NSOnState : NSCell.NSOffState);
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
            p = new Permission(Preferences.instance().getInteger("queue.download.permissions.file.default"));
        }
        if(sender.selectedItem().tag() == 1) {
            p = new Permission(Preferences.instance().getInteger("queue.download.permissions.folder.default"));
        }
        if(null == p) {
            log.error("No selected item for:" + sender);
            return;
        }
        boolean[] ownerPerm = p.getOwnerPermissions();
        boolean[] groupPerm = p.getGroupPermissions();
        boolean[] otherPerm = p.getOtherPermissions();

        downerr.setState(ownerPerm[Permission.READ] ? NSCell.NSOnState : NSCell.NSOffState);
        downerw.setState(ownerPerm[Permission.WRITE] ? NSCell.NSOnState : NSCell.NSOffState);
        downerx.setState(ownerPerm[Permission.EXECUTE] ? NSCell.NSOnState : NSCell.NSOffState);

        dgroupr.setState(groupPerm[Permission.READ] ? NSCell.NSOnState : NSCell.NSOffState);
        dgroupw.setState(groupPerm[Permission.WRITE] ? NSCell.NSOnState : NSCell.NSOffState);
        dgroupx.setState(groupPerm[Permission.EXECUTE] ? NSCell.NSOnState : NSCell.NSOffState);

        dotherr.setState(otherPerm[Permission.READ] ? NSCell.NSOnState : NSCell.NSOffState);
        dotherw.setState(otherPerm[Permission.WRITE] ? NSCell.NSOnState : NSCell.NSOffState);
        dotherx.setState(otherPerm[Permission.EXECUTE] ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Outlet
    private NSButton chmodUploadCheckbox;

    public void setChmodUploadCheckbox(NSButton b) {
        this.chmodUploadCheckbox = b;
        this.chmodUploadCheckbox.setTarget(this.id());
        this.chmodUploadCheckbox.setAction(Foundation.selector("chmodUploadCheckboxClicked:"));
        this.chmodUploadCheckbox.setState(Preferences.instance().getBoolean("queue.upload.changePermissions") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void chmodUploadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.upload.changePermissions", enabled);
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
        this.chmodUploadDefaultCheckbox.setState(Preferences.instance().getBoolean("queue.upload.permissions.useDefault") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodUploadDefaultCheckbox.setEnabled(Preferences.instance().getBoolean("queue.upload.changePermissions"));
    }

    @Action
    public void chmodUploadDefaultCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.upload.permissions.useDefault", enabled);
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
        this.chmodUploadCustomCheckbox.setState(!Preferences.instance().getBoolean("queue.upload.permissions.useDefault") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodUploadCustomCheckbox.setEnabled(Preferences.instance().getBoolean("queue.upload.changePermissions"));
    }

    @Action
    public void chmodUploadCustomCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.upload.permissions.useDefault", !enabled);
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
        this.chmodDownloadCheckbox.setState(Preferences.instance().getBoolean("queue.download.changePermissions") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void chmodDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.download.changePermissions", enabled);
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
        this.chmodDownloadDefaultCheckbox.setState(Preferences.instance().getBoolean("queue.download.permissions.useDefault") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodDownloadDefaultCheckbox.setEnabled(Preferences.instance().getBoolean("queue.download.changePermissions"));
    }

    @Action
    public void chmodDownloadDefaultCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.download.permissions.useDefault", enabled);
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
        this.chmodDownloadCustomCheckbox.setState(!Preferences.instance().getBoolean("queue.download.permissions.useDefault") ? NSCell.NSOnState : NSCell.NSOffState);
        this.chmodDownloadCustomCheckbox.setEnabled(Preferences.instance().getBoolean("queue.download.changePermissions"));
    }

    @Action
    public void chmodDownloadCustomCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.download.permissions.useDefault", !enabled);
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

    public void setDownerr(NSButton downerr) {
        this.downerr = downerr;
    }

    public void setDownerw(NSButton downerw) {
        this.downerw = downerw;
    }

    public void setDownerx(NSButton downerx) {
        this.downerx = downerx;
    }

    public void setDgroupr(NSButton dgroupr) {
        this.dgroupr = dgroupr;
    }

    public void setDgroupw(NSButton dgroupw) {
        this.dgroupw = dgroupw;
    }

    public void setDgroupx(NSButton dgroupx) {
        this.dgroupx = dgroupx;
    }

    public void setDotherr(NSButton dotherr) {
        this.dotherr = dotherr;
    }

    public void setDotherw(NSButton dotherw) {
        this.dotherw = dotherw;
    }

    public void setDotherx(NSButton dotherx) {
        this.dotherx = dotherx;
    }

    public void setUownerr(NSButton uownerr) {
        this.uownerr = uownerr;
    }

    public void setUownerw(NSButton uownerw) {
        this.uownerw = uownerw;
    }

    public void setUownerx(NSButton uownerx) {
        this.uownerx = uownerx;
    }

    public void setUgroupr(NSButton ugroupr) {
        this.ugroupr = ugroupr;
    }

    public void setUgroupw(NSButton ugroupw) {
        this.ugroupw = ugroupw;
    }

    public void setUgroupx(NSButton ugroupx) {
        this.ugroupx = ugroupx;
    }

    public void setUotherr(NSButton uotherr) {
        this.uotherr = uotherr;
    }

    public void setUotherw(NSButton uotherw) {
        this.uotherw = uotherw;
    }

    public void setUotherx(NSButton uotherx) {
        this.uotherx = uotherx;
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
    private NSButton dotherr; //IBOutletdownerr
    @Outlet
    private NSButton dotherw;
    @Outlet
    private NSButton dotherx;

    public void defaultPermissionsDownloadChanged(final ID sender) {
        boolean[][] p = new boolean[3][3];

        p[Permission.OWNER][Permission.READ] = (downerr.state() == NSCell.NSOnState);
        p[Permission.OWNER][Permission.WRITE] = (downerw.state() == NSCell.NSOnState);
        p[Permission.OWNER][Permission.EXECUTE] = (downerx.state() == NSCell.NSOnState);

        p[Permission.GROUP][Permission.READ] = (dgroupr.state() == NSCell.NSOnState);
        p[Permission.GROUP][Permission.WRITE] = (dgroupw.state() == NSCell.NSOnState);
        p[Permission.GROUP][Permission.EXECUTE] = (dgroupx.state() == NSCell.NSOnState);

        p[Permission.OTHER][Permission.READ] = (dotherr.state() == NSCell.NSOnState);
        p[Permission.OTHER][Permission.WRITE] = (dotherw.state() == NSCell.NSOnState);
        p[Permission.OTHER][Permission.EXECUTE] = (dotherx.state() == NSCell.NSOnState);

        Permission permission = new Permission(p);
        if(chmodDownloadTypePopup.selectedItem().tag() == 0) {
            Preferences.instance().setProperty("queue.download.permissions.file.default", permission.getOctalString());
        }
        if(chmodDownloadTypePopup.selectedItem().tag() == 1) {
            Preferences.instance().setProperty("queue.download.permissions.folder.default", permission.getOctalString());
        }
    }

    public NSButton uownerr;
    public NSButton uownerw;
    public NSButton uownerx;
    public NSButton ugroupr;
    public NSButton ugroupw;
    public NSButton ugroupx;
    public NSButton uotherr;
    public NSButton uotherw;
    public NSButton uotherx;

    @Action
    public void defaultPermissionsUploadChanged(final NSButton sender) {
        boolean[][] p = new boolean[3][3];

        p[Permission.OWNER][Permission.READ] = (uownerr.state() == NSCell.NSOnState);
        p[Permission.OWNER][Permission.WRITE] = (uownerw.state() == NSCell.NSOnState);
        p[Permission.OWNER][Permission.EXECUTE] = (uownerx.state() == NSCell.NSOnState);

        p[Permission.GROUP][Permission.READ] = (ugroupr.state() == NSCell.NSOnState);
        p[Permission.GROUP][Permission.WRITE] = (ugroupw.state() == NSCell.NSOnState);
        p[Permission.GROUP][Permission.EXECUTE] = (ugroupx.state() == NSCell.NSOnState);

        p[Permission.OTHER][Permission.READ] = (uotherr.state() == NSCell.NSOnState);
        p[Permission.OTHER][Permission.WRITE] = (uotherw.state() == NSCell.NSOnState);
        p[Permission.OTHER][Permission.EXECUTE] = (uotherx.state() == NSCell.NSOnState);

        Permission permission = new Permission(p);
        if(chmodUploadTypePopup.selectedItem().tag() == 0) {
            Preferences.instance().setProperty("queue.upload.permissions.file.default", permission.getOctalString());
        }
        if(chmodUploadTypePopup.selectedItem().tag() == 1) {
            Preferences.instance().setProperty("queue.upload.permissions.folder.default", permission.getOctalString());
        }
    }

    @Outlet
    private NSButton preserveModificationDownloadCheckbox;

    public void setPreserveModificationDownloadCheckbox(NSButton b) {
        this.preserveModificationDownloadCheckbox = b;
        this.preserveModificationDownloadCheckbox.setTarget(this.id());
        this.preserveModificationDownloadCheckbox.setAction(Foundation.selector("preserveModificationDownloadCheckboxClicked:"));
        this.preserveModificationDownloadCheckbox.setState(Preferences.instance().getBoolean("queue.download.preserveDate") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void preserveModificationDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.download.preserveDate", enabled);
    }

    @Outlet
    private NSButton preserveModificationUploadCheckbox;

    public void setPreserveModificationUploadCheckbox(NSButton b) {
        this.preserveModificationUploadCheckbox = b;
        this.preserveModificationUploadCheckbox.setTarget(this.id());
        this.preserveModificationUploadCheckbox.setAction(Foundation.selector("preserveModificationUploadCheckboxClicked:"));
        this.preserveModificationUploadCheckbox.setState(Preferences.instance().getBoolean("queue.upload.preserveDate") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void preserveModificationUploadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.upload.preserveDate", enabled);
    }

    @Outlet
    private NSButton horizontalLinesCheckbox;

    public void setHorizontalLinesCheckbox(NSButton b) {
        this.horizontalLinesCheckbox = b;
        this.horizontalLinesCheckbox.setTarget(this.id());
        this.horizontalLinesCheckbox.setAction(Foundation.selector("horizontalLinesCheckboxClicked:"));
        this.horizontalLinesCheckbox.setState(Preferences.instance().getBoolean("browser.horizontalLines") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void horizontalLinesCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.horizontalLines", enabled);
        BrowserController.updateBrowserTableAttributes();
    }

    @Outlet
    private NSButton verticalLinesCheckbox;

    public void setVerticalLinesCheckbox(NSButton b) {
        this.verticalLinesCheckbox = b;
        this.verticalLinesCheckbox.setTarget(this.id());
        this.verticalLinesCheckbox.setAction(Foundation.selector("verticalLinesCheckboxClicked:"));
        this.verticalLinesCheckbox.setState(Preferences.instance().getBoolean("browser.verticalLines") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void verticalLinesCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.verticalLines", enabled);
        BrowserController.updateBrowserTableAttributes();
    }

    @Outlet
    private NSButton alternatingRowBackgroundCheckbox;

    public void setAlternatingRowBackgroundCheckbox(NSButton b) {
        this.alternatingRowBackgroundCheckbox = b;
        this.alternatingRowBackgroundCheckbox.setTarget(this.id());
        this.alternatingRowBackgroundCheckbox.setAction(Foundation.selector("alternatingRowBackgroundCheckboxClicked:"));
        this.alternatingRowBackgroundCheckbox.setState(Preferences.instance().getBoolean("browser.alternatingRows") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void alternatingRowBackgroundCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.alternatingRows", enabled);
        BrowserController.updateBrowserTableAttributes();
    }

    @Outlet
    private NSButton infoWindowAsInspectorCheckbox;

    public void setInfoWindowAsInspectorCheckbox(NSButton b) {
        this.infoWindowAsInspectorCheckbox = b;
        this.infoWindowAsInspectorCheckbox.setTarget(this.id());
        this.infoWindowAsInspectorCheckbox.setAction(Foundation.selector("infoWindowAsInspectorCheckboxClicked:"));
        this.infoWindowAsInspectorCheckbox.setState(Preferences.instance().getBoolean("browser.info.isInspector") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void infoWindowAsInspectorCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.info.isInspector", enabled);
    }

    @Outlet
    private NSPopUpButton downloadPathPopup;

    private static final String CHOOSE = Locale.localizedString("Choose") + "…";

    // The currently set download folder
    private final Local DEFAULT_DOWNLOAD_FOLDER = LocalFactory.createLocal(Preferences.instance().getProperty("queue.download.folder"));

    public void setDownloadPathPopup(NSPopUpButton b) {
        this.downloadPathPopup = b;
        this.downloadPathPopup.setTarget(this.id());
        final Selector action = Foundation.selector("downloadPathPopupClicked:");
        this.downloadPathPopup.setAction(action);
        this.downloadPathPopup.removeAllItems();
        // Default download folder
        this.addDownloadPath(action, DEFAULT_DOWNLOAD_FOLDER);
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        // Shortcut to the Desktop
        this.addDownloadPath(action, LocalFactory.createLocal("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(action, LocalFactory.createLocal("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(action, LocalFactory.createLocal("~/Downloads"));
        // Choose another folder
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(CHOOSE, action, "");
        this.downloadPathPopup.lastItem().setTarget(this.id());
    }

    private void addDownloadPath(Selector action, Local f) {
        this.downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(f.getDisplayName(), action, "");
        this.downloadPathPopup.lastItem().setTarget(this.id());
        this.downloadPathPopup.lastItem().setImage(
                IconCache.instance().iconForPath(f, 16)
        );
        this.downloadPathPopup.lastItem().setRepresentedObject(
                f.getAbsolute());
        if(DEFAULT_DOWNLOAD_FOLDER.equals(f)) {
            this.downloadPathPopup.selectItem(this.downloadPathPopup.lastItem());
        }
    }

    private NSOpenPanel downloadPathPanel;

    @Action
    public void downloadPathPopupClicked(final NSMenuItem sender) {
        if(sender.title().equals(CHOOSE)) {
            downloadPathPanel = NSOpenPanel.openPanel();
            downloadPathPanel.setCanChooseFiles(false);
            downloadPathPanel.setCanChooseDirectories(true);
            downloadPathPanel.setAllowsMultipleSelection(false);
            downloadPathPanel.setCanCreateDirectories(true);
            downloadPathPanel.beginSheetForDirectory(null, null, this.window, this.id(),
                    Foundation.selector("downloadPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            Preferences.instance().setProperty("queue.download.folder", LocalFactory.createLocal(
                    sender.representedObject()).getAbbreviatedPath());
        }
    }

    public void downloadPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, int returncode, ID contextInfo) {
        if(returncode == SheetCallback.DEFAULT_OPTION) {
            NSArray selected = sheet.filenames();
            String filename;
            if((filename = selected.lastObject().toString()) != null) {
                Preferences.instance().setProperty("queue.download.folder",
                        LocalFactory.createLocal(filename).getAbbreviatedPath());
            }
        }
        Local custom = LocalFactory.createLocal(Preferences.instance().getProperty("queue.download.folder"));
        downloadPathPopup.itemAtIndex(new NSInteger(0)).setTitle(custom.getDisplayName());
        downloadPathPopup.itemAtIndex(new NSInteger(0)).setRepresentedObject(custom.getAbsolute());
        downloadPathPopup.itemAtIndex(new NSInteger(0)).setImage(IconCache.instance().iconForPath(custom, 16));
        downloadPathPopup.selectItemAtIndex(new NSInteger(0));
        downloadPathPanel = null;
    }

    @Outlet
    private NSPopUpButton transferPopup;

    public void setTransferPopup(NSPopUpButton b) {
        this.transferPopup = b;
        this.transferPopup.setTarget(this.id());
        this.transferPopup.setAction(Foundation.selector("transferPopupClicked:"));
        this.transferPopup.selectItemAtIndex(
                new NSInteger(Preferences.instance().getInteger("connection.host.max") == 1 ? USE_BROWSER_SESSION_INDEX : USE_QUEUE_SESSION_INDEX));
    }

    private static final int USE_QUEUE_SESSION_INDEX = 0;
    private static final int USE_BROWSER_SESSION_INDEX = 1;

    @Action
    public void transferPopupClicked(final NSPopUpButton sender) {
        if(sender.indexOfSelectedItem().intValue() == USE_BROWSER_SESSION_INDEX) {
            Preferences.instance().setProperty("connection.host.max", 1);
        }
        else if(sender.indexOfSelectedItem().intValue() == USE_QUEUE_SESSION_INDEX) {
            Preferences.instance().setProperty("connection.host.max", -1);
        }
    }

    @Outlet
    private NSTextField anonymousField;

    public void setAnonymousField(NSTextField f) {
        this.anonymousField = f;
        this.anonymousField.setStringValue(Preferences.instance().getProperty("connection.login.anon.pass"));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("anonymousFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.anonymousField);
    }

    public void anonymousFieldDidChange(NSNotification sender) {
        Preferences.instance().setProperty("connection.login.anon.pass", this.anonymousField.stringValue());
    }

    @Outlet
    private NSTextField textFileTypeRegexField;

    public void setTextFileTypeRegexField(NSTextField f) {
        this.textFileTypeRegexField = f;
        this.textFileTypeRegexField.setStringValue(Preferences.instance().getProperty("filetype.text.regex"));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("textFileTypeRegexFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.textFileTypeRegexField);
    }

    public void textFileTypeRegexFieldDidChange(NSNotification sender) {
        String value = this.textFileTypeRegexField.stringValue().trim();
        try {
            Pattern compiled = Pattern.compile(value);
            this.textFileTypeRegexField.setTextColor(NSColor.controlTextColor());
            Preferences.instance().setProperty("filetype.text.regex",
                    compiled.pattern());
        }
        catch(PatternSyntaxException e) {
            this.textFileTypeRegexField.setTextColor(NSColor.redColor());
        }
    }

//    @Outlet private NSTextField binaryFileTypeRegexField;
//
//    public void setBinaryFileTypeRegexField(NSTextField binaryFileTypeRegexField) {
//        this.binaryFileTypeRegexField = binaryFileTypeRegexField;
//        this.binaryFileTypeRegexField.setStringValue(Preferences.instance().getProperty("filetype.binary.regex"));
//        NSNotificationCenter.defaultCenter().addObserver(this.id(),
//                Foundation.selector("binaryFileTypeRegexFieldDidChange:"),
//                NSControl.ControlTextDidChangeNotification,
//                this.binaryFileTypeRegexField);
//    }
//
//    public void binaryFileTypeRegexFieldDidChange(NSNotification sender) {
//        String value = this.binaryFileTypeRegexField.stringValue().trim();
//        try {
//            Pattern compiled = Pattern.compile(value);
//            Preferences.instance().setProperty("filetype.binary.regex",
//                    compiled.pattern());
//        }
//        catch(PatternSyntaxException e) {
//            log.warn("Invalid regex:"+e.getMessage());
//        }
//    }

    @Outlet
    private NSButton downloadSkipButton;

    public void setDownloadSkipButton(NSButton b) {
        this.downloadSkipButton = b;
        this.downloadSkipButton.setTarget(this.id());
        this.downloadSkipButton.setAction(Foundation.selector("downloadSkipButtonClicked:"));
        this.downloadSkipButton.setState(Preferences.instance().getBoolean("queue.download.skip.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void downloadSkipButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        downloadSkipRegexField.setSelectable(enabled);
        downloadSkipRegexField.setEditable(enabled);
        downloadSkipRegexField.setTextColor(enabled ? NSColor.controlTextColor() : NSColor.disabledControlTextColor());
        Preferences.instance().setProperty("queue.download.skip.enable", enabled);
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
        final String regex = Preferences.instance().getProperty("queue.download.skip.regex.default");
        this.downloadSkipRegexField.setString(regex);
        Preferences.instance().setProperty("queue.download.skip.regex", regex);
    }

    private NSTextView downloadSkipRegexField;

    public void setDownloadSkipRegexField(NSTextView t) {
        this.downloadSkipRegexField = t;
        this.downloadSkipRegexField.setFont(NSFont.userFixedPitchFontOfSize(9.0f));
        this.downloadSkipRegexField.setString(Preferences.instance().getProperty("queue.download.skip.regex"));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("downloadSkipRegexFieldDidChange:"),
                NSText.TextDidChangeNotification,
                this.downloadSkipRegexField);
    }

    public void downloadSkipRegexFieldDidChange(NSNotification sender) {
        String value = this.downloadSkipRegexField.string().trim();
        if("".equals(value)) {
            Preferences.instance().setProperty("queue.download.skip.enable", false);
            Preferences.instance().setProperty("queue.download.skip.regex", value);
            this.downloadSkipButton.setState(NSCell.NSOffState);
        }
        try {
            Pattern compiled = Pattern.compile(value);
            Preferences.instance().setProperty("queue.download.skip.regex",
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
        this.uploadSkipButton.setState(Preferences.instance().getBoolean("queue.upload.skip.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void uploadSkipButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        uploadSkipRegexField.setSelectable(enabled);
        uploadSkipRegexField.setEditable(enabled);
        uploadSkipRegexField.setTextColor(enabled ? NSColor.controlTextColor() : NSColor.disabledControlTextColor());
        Preferences.instance().setProperty("queue.upload.skip.enable", enabled);
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
        final String regex = Preferences.instance().getProperty("queue.upload.skip.regex.default");
        this.uploadSkipRegexField.setString(regex);
        Preferences.instance().setProperty("queue.upload.skip.regex", regex);
    }

    private NSTextView uploadSkipRegexField;

    public void setUploadSkipRegexField(NSTextView b) {
        this.uploadSkipRegexField = b;
        this.uploadSkipRegexField.setFont(NSFont.userFixedPitchFontOfSize(9.0f));
        this.uploadSkipRegexField.setString(Preferences.instance().getProperty("queue.upload.skip.regex"));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("uploadSkipRegexFieldDidChange:"),
                NSText.TextDidChangeNotification,
                this.uploadSkipRegexField);
    }

    public void uploadSkipRegexFieldDidChange(NSNotification sender) {
        String value = this.uploadSkipRegexField.string().trim();
        if("".equals(value)) {
            Preferences.instance().setProperty("queue.upload.skip.enable", false);
            Preferences.instance().setProperty("queue.upload.skip.regex", value);
            this.uploadSkipButton.setState(NSCell.NSOffState);
        }
        try {
            Pattern compiled = Pattern.compile(value);
            Preferences.instance().setProperty("queue.upload.skip.regex",
                    compiled.pattern());
            this.mark(this.uploadSkipRegexField.textStorage(), null);
        }
        catch(PatternSyntaxException e) {
            this.mark(this.uploadSkipRegexField.textStorage(), e);
        }
    }

    protected static NSDictionary RED_FONT = NSDictionary.dictionaryWithObjectsForKeys(
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
    private NSTextField loginField;

    /**
     * Default SSH login name
     *
     * @param f
     */
    public void setLoginField(NSTextField f) {
        this.loginField = f;
        this.loginField.setStringValue(Preferences.instance().getProperty("connection.login.name"));
        NSNotificationCenter.defaultCenter().addObserver(this.id(),
                Foundation.selector("loginFieldDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.loginField);
    }

    public void loginFieldDidChange(NSNotification sender) {
        Preferences.instance().setProperty("connection.login.name", this.loginField.stringValue());
    }

    @Outlet
    private NSButton keychainCheckbox;

    public void setKeychainCheckbox(NSButton b) {
        this.keychainCheckbox = b;
        this.keychainCheckbox.setTarget(this.id());
        this.keychainCheckbox.setAction(Foundation.selector("keychainCheckboxClicked:"));
        this.keychainCheckbox.setState(Preferences.instance().getBoolean("connection.login.useKeychain") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void keychainCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("connection.login.useKeychain", enabled);
    }

    @Outlet
    private NSButton doubleClickCheckbox;

    public void setDoubleClickCheckbox(NSButton b) {
        this.doubleClickCheckbox = b;
        this.doubleClickCheckbox.setTarget(this.id());
        this.doubleClickCheckbox.setAction(Foundation.selector("doubleClickCheckboxClicked:"));
        this.doubleClickCheckbox.setState(Preferences.instance().getBoolean("browser.doubleclick.edit") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void doubleClickCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.doubleclick.edit", enabled);
    }

    @Outlet
    private NSButton returnKeyCheckbox;

    public void setReturnKeyCheckbox(NSButton b) {
        this.returnKeyCheckbox = b;
        this.returnKeyCheckbox.setTarget(this.id());
        this.returnKeyCheckbox.setAction(Foundation.selector("returnKeyCheckboxClicked:"));
        this.returnKeyCheckbox.setState(Preferences.instance().getBoolean("browser.enterkey.rename") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void returnKeyCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.enterkey.rename", enabled);
    }

    @Outlet
    private NSButton showHiddenCheckbox;

    public void setShowHiddenCheckbox(NSButton b) {
        this.showHiddenCheckbox = b;
        this.showHiddenCheckbox.setTarget(this.id());
        this.showHiddenCheckbox.setAction(Foundation.selector("showHiddenCheckboxClicked:"));
        this.showHiddenCheckbox.setState(Preferences.instance().getBoolean("browser.showHidden") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void showHiddenCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.showHidden", enabled);
    }

    @Outlet
    private NSButton bringQueueToFrontCheckbox;

    public void setBringQueueToFrontCheckbox(NSButton b) {
        this.bringQueueToFrontCheckbox = b;
        this.bringQueueToFrontCheckbox.setTarget(this.id());
        this.bringQueueToFrontCheckbox.setAction(Foundation.selector("bringQueueToFrontCheckboxClicked:"));
        this.bringQueueToFrontCheckbox.setState(Preferences.instance().getBoolean("queue.orderFrontOnStart") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void bringQueueToFrontCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.orderFrontOnStart", enabled);
    }

    @Outlet
    private NSButton bringQueueToBackCheckbox;

    public void setBringQueueToBackCheckbox(NSButton b) {
        this.bringQueueToBackCheckbox = b;
        this.bringQueueToBackCheckbox.setTarget(this.id());
        this.bringQueueToBackCheckbox.setAction(Foundation.selector("bringQueueToBackCheckboxClicked:"));
        this.bringQueueToBackCheckbox.setState(Preferences.instance().getBoolean("queue.orderBackOnStop") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void bringQueueToBackCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.orderBackOnStop", enabled);
    }

    @Outlet
    private NSButton removeFromQueueCheckbox;

    public void setRemoveFromQueueCheckbox(NSButton b) {
        this.removeFromQueueCheckbox = b;
        this.removeFromQueueCheckbox.setTarget(this.id());
        this.removeFromQueueCheckbox.setAction(Foundation.selector("removeFromQueueCheckboxClicked:"));
        this.removeFromQueueCheckbox.setState(Preferences.instance().getBoolean("queue.removeItemWhenComplete") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void removeFromQueueCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.removeItemWhenComplete", enabled);
    }

    @Outlet
    private NSButton openAfterDownloadCheckbox;

    public void setOpenAfterDownloadCheckbox(NSButton b) {
        this.openAfterDownloadCheckbox = b;
        this.openAfterDownloadCheckbox.setTarget(this.id());
        this.openAfterDownloadCheckbox.setAction(Foundation.selector("openAfterDownloadCheckboxClicked:"));
        this.openAfterDownloadCheckbox.setState(Preferences.instance().getBoolean("queue.postProcessItemWhenComplete") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void openAfterDownloadCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.postProcessItemWhenComplete", enabled);
    }

    private void duplicateComboboxClicked(String selected, String property) {
        if(selected.equals(TransferAction.ACTION_CALLBACK.getLocalizableString())) {
            Preferences.instance().setProperty(property, TransferAction.ACTION_CALLBACK.toString());
        }
        else if(selected.equals(TransferAction.ACTION_OVERWRITE.getLocalizableString())) {
            Preferences.instance().setProperty(property, TransferAction.ACTION_OVERWRITE.toString());
        }
        else if(selected.equals(TransferAction.ACTION_RESUME.getLocalizableString())) {
            Preferences.instance().setProperty(property, TransferAction.ACTION_RESUME.toString());
        }
        else if(selected.equals(TransferAction.ACTION_RENAME.getLocalizableString())) {
            Preferences.instance().setProperty(property, TransferAction.ACTION_RENAME.toString());
        }
        else if(selected.equals(TransferAction.ACTION_RENAME_EXISTING.getLocalizableString())) {
            Preferences.instance().setProperty(property, TransferAction.ACTION_RENAME_EXISTING.toString());
        }
        else if(selected.equals(TransferAction.ACTION_SKIP.getLocalizableString())) {
            Preferences.instance().setProperty(property, TransferAction.ACTION_SKIP.toString());
        }
    }

    @Outlet
    private NSPopUpButton duplicateDownloadCombobox;

    public void setDuplicateDownloadCombobox(NSPopUpButton b) {
        this.duplicateDownloadCombobox = b;
        this.duplicateDownloadCombobox.setTarget(this.id());
        this.duplicateDownloadCombobox.setAction(Foundation.selector("duplicateDownloadComboboxClicked:"));
        this.duplicateDownloadCombobox.removeAllItems();
        this.duplicateDownloadCombobox.addItemsWithTitles(NSArray.arrayWithObjects(
                TransferAction.ACTION_CALLBACK.getLocalizableString(), TransferAction.ACTION_OVERWRITE.getLocalizableString(),
                TransferAction.ACTION_RESUME.getLocalizableString(), TransferAction.ACTION_RENAME.getLocalizableString(),
                TransferAction.ACTION_RENAME_EXISTING.getLocalizableString(), TransferAction.ACTION_SKIP.getLocalizableString())
        );
        if(Preferences.instance().getProperty("queue.download.fileExists").equals(TransferAction.ACTION_CALLBACK.toString())) {
            this.duplicateDownloadCombobox.selectItemWithTitle(TransferAction.ACTION_CALLBACK.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.download.fileExists").equals(TransferAction.ACTION_OVERWRITE.toString())) {
            this.duplicateDownloadCombobox.selectItemWithTitle(TransferAction.ACTION_OVERWRITE.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.download.fileExists").equals(TransferAction.ACTION_RESUME.toString())) {
            this.duplicateDownloadCombobox.selectItemWithTitle(TransferAction.ACTION_RESUME.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.download.fileExists").equals(TransferAction.ACTION_RENAME.toString())) {
            this.duplicateDownloadCombobox.selectItemWithTitle(TransferAction.ACTION_RENAME.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.download.fileExists").equals(TransferAction.ACTION_RENAME_EXISTING.toString())) {
            this.duplicateDownloadCombobox.selectItemWithTitle(TransferAction.ACTION_RENAME_EXISTING.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.download.fileExists").equals(TransferAction.ACTION_SKIP.toString())) {
            this.duplicateDownloadCombobox.selectItemWithTitle(TransferAction.ACTION_SKIP.getLocalizableString());
        }
    }

    @Action
    public void duplicateDownloadComboboxClicked(NSPopUpButton sender) {
        this.duplicateComboboxClicked(sender.selectedItem().title(), "queue.download.fileExists");
        this.duplicateDownloadOverwriteButtonClicked(duplicateDownloadOverwriteButton);
    }

    @Outlet
    private NSButton duplicateDownloadOverwriteButton;

    public void setDuplicateDownloadOverwriteButton(NSButton b) {
        this.duplicateDownloadOverwriteButton = b;
        this.duplicateDownloadOverwriteButton.setTarget(this.id());
        this.duplicateDownloadOverwriteButton.setAction(Foundation.selector("duplicateDownloadOverwriteButtonClicked:"));
        this.duplicateDownloadOverwriteButton.setState(
                Preferences.instance().getProperty("queue.download.reload.fileExists").equals(
                        TransferAction.ACTION_OVERWRITE.toString()) ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void duplicateDownloadOverwriteButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        if(enabled) {
            Preferences.instance().setProperty("queue.download.reload.fileExists", TransferAction.ACTION_OVERWRITE.toString());
        }
        else {
            Preferences.instance().setProperty("queue.download.reload.fileExists",
                    Preferences.instance().getProperty("queue.download.fileExists"));
        }
    }

    @Outlet
    private NSPopUpButton duplicateUploadCombobox;

    public void setDuplicateUploadCombobox(NSPopUpButton b) {
        this.duplicateUploadCombobox = b;
        this.duplicateUploadCombobox.setTarget(this.id());
        this.duplicateUploadCombobox.setAction(Foundation.selector("duplicateUploadComboboxClicked:"));
        this.duplicateUploadCombobox.removeAllItems();
        this.duplicateUploadCombobox.addItemsWithTitles(NSArray.arrayWithObjects(
                TransferAction.ACTION_CALLBACK.getLocalizableString(), TransferAction.ACTION_OVERWRITE.getLocalizableString(),
                TransferAction.ACTION_RESUME.getLocalizableString(), TransferAction.ACTION_RENAME.getLocalizableString(),
                TransferAction.ACTION_RENAME_EXISTING.getLocalizableString(), TransferAction.ACTION_SKIP.getLocalizableString())
        );
        if(Preferences.instance().getProperty("queue.upload.fileExists").equals(TransferAction.ACTION_CALLBACK.toString())) {
            this.duplicateUploadCombobox.selectItemWithTitle(TransferAction.ACTION_CALLBACK.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.upload.fileExists").equals(TransferAction.ACTION_OVERWRITE.toString())) {
            this.duplicateUploadCombobox.selectItemWithTitle(TransferAction.ACTION_OVERWRITE.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.upload.fileExists").equals(TransferAction.ACTION_RESUME.toString())) {
            this.duplicateUploadCombobox.selectItemWithTitle(TransferAction.ACTION_RESUME.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.upload.fileExists").equals(TransferAction.ACTION_RENAME.toString())) {
            this.duplicateUploadCombobox.selectItemWithTitle(TransferAction.ACTION_RENAME.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.upload.fileExists").equals(TransferAction.ACTION_RENAME_EXISTING.toString())) {
            this.duplicateUploadCombobox.selectItemWithTitle(TransferAction.ACTION_RENAME_EXISTING.getLocalizableString());
        }
        else if(Preferences.instance().getProperty("queue.upload.fileExists").equals(TransferAction.ACTION_SKIP.toString())) {
            this.duplicateUploadCombobox.selectItemWithTitle(TransferAction.ACTION_SKIP.getLocalizableString());
        }
    }

    @Action
    public void duplicateUploadComboboxClicked(NSPopUpButton sender) {
        this.duplicateComboboxClicked(sender.selectedItem().title(), "queue.upload.fileExists");
        this.duplicateUploadOverwriteButtonClicked(duplicateUploadOverwriteButton);
    }

    @Outlet
    private NSButton duplicateUploadOverwriteButton;

    public void setDuplicateUploadOverwriteButton(NSButton b) {
        this.duplicateUploadOverwriteButton = b;
        this.duplicateUploadOverwriteButton.setTarget(this.id());
        this.duplicateUploadOverwriteButton.setAction(Foundation.selector("duplicateUploadOverwriteButtonClicked:"));
        this.duplicateUploadOverwriteButton.setState(
                Preferences.instance().getProperty("queue.upload.reload.fileExists").equals(
                        TransferAction.ACTION_OVERWRITE.toString()) ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void duplicateUploadOverwriteButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        if(enabled) {
            Preferences.instance().setProperty("queue.upload.reload.fileExists", TransferAction.ACTION_OVERWRITE.toString());
        }
        else {
            Preferences.instance().setProperty("queue.upload.reload.fileExists",
                    Preferences.instance().getProperty("queue.upload.fileExists"));
        }
    }

    @Outlet
    private NSButton uploadTemporaryFilenameButton;

    public void setUploadTemporaryFilenameButton(NSButton b) {
        this.uploadTemporaryFilenameButton = b;
        this.uploadTemporaryFilenameButton.setTarget(this.id());
        this.uploadTemporaryFilenameButton.setAction(Foundation.selector("uploadTemporaryFilenameButtonClicked:"));
        this.uploadTemporaryFilenameButton.setState(
                Preferences.instance().getBoolean("queue.upload.file.temporary") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void uploadTemporaryFilenameButtonClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("queue.upload.file.temporary", enabled);
    }

    @Outlet
    private NSPopUpButton protocolCombobox;

    public void setProtocolCombobox(NSPopUpButton b) {
        this.protocolCombobox = b;
        this.protocolCombobox.setTarget(this.id());
        this.protocolCombobox.setAction(Foundation.selector("protocolComboboxClicked:"));
        this.protocolCombobox.removeAllItems();
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            this.protocolCombobox.addItemWithTitle(protocol.getDescription());
        }
        for(Protocol protocol : Protocol.getKnownProtocols()) {
            final NSMenuItem item = this.protocolCombobox.itemWithTitle(protocol.getDescription());
            item.setRepresentedObject(protocol.getIdentifier());
            item.setImage(IconCache.iconNamed(protocol.icon(), 16));
        }

        final Protocol defaultProtocol
                = Protocol.forName(Preferences.instance().getProperty("connection.protocol.default"));
        this.protocolCombobox.selectItemWithTitle(defaultProtocol.getDescription());
    }

    @Action
    public void protocolComboboxClicked(NSPopUpButton sender) {
        final Protocol selected = Protocol.forName(sender.selectedItem().representedObject());
        Preferences.instance().setProperty("connection.protocol.default", selected.getIdentifier());
        Preferences.instance().setProperty("connection.port.default", selected.getDefaultPort());
    }

    @Outlet
    private NSButton confirmDisconnectCheckbox;

    public void setConfirmDisconnectCheckbox(NSButton b) {
        this.confirmDisconnectCheckbox = b;
        this.confirmDisconnectCheckbox.setTarget(this.id());
        this.confirmDisconnectCheckbox.setAction(Foundation.selector("confirmDisconnectCheckboxClicked:"));
        this.confirmDisconnectCheckbox.setState(Preferences.instance().getBoolean("browser.confirmDisconnect") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void confirmDisconnectCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("browser.confirmDisconnect", enabled);
    }

    @Outlet
    private NSPopUpButton sshTransfersCombobox;

    /**
     * SSH Transfers (SFTP or SCP)
     *
     * @param b
     */
    public void setSshTransfersCombobox(NSPopUpButton b) {
        this.sshTransfersCombobox = b;
        this.sshTransfersCombobox.setTarget(this.id());
        this.sshTransfersCombobox.setAction(Foundation.selector("sshTransfersComboboxClicked:"));
        this.sshTransfersCombobox.removeAllItems();
        this.sshTransfersCombobox.addItemsWithTitles(NSArray.arrayWithObjects(Protocol.SFTP.getDescription(), Protocol.SCP.getDescription()));
        this.sshTransfersCombobox.itemWithTitle(Protocol.SFTP.getDescription()).setRepresentedObject(Protocol.SFTP.getIdentifier());
        this.sshTransfersCombobox.itemWithTitle(Protocol.SCP.getDescription()).setRepresentedObject(Protocol.SCP.getIdentifier());
        if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SFTP.toString())) {
            this.sshTransfersCombobox.selectItemWithTitle(Protocol.SFTP.getDescription());
        }
        else if(Preferences.instance().getProperty("ssh.transfer").equals(Protocol.SCP.toString())) {
            this.sshTransfersCombobox.selectItemWithTitle(Protocol.SCP.getDescription());
        }
    }

    @Action
    public void sshTransfersComboboxClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("ssh.transfer", sender.selectedItem().representedObject());
    }

    private void configureDefaultProtocolHandlerCombobox(NSPopUpButton defaultProtocolHandlerCombobox, Protocol protocol) {
        final String defaultHandler = URLSchemeHandlerConfiguration.instance().getDefaultHandlerForURLScheme(protocol.getScheme());
        if(null == defaultHandler) {
            defaultProtocolHandlerCombobox.addItemWithTitle(Locale.localizedString("Unknown"));
            defaultProtocolHandlerCombobox.setEnabled(false);
            return;
        }
        log.debug("Default Protocol Handler for " + protocol + ":" + defaultHandler);
        final String[] bundleIdentifiers = URLSchemeHandlerConfiguration.instance().getAllHandlersForURLScheme(protocol.getScheme());
        for(String bundleIdentifier : bundleIdentifiers) {
            String app = EditorFactory.getApplicationName(bundleIdentifier);
            if(StringUtils.isEmpty(app)) {
                continue;
            }
            defaultProtocolHandlerCombobox.addItemWithTitle(app);
            final NSMenuItem item = defaultProtocolHandlerCombobox.lastItem();
            item.setImage(IconCache.instance().iconForApplication(bundleIdentifier, 16));
            item.setRepresentedObject(bundleIdentifier);
            if(bundleIdentifier.equals(defaultHandler)) {
                defaultProtocolHandlerCombobox.selectItem(item);
            }
        }
    }

    @Outlet
    private NSPopUpButton defaultFTPHandlerCombobox;

    /**
     * Protocol Handler FTP
     *
     * @param b
     */
    public void setDefaultFTPHandlerCombobox(NSPopUpButton b) {
        this.defaultFTPHandlerCombobox = b;
        this.defaultFTPHandlerCombobox.setTarget(this.id());
        this.defaultFTPHandlerCombobox.setAction(Foundation.selector("defaultFTPHandlerComboboxClicked:"));
        this.defaultFTPHandlerCombobox.removeAllItems();
        this.configureDefaultProtocolHandlerCombobox(this.defaultFTPHandlerCombobox, Protocol.FTP);
    }

    @Action
    public void defaultFTPHandlerComboboxClicked(NSPopUpButton sender) {
        String bundle = sender.selectedItem().representedObject();
        URLSchemeHandlerConfiguration.instance().setDefaultHandlerForURLScheme(
                new String[]{Protocol.FTP.getScheme(), Protocol.FTP_TLS.getScheme()}, bundle);
    }

    @Outlet
    private NSPopUpButton defaultSFTPHandlerCombobox;

    /**
     * Protocol Handler SFTP
     *
     * @param b
     */
    public void setDefaultSFTPHandlerCombobox(NSPopUpButton b) {
        this.defaultSFTPHandlerCombobox = b;
        this.defaultSFTPHandlerCombobox.setTarget(this.id());
        this.defaultSFTPHandlerCombobox.setAction(Foundation.selector("defaultSFTPHandlerComboboxClicked:"));
        this.defaultSFTPHandlerCombobox.removeAllItems();
        this.configureDefaultProtocolHandlerCombobox(this.defaultSFTPHandlerCombobox, Protocol.SFTP);
    }

    @Action
    public void defaultSFTPHandlerComboboxClicked(NSPopUpButton sender) {
        String bundle = sender.selectedItem().representedObject();
        URLSchemeHandlerConfiguration.instance().setDefaultHandlerForURLScheme(
                Protocol.SFTP.getScheme(), bundle
        );
    }

    @Outlet
    private NSPopUpButton defaultDownloadThrottleCombobox;

    /**
     * Download Bandwidth
     *
     * @param b
     */
    public void setDefaultDownloadThrottleCombobox(NSPopUpButton b) {
        this.defaultDownloadThrottleCombobox = b;
        this.defaultDownloadThrottleCombobox.setTarget(this.id());
        this.defaultDownloadThrottleCombobox.setAction(Foundation.selector("defaultDownloadThrottleComboboxClicked:"));
        int bandwidth = Preferences.instance().getInteger("queue.download.bandwidth.bytes");
        final StringTokenizer options = new StringTokenizer(Preferences.instance().getProperty("queue.bandwidth.options"), ",");
        while(options.hasMoreTokens()) {
            final String bytes = options.nextToken();
            this.defaultDownloadThrottleCombobox.addItemWithTitle(Status.getSizeAsString(Integer.parseInt(bytes)) + "/s");
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
            Preferences.instance().setProperty("queue.download.bandwidth.bytes", -1);
        }
        else {
            Preferences.instance().setProperty("queue.download.bandwidth.bytes", Integer.parseInt(bytes));
        }
    }

    @Outlet
    private NSPopUpButton defaultUploadThrottleCombobox;

    /**
     * Upload Bandwidth
     *
     * @param b
     */
    public void setDefaultUploadThrottleCombobox(NSPopUpButton b) {
        this.defaultUploadThrottleCombobox = b;
        this.defaultUploadThrottleCombobox.setTarget(this.id());
        this.defaultUploadThrottleCombobox.setAction(Foundation.selector("defaultUploadThrottleComboboxClicked:"));
        int bandwidth = Preferences.instance().getInteger("queue.upload.bandwidth.bytes");
        final StringTokenizer options = new StringTokenizer(Preferences.instance().getProperty("queue.bandwidth.options"), ",");
        while(options.hasMoreTokens()) {
            final String bytes = options.nextToken();
            this.defaultUploadThrottleCombobox.addItemWithTitle(Status.getSizeAsString(Integer.parseInt(bytes)) + "/s");
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
            Preferences.instance().setProperty("queue.upload.bandwidth.bytes", -1);
        }
        else {
            Preferences.instance().setProperty("queue.upload.bandwidth.bytes", Integer.parseInt(bytes));
        }
    }

    @Outlet
    private NSButton updateCheckbox;

    public void setUpdateCheckbox(NSButton b) {
        this.updateCheckbox = b;
        this.updateCheckbox.setTarget(this.id());
        this.updateCheckbox.setAction(Foundation.selector("updateCheckboxClicked:"));
        this.updateCheckbox.setState(Preferences.instance().getBoolean("update.check") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void updateCheckboxClicked(final NSButton sender) {
        boolean enabled = sender.state() == NSCell.NSOnState;
        Preferences.instance().setProperty("update.check", enabled);
        // Update the Sparkle property. Default is in Info.plist
        if(enabled) {
            Preferences.instance().setProperty("SUScheduledCheckInterval", Preferences.instance().getProperty("update.check.interval"));
        }
        else {
            Preferences.instance().deleteProperty("SUScheduledCheckInterval");
        }
    }

    @Outlet
    private NSPopUpButton updateFeedPopup;

    public void setUpdateFeedPopup(NSPopUpButton b) {
        this.updateFeedPopup = b;
        this.updateFeedPopup.removeAllItems();
        this.updateFeedPopup.setAction(Foundation.selector("updateFeedPopupClicked:"));
        this.updateFeedPopup.addItemWithTitle(Locale.localizedString("Release"));
        this.updateFeedPopup.lastItem().setRepresentedObject(Preferences.instance().getProperty("update.feed.release"));
        this.updateFeedPopup.addItemWithTitle(Locale.localizedString("Beta"));
        this.updateFeedPopup.lastItem().setRepresentedObject(Preferences.instance().getProperty("update.feed.beta"));
        this.updateFeedPopup.addItemWithTitle(Locale.localizedString("Snapshot Builds"));
        this.updateFeedPopup.lastItem().setRepresentedObject(Preferences.instance().getProperty("update.feed.nightly"));
        String feed = Preferences.instance().getProperty("SUFeedURL");
        NSInteger selected = this.updateFeedPopup.menu().indexOfItemWithRepresentedObject(feed);
        if(-1 == selected.intValue()) {
            log.warn("Invalid feed setting:" + feed);
            this.updateFeedPopup.selectItemAtIndex(this.updateFeedPopup.menu().indexOfItemWithRepresentedObject(
                    Preferences.instance().getProperty("update.feed.release")));
        }
        else {
            this.updateFeedPopup.selectItemAtIndex(selected);
        }
    }

    @Action
    public void updateFeedPopupClicked(NSPopUpButton sender) {
        // Update sparkle feed property. Default is in Info.plist
        String selected = sender.selectedItem().representedObject();
        if(null == selected || Preferences.instance().getDefault("SUFeedURL").equals(selected)) {
            // Remove custom value
            Preferences.instance().deleteProperty("SUFeedURL");
        }
        else {
            Preferences.instance().setProperty("SUFeedURL", selected);
        }
    }

    @Outlet
    private NSPopUpButton defaultBucketLocation;

    public void setDefaultBucketLocation(NSPopUpButton b) {
        this.defaultBucketLocation = b;
        this.defaultBucketLocation.setAutoenablesItems(false);
        this.defaultBucketLocation.removeAllItems();
        for(String location : S3Session.getAvailableLocations()) {
            this.defaultBucketLocation.addItemWithTitle(Locale.localizedString(location, "S3"));
            this.defaultBucketLocation.lastItem().setRepresentedObject(location);
        }
        this.defaultBucketLocation.setTarget(this.id());
        this.defaultBucketLocation.setAction(Foundation.selector("defaultBucketLocationClicked:"));
        this.defaultBucketLocation.selectItemWithTitle(Locale.localizedString(Preferences.instance().getProperty("s3.location"), "S3"));
    }

    @Action
    public void defaultBucketLocationClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("s3.location", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton defaultStorageClassPopup;

    public void setDefaultStorageClassPopup(NSPopUpButton b) {
        this.defaultStorageClassPopup = b;
        this.defaultStorageClassPopup.setAutoenablesItems(false);
        this.defaultStorageClassPopup.removeAllItems();
        this.defaultStorageClassPopup.addItemWithTitle(Locale.localizedString(S3Object.STORAGE_CLASS_STANDARD, "S3"));
        this.defaultStorageClassPopup.lastItem().setRepresentedObject(S3Object.STORAGE_CLASS_STANDARD);
        this.defaultStorageClassPopup.addItemWithTitle(Locale.localizedString(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY, "S3"));
        this.defaultStorageClassPopup.lastItem().setRepresentedObject(S3Object.STORAGE_CLASS_REDUCED_REDUNDANCY);
        this.defaultStorageClassPopup.setTarget(this.id());
        this.defaultStorageClassPopup.setAction(Foundation.selector("setDefaultStorageClassPopupClicked:"));
        this.defaultStorageClassPopup.selectItemWithTitle(Locale.localizedString(Preferences.instance().getProperty("s3.storage.class"), "S3"));
    }

    @Action
    public void setDefaultStorageClassPopupClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("s3.storage.class", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton documentExportFormatPopup;

    public void setDocumentExportFormatPopup(NSPopUpButton b) {
        this.documentExportFormatPopup = b;
        this.documentExportFormatPopup.setAutoenablesItems(false);
        this.documentExportFormatPopup.removeAllItems();
        StringTokenizer formats = new StringTokenizer(
                Preferences.instance().getProperty("google.docs.export.document.formats"), ",");
        for(int i = 0; formats.hasMoreTokens(); i++) {
            String format = formats.nextToken();
            final String title = new StringBuilder(FinderLocal.kind(format)).append(" (.").append(format).append(")").toString();
            this.documentExportFormatPopup.addItemWithTitle(title);
            this.documentExportFormatPopup.lastItem().setRepresentedObject(format);
            if(format.equals(Preferences.instance().getProperty("google.docs.export.document"))) {
                this.documentExportFormatPopup.selectItemWithTitle(title);
            }
        }
        this.documentExportFormatPopup.setTarget(this.id());
        this.documentExportFormatPopup.setAction(Foundation.selector("documentExportFormatPopupClicked:"));
    }

    @Action
    public void documentExportFormatPopupClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("google.docs.export.document", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton spreadsheetExportFormatPopup;

    public void setSpreadsheetExportFormatPopup(NSPopUpButton b) {
        this.spreadsheetExportFormatPopup = b;
        this.spreadsheetExportFormatPopup.setAutoenablesItems(false);
        this.spreadsheetExportFormatPopup.removeAllItems();
        StringTokenizer formats = new StringTokenizer(
                Preferences.instance().getProperty("google.docs.export.spreadsheet.formats"), ",");
        for(int i = 0; formats.hasMoreTokens(); i++) {
            String format = formats.nextToken();
            final String title = new StringBuilder(FinderLocal.kind(format)).append(" (.").append(format).append(")").toString();
            this.spreadsheetExportFormatPopup.addItemWithTitle(title);
            this.spreadsheetExportFormatPopup.lastItem().setRepresentedObject(format);
            if(format.equals(Preferences.instance().getProperty("google.docs.export.spreadsheet"))) {
                this.spreadsheetExportFormatPopup.selectItemWithTitle(title);
            }
        }
        this.spreadsheetExportFormatPopup.setTarget(this.id());
        this.spreadsheetExportFormatPopup.setAction(Foundation.selector("spreadsheetExportFormatPopupClicked:"));
    }

    @Action
    public void spreadsheetExportFormatPopupClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("google.docs.export.spreadsheet", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSPopUpButton presentationExportFormatPopup;

    public void setPresentationExportFormatPopup(NSPopUpButton b) {
        this.presentationExportFormatPopup = b;
        this.presentationExportFormatPopup.setAutoenablesItems(false);
        this.presentationExportFormatPopup.removeAllItems();
        StringTokenizer formats = new StringTokenizer(
                Preferences.instance().getProperty("google.docs.export.presentation.formats"), ",");
        for(int i = 0; formats.hasMoreTokens(); i++) {
            String format = formats.nextToken();
            final String title = new StringBuilder(FinderLocal.kind(format)).append(" (.").append(format).append(")").toString();
            this.presentationExportFormatPopup.addItemWithTitle(title);
            this.presentationExportFormatPopup.lastItem().setRepresentedObject(format);
            if(format.equals(Preferences.instance().getProperty("google.docs.export.presentation"))) {
                this.presentationExportFormatPopup.selectItemWithTitle(title);
            }
        }
        this.presentationExportFormatPopup.setTarget(this.id());
        this.presentationExportFormatPopup.setAction(Foundation.selector("presentationExportFormatPopupClicked:"));
    }

    @Action
    public void presentationExportFormatPopupClicked(NSPopUpButton sender) {
        Preferences.instance().setProperty("google.docs.export.presentation", sender.selectedItem().representedObject());
    }

    @Outlet
    private NSButton convertUploadsCheckbox;

    public void setConvertUploadsCheckbox(NSButton b) {
        this.convertUploadsCheckbox = b;
        this.convertUploadsCheckbox.setTarget(this.id());
        this.convertUploadsCheckbox.setAction(Foundation.selector("convertUploadsCheckboxClicked:"));
        this.convertUploadsCheckbox.setState(Preferences.instance().getBoolean("google.docs.upload.convert") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void convertUploadsCheckboxClicked(NSButton sender) {
        Preferences.instance().setProperty("google.docs.upload.convert", sender.state() == NSCell.NSOnState);
    }

    @Outlet
    private NSButton ocrUploadsCheckbox;

    public void setOcrUploadsCheckbox(NSButton b) {
        this.ocrUploadsCheckbox = b;
        this.ocrUploadsCheckbox.setTarget(this.id());
        this.ocrUploadsCheckbox.setAction(Foundation.selector("ocrUploadsCheckboxClicked:"));
        this.ocrUploadsCheckbox.setState(Preferences.instance().getBoolean("google.docs.upload.ocr") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void ocrUploadsCheckboxClicked(NSButton sender) {
        Preferences.instance().setProperty("google.docs.upload.ocr", sender.state() == NSCell.NSOnState);
    }

    @Outlet
    private NSPopUpButton languagePopup;

    public void setLanguagePopup(NSPopUpButton b) {
        this.languagePopup = b;
        this.languagePopup.removeAllItems();
        this.languagePopup.setTarget(this.id());
        this.languagePopup.setAction(Foundation.selector("languagePopupClicked:"));
        this.languagePopup.addItemWithTitle(Locale.localizedString("Default"));
        this.languagePopup.menu().addItem(NSMenuItem.separatorItem());
        String custom = null;
        if(Preferences.instance().systemLocales().size() > 1) {
            // No user default application scope single value of AppleLanguages property is set but a list
            // of preferred languages from system preferences is returned.
            this.languagePopup.selectItemWithTitle(Locale.localizedString("Default"));
        }
        else {
            // Custom language set for this application identifier
            custom = Preferences.instance().locale();
        }
        for(String identifier : Preferences.instance().applicationLocales()) {
            this.languagePopup.addItemWithTitle(Preferences.instance().getDisplayName(identifier));
            this.languagePopup.lastItem().setRepresentedObject(identifier);
            if(identifier.equals(custom)) {
                this.languagePopup.selectItem(this.languagePopup.lastItem());
            }
        }
    }

    @Action
    public void languagePopupClicked(NSPopUpButton sender) {
        if(null == sender.selectedItem().representedObject()) {
            // Revert to system default language
            Preferences.instance().deleteProperty("AppleLanguages");
        }
        else {
            Preferences.instance().setProperty("AppleLanguages",
                    Collections.singletonList(sender.selectedItem().representedObject()));
        }
    }

    @Outlet
    private NSPopUpButton useProxiesButton;

    public void setUseProxiesButton(NSPopUpButton b) {
        this.useProxiesButton = b;
        this.useProxiesButton.setTarget(this.id());
        this.useProxiesButton.setAction(Foundation.selector("useProxiesButtonClicked:"));
        this.useProxiesButton.setState(Preferences.instance().getBoolean("connection.proxy.enable") ? NSCell.NSOnState : NSCell.NSOffState);
    }

    @Action
    public void useProxiesButtonClicked(NSButton sender) {
        Preferences.instance().setProperty("connection.proxy.enable", sender.state() == NSCell.NSOnState);
    }

    @Outlet
    private NSPopUpButton configureProxiesButton;

    public void setConfigureProxiesButton(NSPopUpButton b) {
        this.configureProxiesButton = b;
        this.configureProxiesButton.setTarget(this.id());
        this.configureProxiesButton.setAction(Foundation.selector("configureProxiesButtonClicked:"));
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
}
