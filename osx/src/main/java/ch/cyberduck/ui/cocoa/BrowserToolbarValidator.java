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

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.Archive;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.TerminalServiceFactory;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.browser.UploadTargetFinder;
import ch.cyberduck.ui.cocoa.quicklook.QuickLook;
import ch.cyberduck.ui.cocoa.quicklook.QuickLookFactory;

import org.rococoa.Foundation;
import org.rococoa.Selector;

public class BrowserToolbarValidator implements ToolbarValidator {

    private final QuickLook quicklook = QuickLookFactory.get();

    private BrowserController controller;

    public BrowserToolbarValidator(final BrowserController controller) {
        this.controller = controller;
    }

    @Override
    public boolean validate(final NSToolbarItem item) {
        final String identifier = item.itemIdentifier();
        switch(identifier) {
            case BrowserToolbarFactory.TOOLBAR_DISCONNECT:
                if(controller.isActivityRunning()) {
                    item.setLabel(LocaleFactory.localizedString("Stop"));
                    item.setPaletteLabel(LocaleFactory.localizedString("Stop"));
                    item.setToolTip(LocaleFactory.localizedString("Cancel current operation in progress"));
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed("stop.pdf"));
                }
                else {
                    item.setLabel(LocaleFactory.localizedString(BrowserToolbarFactory.TOOLBAR_DISCONNECT));
                    item.setPaletteLabel(LocaleFactory.localizedString(BrowserToolbarFactory.TOOLBAR_DISCONNECT));
                    item.setToolTip(LocaleFactory.localizedString("Disconnect from server"));
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed("eject.pdf"));
                }
                break;
            case BrowserToolbarFactory.TOOLBAR_ARCHIVE: {
                final Path selected = controller.getSelectedPath();
                if(null != selected) {
                    if(Archive.isArchive(selected.getName())) {
                        item.setLabel(LocaleFactory.localizedString("Unarchive", "Archive"));
                        item.setPaletteLabel(LocaleFactory.localizedString("Unarchive"));
                        item.setAction(Foundation.selector("unarchiveButtonClicked:"));
                    }
                    else {
                        item.setLabel(LocaleFactory.localizedString("Archive", "Archive"));
                        item.setPaletteLabel(LocaleFactory.localizedString("Archive"));
                        item.setAction(Foundation.selector("archiveButtonClicked:"));
                    }
                }
                break;
            }
        }
        return this.validate(item.action());
    }

    /**
     * @param action the method selector
     * @return true if the item by that identifier should be enabled
     */
    @Override
    public boolean validate(final Selector action) {
        if(action.equals(Foundation.selector("cut:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("copy:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("paste:"))) {
            if(this.isBrowser() && controller.isMounted()) {
                if(controller.getPasteboard().isEmpty()) {
                    NSPasteboard pboard = NSPasteboard.generalPasteboard();
                    if(pboard.availableTypeFromArray(NSArray.arrayWithObject(NSPasteboard.FilenamesPboardType)) != null) {
                        Object o = pboard.propertyListForType(NSPasteboard.FilenamesPboardType);
                        if(o != null) {
                            return true;
                        }
                    }
                    return false;
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("encodingMenuClicked:"))) {
            return this.isBrowser() && !controller.isActivityRunning();
        }
        else if(action.equals(Foundation.selector("connectBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return controller.getBookmarkTable().numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("addBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return controller.getBookmarkModel().getSource().allowsAdd();
            }
            return true;
        }
        else if(action.equals(Foundation.selector("deleteBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return controller.getBookmarkModel().getSource().allowsDelete()
                        && controller.getBookmarkTable().selectedRow().intValue() != -1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("duplicateBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return controller.getBookmarkModel().getSource().allowsEdit()
                        && controller.getBookmarkTable().numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("editBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return controller.getBookmarkModel().getSource().allowsEdit()
                        && controller.getBookmarkTable().numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("editButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0) {
                final EditorFactory factory = EditorFactory.instance();
                for(Path s : controller.getSelectedPaths()) {
                    if(!controller.isEditable(s)) {
                        return false;
                    }
                    // Choose editor for selected file
                    if(null == factory.getEditor(s.getName())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("editMenuClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0) {
                for(Path s : controller.getSelectedPaths()) {
                    if(!controller.isEditable(s)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("searchButtonClicked:"))) {
            return controller.isMounted() || this.isBookmarks();
        }
        else if(action.equals(Foundation.selector("quicklookButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && quicklook.isAvailable()) {
                if(controller.getSelectionCount() > 0) {
                    final Path selected = controller.getSelectedPath();
                    if(null == selected) {
                        return false;
                    }
                    return selected.isFile();
                }
            }
            return false;
        }
        else if(action.equals(Foundation.selector("openBrowserButtonClicked:"))) {
            return controller.isMounted();
        }
        else if(action.equals(Foundation.selector("sendCustomCommandClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Command.class) != null;
        }
        else if(action.equals(Foundation.selector("gotoButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("infoButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("createFolderButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("createFileButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Touch.class).isSupported(
                    new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath())
            );
        }
        else if(action.equals(Foundation.selector("uploadButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Touch.class).isSupported(
                    new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath())
            );
        }
        else if(action.equals(Foundation.selector("createSymlinkButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Symlink.class) != null
                    && controller.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("duplicateFileButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("renameFileButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() == 1) {
                final Path selected = controller.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return controller.getSession().getFeature(Move.class).isSupported(selected);
            }
            return false;
        }
        else if(action.equals(Foundation.selector("deleteFileButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("revertFileButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() == 1) {
                return controller.getSession().getFeature(Versioning.class) != null;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("reloadButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("newBrowserButtonClicked:"))) {
            return controller.isMounted();
        }
        else if(action.equals(Foundation.selector("syncButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("downloadAsButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("downloadToButtonClicked:")) || action.equals(Foundation.selector("downloadButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("insideButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(Foundation.selector("upButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && !controller.workdir().isRoot();
        }
        else if(action.equals(Foundation.selector("backButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getNavigation().getBack().size() > 1;
        }
        else if(action.equals(Foundation.selector("forwardButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getNavigation().getForward().size() > 0;
        }
        else if(action.equals(Foundation.selector("printDocument:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("disconnectButtonClicked:"))) {
            if(this.isBrowser()) {
                if(!controller.isConnected()) {
                    return controller.isActivityRunning();
                }
                return controller.isConnected();
            }
        }
        else if(action.equals(Foundation.selector("gotofolderButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("openTerminalButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted()
                    && controller.getSession().getHost().getProtocol().getType() == Protocol.Type.sftp
                    && TerminalServiceFactory.get() != null;
        }
        else if(action.equals(Foundation.selector("archiveButtonClicked:")) || action.equals(Foundation.selector("archiveMenuClicked:"))) {
            if(this.isBrowser() && controller.isMounted()) {
                if(controller.getSession().getFeature(Compress.class) == null) {
                    return false;
                }
                if(controller.getSelectionCount() > 0) {
                    for(Path s : controller.getSelectedPaths()) {
                        if(s.isFile() && Archive.isArchive(s.getName())) {
                            // At least one file selected is already an archive. No distinct action possible
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        else if(action.equals(Foundation.selector("unarchiveButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted()) {
                if(controller.getSession().getFeature(Compress.class) == null) {
                    return false;
                }
                if(controller.getSelectionCount() > 0) {
                    for(Path s : controller.getSelectedPaths()) {
                        if(s.isDirectory()) {
                            return false;
                        }
                        if(!Archive.isArchive(s.getName())) {
                            return false;
                        }
                    }
                    return true;
                }
            }
            return false;
        }
        return true; // by default everything is enabled
    }

    /**
     * @return Browser tab active
     */
    protected boolean isBrowser() {
        return controller.getSelectedTabView() == BrowserController.TAB_LIST_VIEW
                || controller.getSelectedTabView() == BrowserController.TAB_OUTLINE_VIEW;
    }

    /**
     * @return Bookmarks tab active
     */
    protected boolean isBookmarks() {
        return controller.getSelectedTabView() == BrowserController.TAB_BOOKMARKS;
    }
}
