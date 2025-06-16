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
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.binding.foundation.NSMutableAttributedString;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.features.Location;

import org.apache.commons.lang3.StringUtils;
import org.rococoa.cocoa.foundation.NSRect;

import java.util.Comparator;
import java.util.Set;

public class RegionController extends AlertController {

    @Outlet
    private NSPopUpButton regionPopup;

    private final Set<Location.Name> regions;
    private final Location.Name defaultRegion;
    private final RegionController.Callback callback;

    public RegionController(final Set<Location.Name> regions, final Location.Name defaultRegion, final Callback callback) {
        this.regions = regions;
        this.defaultRegion = defaultRegion;
        this.callback = callback;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setMessageText(LocaleFactory.localizedString("Choose Region", "Folder"));
        final String message = LocaleFactory.localizedString("Select the region for the new folder", "Folder");
        alert.setInformativeText(new StringAppender().append(message).toString());
        alert.addButtonWithTitle(LocaleFactory.localizedString("Choose"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Folder"));
        return alert;
    }

    public NSView getAccessoryView(final NSAlert alert) {
        regionPopup = NSPopUpButton.buttonWithFrame(new NSRect(alert.window().frame().size.width.doubleValue(), 26));
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
        return regionPopup;
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
