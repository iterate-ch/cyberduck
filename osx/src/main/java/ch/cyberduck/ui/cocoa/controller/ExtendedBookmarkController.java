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
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSOpenPanel;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.binding.foundation.NSObject;
import ch.cyberduck.binding.foundation.NSURL;
import ch.cyberduck.core.CollectionListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.local.FilesystemBookmarkResolverFactory;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.browser.DownloadDirectoryFinder;

import org.rococoa.Foundation;
import org.rococoa.ID;
import org.rococoa.Rococoa;
import org.rococoa.cocoa.foundation.NSInteger;

import static ch.cyberduck.core.features.AclPermission.preferences;

public class ExtendedBookmarkController extends BookmarkContainerController implements CollectionListener<Host> {

    private final Host bookmark;

    @Outlet
    private NSPopUpButton transferPopup;
    @Outlet
    private NSPopUpButton downloadPathPopup;
    @Outlet
    private NSOpenPanel downloadFolderOpenPanel;

    public ExtendedBookmarkController(final Host bookmark) {
        super(bookmark, new DefaultBookmarkController(bookmark, new LoginOptions(bookmark.getProtocol())));
        this.bookmark = bookmark;
    }

    @Override
    protected String getBundleName() {
        return "Edit";
    }

    @Override
    public void collectionLoaded() {
        //
    }

    @Override
    public void collectionItemAdded(final Host item) {
        //
    }

    @Override
    public void collectionItemRemoved(final Host item) {
        if(item.equals(bookmark)) {
            this.close();
        }
    }

    @Override
    public void collectionItemChanged(final Host item) {
        //
    }

    public void setTransferPopup(final NSPopUpButton button) {
        this.transferPopup = button;
        this.transferPopup.setTarget(this.id());
        this.transferPopup.setAction(Foundation.selector("transferPopupClicked:"));
        this.transferPopup.removeAllItems();
        final Host.TransferType unknown = Host.TransferType.unknown;
        this.transferPopup.addItemWithTitle(unknown.toString());
        this.transferPopup.lastItem().setRepresentedObject(unknown.name());
        this.transferPopup.lastItem().setToolTip(Host.TransferType.valueOf(PreferencesFactory.get().getProperty("queue.transfer.type")).toString());
        this.transferPopup.menu().addItem(NSMenuItem.separatorItem());
        for(String name : HostPreferencesFactory.get(bookmark).getList("queue.transfer.type.enabled")) {
            final Host.TransferType t = Host.TransferType.valueOf(name);
            this.transferPopup.addItemWithTitle(t.toString());
            this.transferPopup.lastItem().setRepresentedObject(t.name());
        }
        transferPopup.selectItemAtIndex(transferPopup.indexOfItemWithRepresentedObject(bookmark.getTransferType().name()));
    }

    @Action
    public void transferPopupClicked(final NSPopUpButton sender) {
        bookmark.setTransfer(Host.TransferType.valueOf(sender.selectedItem().representedObject()));
        this.update();
    }

    public void setDownloadPathPopup(final NSPopUpButton button) {
        this.downloadPathPopup = button;
        this.downloadPathPopup.setTarget(this.id());
        this.downloadPathPopup.setAction(Foundation.selector("downloadPathPopupClicked:"));
        this.downloadPathPopup.removeAllItems();

        // Default download folder
        this.addDownloadPath(new DownloadDirectoryFinder().find(bookmark));
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.addDownloadPath(LocalFactory.get(preferences.getProperty("queue.download.folder")));
        // Shortcut to the Desktop
        this.addDownloadPath(LocalFactory.get("~/Desktop"));
        // Shortcut to user home
        this.addDownloadPath(LocalFactory.get("~"));
        // Shortcut to user downloads for 10.5
        this.addDownloadPath(LocalFactory.get("~/Downloads"));
        // Choose another folder

        // Choose another folder
        this.downloadPathPopup.menu().addItem(NSMenuItem.separatorItem());
        this.downloadPathPopup.addItemWithTitle(String.format("%s…", LocaleFactory.localizedString("Choose")));
    }

    private void addDownloadPath(final Local f) {
        if(downloadPathPopup.menu().itemWithTitle(f.getDisplayName()) == null) {
            downloadPathPopup.addItemWithTitle(f.getDisplayName());
            downloadPathPopup.lastItem().setImage(IconCacheFactory.<NSImage>get().fileIcon(f, 16));
            downloadPathPopup.lastItem().setRepresentedObject(f.getAbsolute());
            if(new DownloadDirectoryFinder().find(bookmark).equals(f)) {
                downloadPathPopup.selectItem(downloadPathPopup.lastItem());
            }
        }
    }

    @Action
    public void downloadPathPopupClicked(final NSPopUpButton sender) {
        if(null == sender.selectedItem().representedObject()) {
            downloadFolderOpenPanel = NSOpenPanel.openPanel();
            downloadFolderOpenPanel.setCanChooseFiles(false);
            downloadFolderOpenPanel.setCanChooseDirectories(true);
            downloadFolderOpenPanel.setAllowsMultipleSelection(false);
            downloadFolderOpenPanel.setCanCreateDirectories(true);
            downloadFolderOpenPanel.beginSheetForDirectory(null, null, this.window, this.id(),
                    Foundation.selector("downloadPathPanelDidEnd:returnCode:contextInfo:"), null);
        }
        else {
            final Local folder = LocalFactory.get(sender.selectedItem().representedObject());
            bookmark.setDownloadFolder(folder);
            this.update();
        }
    }

    public void downloadPathPanelDidEnd_returnCode_contextInfo(NSOpenPanel sheet, final int returncode, ID contextInfo) {
        switch(returncode) {
            case SheetCallback.DEFAULT_OPTION:
                final NSObject url = sheet.URLs().lastObject();
                if(url != null) {
                    final Local selected = LocalFactory.get(Rococoa.cast(url, NSURL.class).path());
                    bookmark.setDownloadFolder(selected.setBookmark(FilesystemBookmarkResolverFactory.get().create(selected)));
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
        this.update();
    }
}
