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

import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSCell;
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallbackFactory;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.worker.CreateDirectoryWorker;
import ch.cyberduck.core.worker.CreateVaultWorker;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class FolderController extends FileController {

    private final Set<Location.Name> regions;

    private final BrowserController parent;

    @Outlet
    private NSPopUpButton regionPopup;
    @Outlet
    private NSView view;

    public FolderController(final BrowserController parent, final Cache<Path> cache, final Set<Location.Name> regions) {
        super(parent, cache, NSAlert.alert(
                LocaleFactory.localizedString("Create new folder", "Folder"),
                LocaleFactory.localizedString("Enter the name for the new folder:", "Folder"),
                LocaleFactory.localizedString("Create", "Folder"),
                null,
                LocaleFactory.localizedString("Cancel", "Folder")
        ));
        this.alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("newfolder.tiff", 64));
        this.alert.setShowsSuppressionButton(true);
        this.alert.suppressionButton().setTitle(LocaleFactory.localizedString("Create encrypted Vault"));
        this.parent = parent;
        this.regions = regions;
    }

    public void setRegionPopup(final NSPopUpButton regionPopup) {
        this.regionPopup = regionPopup;
    }

    public void setView(final NSView view) {
        this.view = view;
    }

    @Override
    protected String getBundleName() {
        return "Folder";
    }

    public NSView getAccessoryView() {
        if(this.hasLocation()) {
            // Override accessory view with location menu added
            this.loadBundle();
            for(Location.Name region : regions) {
                regionPopup.addItemWithTitle(region.toString());
                regionPopup.itemWithTitle(region.toString()).setRepresentedObject(region.getIdentifier());
                if(region.getIdentifier().equals(PreferencesFactory.get().getProperty("s3.location"))) {
                    regionPopup.selectItem(regionPopup.lastItem());
                }
            }
            return view;
        }
        return super.getAccessoryView();
    }

    private boolean hasLocation() {
        return !regions.isEmpty()
                && new UploadTargetFinder(this.getWorkdir()).find(this.getSelected()).isRoot();
    }

    @Override
    public void callback(int returncode) {
        if(returncode == DEFAULT_OPTION) {
            final String filename = inputField.stringValue();
            final Path folder = new Path(new UploadTargetFinder(this.getWorkdir()).find(this.getSelected()),
                    filename, EnumSet.of(Path.Type.directory));
            final String region = this.hasLocation() ? regionPopup.selectedItem().representedObject() : null;
            if(alert.suppressionButton().state() == NSCell.NSOnState) {
                parent.background(new WorkerBackgroundAction<Boolean>(parent, parent.getSession(),
                        new CreateVaultWorker(folder, PasswordStoreFactory.get(), LoginCallbackFactory.get(parent)) {
                            @Override
                            public void cleanup(final Boolean done) {
                                if(filename.charAt(0) == '.') {
                                    parent.setShowHiddenFiles(true);
                                }
                                parent.reload(parent.workdir(), Collections.singletonList(folder), Collections.singletonList(folder));
                            }
                        }));
            }
            else {
                parent.background(new WorkerBackgroundAction<Boolean>(parent, parent.getSession(),
                        new CreateDirectoryWorker(folder, region) {
                            @Override
                            public void cleanup(final Boolean done) {
                                if(filename.charAt(0) == '.') {
                                    parent.setShowHiddenFiles(true);
                                }
                                parent.reload(parent.workdir(), Collections.singletonList(folder), Collections.singletonList(folder));
                            }
                        }));
            }
        }
    }
}
