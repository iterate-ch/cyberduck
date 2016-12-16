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

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.Outlet;
import ch.cyberduck.binding.application.NSAlert;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.rococoa.cocoa.foundation.NSPoint;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.Set;

public class RegionController extends AlertController {

    @Outlet
    private final NSPopUpButton regionPopup;
    @Outlet
    private final NSView view;
    private final RegionController.Callback callback;

    public RegionController(final Set<Location.Name> regions, final RegionController.Callback callback) {
        super(NSAlert.alert(
                LocaleFactory.localizedString("Choose Region", "Folder"),
                null,
                LocaleFactory.localizedString("Choose"),
                null,
                LocaleFactory.localizedString("Cancel", "Folder")));
        this.callback = callback;
        this.view = NSView.create(new NSRect(window.frame().size.width.doubleValue(), 0));
        this.regionPopup = NSPopUpButton.buttonWithFrame(new NSRect(window.frame().size.width.doubleValue(), 26));
        for(Location.Name region : regions) {
            regionPopup.addItemWithTitle(region.toString());
            regionPopup.itemWithTitle(region.toString()).setRepresentedObject(region.getIdentifier());
            if(region.getIdentifier().equals(PreferencesFactory.get().getProperty("s3.location"))) {
                regionPopup.selectItem(regionPopup.lastItem());
            }
        }
    }

    public NSView getAccessoryView() {
        // Override accessory view with location menu added
        regionPopup.setFrameOrigin(new NSPoint(0, 0));
        view.addSubview(regionPopup);
        return view;
    }

    @Override
    public void callback(final int returncode) {
        if(returncode == DEFAULT_OPTION) {
            callback.callback(new Location.Name(regionPopup.selectedItem().representedObject()));
        }
    }


    public interface Callback {
        void callback(final Location.Name region);
    }
}
