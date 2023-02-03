package ch.cyberduck.ui.cocoa.toolbar;

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

import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPasteboard;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSToolbarItem;
import ch.cyberduck.binding.foundation.NSArray;
import ch.cyberduck.core.Archive;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.TerminalServiceFactory;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.features.Command;
import ch.cyberduck.core.features.Compress;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.features.PromptUrlProvider;
import ch.cyberduck.core.features.Restore;
import ch.cyberduck.core.features.Symlink;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.ui.browser.UploadTargetFinder;
import ch.cyberduck.ui.cocoa.controller.BrowserController;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.Foundation;
import org.rococoa.Rococoa;
import org.rococoa.Selector;

import static ch.cyberduck.ui.cocoa.toolbar.BrowserToolbarFactory.BrowserToolbarItem.*;

public class BrowserToolbarValidator implements ToolbarValidator {

    private final BrowserController controller;

    public BrowserToolbarValidator(final BrowserController controller) {
        this.controller = controller;
    }

    @Override
    public boolean validate(final NSToolbarItem item) {
        final String identifier = item.itemIdentifier();
        switch(valueOf(identifier)) {
            case disconnect: {
                if(!controller.isIdle()) {
                    item.setLabel(stop.label());
                    item.setPaletteLabel(stop.label());
                    item.setToolTip(stop.tooltip());
                    item.setImage(stop.image());
                }
                else {
                    item.setLabel(disconnect.label());
                    item.setPaletteLabel(disconnect.label());
                    item.setToolTip(disconnect.tooltip());
                    item.setImage(disconnect.image());
                }
                break;
            }
            case archive: {
                final Path selected = controller.getSelectedPath();
                if(null != selected) {
                    if(Archive.isArchive(selected.getName())) {
                        item.setLabel(unarchive.label());
                        item.setPaletteLabel(unarchive.label());
                        item.setAction(unarchive.action());
                    }
                    else {
                        item.setLabel(archive.label());
                        item.setPaletteLabel(archive.label());
                        item.setAction(archive.action());
                    }
                }
                break;
            }
            case encoding: {
                final NSPopUpButton popup = Rococoa.cast(item.view(), NSPopUpButton.class);
                popup.selectItemAtIndex(popup.indexOfItemWithRepresentedObject(controller.isMounted() ?
                    controller.getSession().getHost().getEncoding() : PreferencesFactory.get().getProperty("browser.charset.encoding")));
            }
            case cryptomator: {
                final Path selected = new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath());
                final VaultRegistry registry = controller.getSession().getVault();
                if(registry.contains(selected)) {
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSLockUnlockedTemplate"));
                }
                else {
                    item.setImage(IconCacheFactory.<NSImage>get().iconNamed("NSLockLockedTemplate"));
                }
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
        else if(action.equals(encoding.action())) {
            return this.isBrowser();
        }
        else if(action.equals(Foundation.selector("connectBookmarkButtonClicked:"))) {
            if(this.isBookmarks()) {
                return controller.getBookmarkTable().numberOfSelectedRows().intValue() == 1;
            }
            return false;
        }
        else if(action.equals(addbookmark.action())) {
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
        else if(action.equals(edit.action())) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0) {
                for(Path s : controller.getSelectedPaths()) {
                    if(!controller.isEditable(s)) {
                        return false;
                    }
                    // Choose editor for selected file
                    if(null == EditorFactory.getEditor(s.getName())) {
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
        else if(action.equals(BrowserToolbarFactory.BrowserToolbarItem.quicklook.action())) {
            if(this.isBrowser() && controller.isMounted()) {
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
        else if(action.equals(webbrowser.action())) {
            return controller.isMounted();
        }
        else if(action.equals(Foundation.selector("sendCustomCommandClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Command.class) != null;
        }
        else if(action.equals(Foundation.selector("gotoButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(info.action())) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0;
        }
        else if(action.equals(newfolder.action())) {
            return this.isBrowser() && controller.isMounted() &&
                controller.getSession().getFeature(Directory.class).isSupported(
                    new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath()), StringUtils.EMPTY
                );
        }
        else if(action.equals(Foundation.selector("createEncryptedVaultButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getVault() != VaultRegistry.DISABLED &&
                null == controller.workdir().attributes().getVault() &&
                controller.getSession().getFeature(Directory.class).isSupported(
                    new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath()), StringUtils.EMPTY
                );
        }
        else if(action.equals(Foundation.selector("createFileButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Touch.class).isSupported(
                new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath()), StringUtils.EMPTY
            );
        }
        else if(action.equals(upload.action())) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Touch.class).isSupported(
                new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath()),
                StringUtils.EMPTY);
        }
        else if(action.equals(Foundation.selector("createSymlinkButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Symlink.class) != null
                && controller.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("duplicateFileButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSession().getFeature(Copy.class) != null
                && controller.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("renameFileButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() == 1) {
                final Path selected = controller.getSelectedPath();
                if(null == selected) {
                    return false;
                }
                return controller.getSession().getFeature(Move.class).isSupported(selected, selected);
            }
            return false;
        }
        else if(action.equals(delete.action())) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0) {
                for(Path selected : controller.getSelectedPaths()) {
                    if(!controller.getSession().getFeature(Delete.class).isSupported(selected)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(share.action())) {
            if(this.isBrowser() && controller.isMounted()) {
                final Path selected = null != controller.getSelectedPath() ? controller.getSelectedPath() : controller.workdir();
                return controller.getSession().getFeature(PromptUrlProvider.class) != null &&
                    controller.getSession().getFeature(PromptUrlProvider.class).isSupported(selected, PromptUrlProvider.Type.download);
            }
            return false;
        }
        else if(action.equals(requestfiles.action())) {
            if(this.isBrowser() && controller.isMounted()) {
                final Path selected = null != controller.getSelectedPath() ? controller.getSelectedPath() : controller.workdir();
                return controller.getSession().getFeature(PromptUrlProvider.class) != null &&
                        controller.getSession().getFeature(PromptUrlProvider.class).isSupported(selected, PromptUrlProvider.Type.upload);
            }
            return false;
        }
        else if(action.equals(Foundation.selector("revertFileButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0) {
                for(Path selected : controller.getSelectedPaths()) {
                    if(null == controller.getSession().getFeature(Versioning.class)) {
                        return false;
                    }
                    if(!controller.getSession().getFeature(Versioning.class).isRevertable(selected)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(Foundation.selector("restoreFileButtonClicked:"))) {
            if(this.isBrowser() && controller.isMounted() && controller.getSelectionCount() > 0) {
                for(Path selected : controller.getSelectedPaths()) {
                    if(null == controller.getSession().getFeature(Restore.class)) {
                        return false;
                    }
                    if(!controller.getSession().getFeature(Restore.class).isRestorable(selected)) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        else if(action.equals(reload.action())) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("newBrowserButtonClicked:"))) {
            return controller.isMounted();
        }
        else if(action.equals(synchronize.action())) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(Foundation.selector("downloadAsButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted() && controller.getSelectionCount() == 1;
        }
        else if(action.equals(Foundation.selector("downloadToButtonClicked:")) || action.equals(download.action())) {
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
        else if(action.equals(disconnect.action())) {
            return !controller.isIdle() || controller.isConnected();
        }
        else if(action.equals(Foundation.selector("gotofolderButtonClicked:"))) {
            return this.isBrowser() && controller.isMounted();
        }
        else if(action.equals(terminal.action())) {
            return this.isBrowser() && controller.isMounted()
                && controller.getSession().getHost().getProtocol().getType() == Protocol.Type.sftp
                && TerminalServiceFactory.get() != null;
        }
        else if(action.equals(archive.action()) || action.equals(Foundation.selector("archiveMenuClicked:"))) {
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
        else if(action.equals(unarchive.action())) {
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
        else if(action.equals(cryptomator.action())) {
            if(this.isBrowser() && controller.isMounted() && !PreferencesFactory.get().getBoolean("cryptomator.vault.autodetect")) {
                final Path selected = new UploadTargetFinder(controller.workdir()).find(controller.getSelectedPath());
                final VaultRegistry registry = controller.getSession().getVault();
                if(registry.contains(selected)) {
                    // Allow to lock vault
                    return true;
                }
                final AttributedList<Path> cache = controller.getCache().get(controller.workdir());
                return null != cache.find(new SimplePathPredicate(Path.Type.file,
                    String.format("%s%s%s", controller.workdir().getAbsolute(), Path.DELIMITER,
                            new HostPreferences(controller.getSession().getHost()).getProperty("cryptomator.vault.masterkey.filename"))));
            }
            return false;
        }
        return true; // by default everything is enabled
    }

    /**
     * @return Browser tab active
     */
    protected boolean isBrowser() {
        switch(controller.getSelectedTabView()) {
            case list:
            case outline:
                return true;
            default:
                return false;
        }
    }

    /**
     * @return Bookmarks tab active
     */
    protected boolean isBookmarks() {
        switch(controller.getSelectedTabView()) {
            case bookmarks:
                return true;
            default:
                return false;
        }
    }
}
