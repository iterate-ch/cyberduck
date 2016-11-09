package ch.cyberduck.ui.cocoa;

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
import ch.cyberduck.binding.application.NSButton;
import ch.cyberduck.binding.application.NSControl;
import ch.cyberduck.binding.application.NSFont;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSText;
import ch.cyberduck.binding.application.NSTextField;
import ch.cyberduck.binding.application.NSTextFieldCell;
import ch.cyberduck.binding.application.NSTextView;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSData;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.BookmarkCollection;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.ftp.FTPConnectMode;
import ch.cyberduck.core.local.BrowserLauncherFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.AbstractBackgroundAction;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Selector;
import org.rococoa.cocoa.foundation.NSInteger;
import org.rococoa.cocoa.foundation.NSSize;

public class ExtendedBookmarkController extends BookmarkController {

    @Outlet
    private NSButton toggleOptionsButton;
    @Outlet
    private NSPopUpButton transferPopup;
    @Outlet
    private NSPopUpButton downloadPathPopup;
    @Outlet
    private NSOpenPanel downloadFolderOpenPanel;
    @Outlet
    private NSTextView commentField;
    @Outlet
    private NSPopUpButton connectmodePopup;
    @Outlet
    private NSImage favicon;
    @Outlet
    private NSTextField webURLField;
    @Outlet
    private NSButton webUrlImage;

    public ExtendedBookmarkController(BookmarkCollection collection, Host bookmark) {
        super(collection, bookmark);
    }

    public ExtendedBookmarkController(Host host) {
        super(host);
    }

    public void setToggleOptionsButton(final NSButton toggleOptionsButton) {
        this.toggleOptionsButton = toggleOptionsButton;
    }

    public void setCommentField(final NSTextView field) {
        this.commentField = field;
        this.commentField.setFont(NSFont.userFixedPitchFontOfSize(11f));
        this.notificationCenter.addObserver(this.id(),
                Foundation.selector("commentInputDidChange:"),
                NSText.TextDidChangeNotification,
                this.commentField);
    }

    @Action
    public void commentInputDidChange(final NSNotification sender) {
        bookmark.setComment(commentField.textStorage().string());
        this.itemChanged();
    }

    public void setConnectmodePopup(NSPopUpButton button) {
        this.connectmodePopup = button;
        this.connectmodePopup.setTarget(this.id());
        this.connectmodePopup.setAction(Foundation.selector("connectmodePopupClicked:"));
        this.connectmodePopup.removeAllItems();
        for(FTPConnectMode m : FTPConnectMode.values()) {
            this.connectmodePopup.addItemWithTitle(m.toString());
            this.connectmodePopup.lastItem().setRepresentedObject(m.name());
            if(m.equals(FTPConnectMode.unknown)) {
                this.connectmodePopup.menu().addItem(NSMenuItem.separatorItem());
            }
        }
    }

    @Action
    public void connectmodePopupClicked(final NSPopUpButton sender) {
        bookmark.setFTPConnectMode(FTPConnectMode.valueOf(sender.selectedItem().representedObject()));
        this.itemChanged();
    }

    public void setTransferPopup(final NSPopUpButton button) {
        this.transferPopup = button;
        this.transferPopup.setTarget(this.id());
        this.transferPopup.setAction(Foundation.selector("transferPopupClicked:"));
        this.transferPopup.removeAllItems();
        final Host.TransferType unknown = Host.TransferType.unknown;
        this.transferPopup.addItemWithTitle(unknown.toString());
        this.transferPopup.lastItem().setRepresentedObject(unknown.name());
        this.transferPopup.menu().addItem(NSMenuItem.separatorItem());
        for(String name : preferences.getList("queue.transfer.type.enabled")) {
            final Host.TransferType t = Host.TransferType.valueOf(name);
            this.transferPopup.addItemWithTitle(t.toString());
            this.transferPopup.lastItem().setRepresentedObject(t.name());
        }
    }

    @Action
    public void transferPopupClicked(final NSPopUpButton sender) {
        bookmark.setTransfer(Host.TransferType.valueOf(sender.selectedItem().representedObject()));
        this.itemChanged();
    }

