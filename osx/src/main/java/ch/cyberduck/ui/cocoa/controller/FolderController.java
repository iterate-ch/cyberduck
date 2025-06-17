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
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.resources.IconCacheFactory;
import ch.cyberduck.ui.browser.UploadTargetFinder;

import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

public class FolderController extends FileController {

    private final Path workdir;
    private final Path selected;
    private final Set<Location.Name> regions;
    private final Location.Name defaultRegion;
    private final Callback callback;

    @Outlet
    private final NSPopUpButton regionPopup = NSPopUpButton.buttonPullsDown(false);

    public FolderController(final Path workdir, final Path selected, final Cache<Path> cache, final Set<Location.Name> regions, final Location.Name defaultRegion, final Callback callback) {
        super(workdir, selected, cache);
        this.workdir = workdir;
        this.selected = selected;
        this.regions = regions;
        this.defaultRegion = defaultRegion;
        this.callback = callback;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Create new folder", "Folder"));
        final String message = LocaleFactory.localizedString("Enter the name for the new folder", "Folder");
        alert.setInformativeText(new StringAppender().append(message).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create", "Folder"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Folder"));
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed("folderplus.tiff", 64));
        return alert;
    }

    public NSView getAccessoryView(final NSAlert alert) {
        if(this.hasLocation()) {
            final NSView accessoryView = NSView.create();
            regions.stream().sorted(Comparator.comparing(Location.Name::toString)).forEach(region -> {
                regionPopup.addItemWithTitle(region.toString());
                final NSMenuItem item = regionPopup.itemWithTitle(region.toString());
                item.setRepresentedObject(region.getIdentifier());
                if(!StringUtils.equals(region.getIdentifier(), region.toString())) {
                    final NSMutableAttributedString description = NSMutableAttributedString.create(item.title());
                    description.appendAttributedString(NSMutableAttributedString.create(String.format("\n%s", region.getIdentifier()), MENU_HELP_FONT_ATTRIBUTES));
                    item.setAttributedTitle(description);
                }
                if(region.equals(defaultRegion)) {
                    regionPopup.selectItem(regionPopup.lastItem());
                }
            });
            this.addAccessorySubview(accessoryView, regionPopup);
            this.addAccessorySubview(accessoryView, super.getAccessoryView(alert));
            return accessoryView;
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
