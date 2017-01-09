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
    private NSView view;
    @Outlet
    private NSPopUpButton regionPopup;

    private final Set<Location.Name> regions;
    private final RegionController.Callback callback;

    public RegionController(final Set<Location.Name> regions, final RegionController.Callback callback) {
        this.regions = regions;
        this.callback = callback;
    }

    @Override
    public void loadBundle() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Choose Region", "Folder"));
        alert.setInformativeText(LocaleFactory.localizedString("Enter the name for the new folder", "Folder"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Choose"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Folder"));
        super.loadBundle(alert);
    }

    public NSView getAccessoryView(final NSAlert alert) {
        view = NSView.create(new NSRect(alert.window().frame().size.width.doubleValue(), 0));
        regionPopup = NSPopUpButton.buttonWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 26));
        regionPopup.setFrameOrigin(new NSPoint(0, 0));
        for(Location.Name region : regions) {
            regionPopup.addItemWithTitle(region.toString());
            regionPopup.itemWithTitle(region.toString()).setRepresentedObject(region.getIdentifier());
            if(region.getIdentifier().equals(PreferencesFactory.get().getProperty("s3.location"))) {
                regionPopup.selectItem(regionPopup.lastItem());
            }
        }
        // Override accessory view with location menu added
        view.addSubview(regionPopup);
        return view;
    }

    @Override
    public void callback(final int returncode) {
        switch(returncode) {
            case DEFAULT_OPTION:
                callback.callback(new Location.Name(regionPopup.selectedItem().representedObject()));
                break;
        }
    }


    public interface Callback {
        void callback(final Location.Name region);
    }
}
