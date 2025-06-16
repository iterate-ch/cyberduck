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
import ch.cyberduck.binding.application.NSImage;
import ch.cyberduck.binding.application.NSMenuItem;
import ch.cyberduck.binding.application.NSPopUpButton;
import ch.cyberduck.binding.application.NSView;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.resources.IconCacheFactory;

import org.rococoa.cocoa.foundation.NSRect;

import java.util.Comparator;
import java.util.Set;

public class ShareeController extends AlertController {

    @Outlet
    private final NSPopUpButton shareePopup = NSPopUpButton.buttonWithFrame(new NSRect(0, 26));

    private final Host host;
    private final Share.Type type;
    private final Set<Share.Sharee> sharees;
    private final ShareeController.Callback callback;

    public ShareeController(final Host host, final Share.Type type, final Set<Share.Sharee> sharees, final Callback callback) {
        this.host = host;
        this.type = type;
        this.sharees = sharees;
        this.callback = callback;
    }

    @Override
    public NSAlert loadAlert() {
        final NSAlert alert = NSAlert.alert();
        alert.setAlertStyle(NSAlert.NSInformationalAlertStyle);
        alert.setIcon(IconCacheFactory.<NSImage>get().iconNamed(host.getProtocol().disk(), 64));
        switch(type) {
            case download:
                alert.setMessageText(LocaleFactory.localizedString("Create Download Share", "Share"));
                break;
        }
        alert.setInformativeText(LocaleFactory.localizedString("Send share to:", "Share"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Create", "Share"));
        alert.addButtonWithTitle(LocaleFactory.localizedString("Cancel", "Share"));
        return alert;
    }

    public NSView getAccessoryView(final NSAlert alert) {
        shareePopup.addItemWithTitle(Share.Sharee.world.getDescription());
        shareePopup.menu().addItem(NSMenuItem.separatorItem());
        sharees.stream().sorted(Comparator.comparing(Share.Sharee::getDescription)).forEach(sharee -> {
            if(!sharee.equals(Share.Sharee.world)) {
                shareePopup.addItemWithTitle(sharee.getDescription());
                final NSMenuItem item = shareePopup.itemWithTitle(sharee.getDescription());
                item.setRepresentedObject(sharee.getIdentifier());
            }
        });
        shareePopup.selectItemAtIndex(shareePopup.indexOfItemWithRepresentedObject(Share.Sharee.world.getIdentifier()));
        return shareePopup;
    }

    @Override
    public void callback(final int returncode) {
        switch(returncode) {
            case DEFAULT_OPTION:
                callback.callback(new Share.Sharee(shareePopup.selectedItem().representedObject(), null));
                break;
        }
    }

    public interface Callback {
        void callback(final Share.Sharee region);
    }
}