    public void setDownloadPathPopup(final NSPopUpButton button) {
        this.downloadPathPopup = button;
        this.downloadPathPopup.setTarget(this.id());
        final Selector action = Foundation.selector("downloadPathPopupClicked:");
        this.downloadPathPopup.setAction(action);
        this.downloadPathPopup.removeAllItems();

        // Default download folder
        this.addDownloadPath(action, new DownloadDirectoryFinder().find(bookmark));
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.addDownloadPath(action, LocalFactory.get(preferences.getProperty("queue.download.folder")));
        // Shortcut to the Desktop
        this.addDownloadPath(action, LocalFactory.get("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(action, LocalFactory.get("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(action, LocalFactory.get("~/Downloads"));
        // Choose another folder

        // Choose another folder
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(CHOOSE, action, StringUtils.EMPTY);
        this.downloadPathPopup.lastItem().setTarget(this.id());
    }

    private void addDownloadPath(Selector action, Local f) {
        if(downloadPathPopup.menu().itemWithTitle(f.getDisplayName()) == null) {
            downloadPathPopup.menu().addItemWithTitle_action_keyEquivalent(f.getDisplayName(), action, StringUtils.EMPTY);
            downloadPathPopup.lastItem().setTarget(this.id());
            downloadPathPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().fileIcon(f, 16));
            downloadPathPopup.lastItem().setRepresentedObject(f.getAbsolute());
            if(new DownloadDirectoryFinder().find(bookmark).equals(f)) {
                downloadPathPopup.selectItem(downloadPathPopup.lastItem());
            }
        }
    }

    @Action
    public void downloadPathPopupClicked(final NSMenuItem sender) {
        if(sender.title().equals(CHOOSE)) {
            downloadFolderOpenPanel = NSOpenPanel.openPanel();
            downloadFolderOpenPanel.setCanChooseFiles(false);
            downloadFolderOpenPanel.setCanChooseDirectories(true);
            downloadFolderOpenPanel.setAllowsMultipleSelection(false);
            downloadFolderOpenPanel.setCanCreateDirectories(true);
            downloadFolderOpenPanel.beginSheetForDirectory(null, null, this.window, this.id(),
                    Foundation.selector("downloadPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            final Local folder = LocalFactory.get(sender.representedObject());
            bookmark.setDownloadFolder(folder);
            this.itemChanged();
        }
    }

    public void downloadPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject selected = sheet.filenames().lastObject();
                if(selected != null) {
                    bookmark.setDownloadFolder(LocalFactory.get(selected.toString()));
                }
                break;
        }
        final NSMenuItem item = downloadPathPopup.itemAtIndex(new NSInteger(0));
        final Local folder = new DownloadDirectoryFinder().find(bookmark);
        item.setTitle(folder.getDisplayName());
        item.setRepresentedObject(folder.getAbsolute());
        item.setImage(IconCacheFactory.<NSImage>get().fileIcon(folder, 16));
        downloadPathPopup.selectItem(item);
        downloadFolderOpenPanel = null;
        this.itemChanged();
    }

    public void setWebURLField(final NSTextField field) {
        this.webURLField = field;
        final NSTextFieldCell cell = this.webURLField.cell();
        cell.setPlaceholderString(bookmark.getDefaultWebURL());
        notificationCenter.addObserver(this.id(),
                Foundation.selector("webURLInputDidChange:"),
                NSControl.NSControlTextDidChangeNotification,
                this.webURLField);
    }

    @Action
    public void webURLInputDidChange(final NSNotification sender) {
        bookmark.setWebURL(webURLField.stringValue());
        this.updateFavicon();
        this.itemChanged();
    }

    public void setWebUrlImage(final NSButton button) {
        this.webUrlImage = button;
        this.webUrlImage.setTarget(this.id());
        this.webUrlImage.setAction(Foundation.selector("openWebUrl:"));
        this.webUrlImage.setImage(IconCacheFactory.<NSImage>get().iconNamed("site.tiff", 16));
    }

    @Action
    public void openWebUrl(final NSButton sender) {
        BrowserLauncherFactory.get().open(bookmark.getWebURL());
    }

    private void updateFavicon() {
        if(preferences.getBoolean("bookmark.favicon.download")) {
            this.background(new AbstractBackgroundAction<Void>() {
                @Override
                public Void run() throws BackgroundException {
                    final String f = bookmark.getProtocol().favicon();
                    if(StringUtils.isNotBlank(f)) {
                        favicon = IconCacheFactory.<NSImage>get().iconNamed(f, 16);
                    }
                    else {
                        String url = bookmark.getWebURL() + "/favicon.ico";
                        // Default favicon location
                        final NSData data = NSData.dataWithContentsOfURL(NSURL.URLWithString(url));
                        if(null == data) {
                            return null;
                        }
                        favicon = NSImage.imageWithData(data);
                    }
                    if(null != favicon) {
                        favicon.setSize(new NSSize(16, 16));
                    }
                    return null;
                }

                @Override
                public void cleanup() {
                    if(null != favicon) {
                        webUrlImage.setImage(favicon);
                    }
                }

                @Override
                public Object lock() {
                    return bookmark;
                }
            });
        }
    }

    @Override
    public void awakeFromNib() {
        super.awakeFromNib();
        this.setState(this.toggleOptionsButton, preferences.getBoolean("bookmark.toggle.options"));
        this.updateFavicon();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        preferences.setProperty("bookmark.toggle.options", this.toggleOptionsButton.state());
    }

    @Override
    protected void init() {
        super.init();
        transferPopup.selectItemAtIndex(transferPopup.indexOfItemWithRepresentedObject(bookmark.getTransfer().name()));
        connectmodePopup.setEnabled(bookmark.getProtocol().getType() == Protocol.Type.ftp);
        if(bookmark.getProtocol().getType() == Protocol.Type.ftp) {
            connectmodePopup.selectItemAtIndex(connectmodePopup.indexOfItemWithRepresentedObject(bookmark.getFTPConnectMode().name()));
        }
        this.updateField(commentField, bookmark.getComment());
        final String webURL = bookmark.getWebURL();
        webUrlImage.setToolTip(webURL);
        this.updateField(webURLField, bookmark.getDefaultWebURL().equals(webURL) ? null : webURL);
    }
}
