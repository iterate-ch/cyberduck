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
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.EnumSet;
import java.util.Set;

public class FolderController extends FileController {

    private final Path workdir;
    private final Path selected;
    private final Set<Location.Name> regions;
    private final Callback callback;

    @Outlet
    private NSPopUpButton regionPopup;

    public FolderController(final Path workdir, final Path selected, final Cache<Path> cache, final Set<Location.Name> regions, final Callback callback) {
        super(workdir, selected, cache);
        this.workdir = workdir;
        this.selected = selected;
        this.regions = regions;
        this.callback = callback;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Create new folder", "Folder"));
        alert.setInformativeText(LocaleFactory.localizedString("Enter the name for the new folder", "Folder"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create", "Folder"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Folder"));
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("newfolder.tiff", 64));
        super.loadBundle(alert);
    }

    public NSView getAccessoryView(final NSAlert alert) {
        if(this.hasLocation()) {
            final NSView view = NSView.create(new NSRect(alert.window().frame().size.width.doubleValue(), 0));
            this.regionPopup = NSPopUpButton.buttonWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 26));
            for(Location.Name region : regions) {
                regionPopup.addItemWithTitle(region.toString());
                regionPopup.itemWithTitle(region.toString()).setRepresentedObject(region.getIdentifier());
                if(region.getIdentifier().equals(PreferencesFactory.get().getProperty("s3.location"))) {
                    regionPopup.selectItem(regionPopup.lastItem());
                }
            }
            // Override accessory view with location menu added
            regionPopup.setFrameOrigin(new NSPoint(0, 0));
            view.addSubview(regionPopup);
            inputField.setFrameOrigin(new NSPoint(0, this.getFrame(alert, view).size.height.doubleValue() + view.subviews().count().doubleValue() * SUBVIEWS_VERTICAL_SPACE));
            view.addSubview(inputField);
            return view;
        }
        return super.getAccessoryView(alert);
    }

    protected boolean hasLocation() {
        return !regions.isEmpty() && new UploadTargetFinder(workdir).find(selected).isRoot();
    }

    protected String getLocation() {
        return this.hasLocation() ? regionPopup.selectedItem().representedObject() : null;
    }

    @Override
    public void callback(final int returncode, final Path file) {
        file.setType(EnumSet.of(Path.Type.directory));
        callback.callback(file, this.getLocation());
    }

    public interface Callback {
        void callback(final Path folder, final String region);
    }
}
