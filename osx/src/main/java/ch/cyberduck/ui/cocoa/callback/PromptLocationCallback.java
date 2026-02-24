package ch.cyberduck.ui.cocoa.callback;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.binding.AlertController;
import ch.cyberduck.binding.ProxyController;
import ch.cyberduck.binding.application.SheetCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocationCallback;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Location;
import ch.cyberduck.ui.cocoa.controller.RegionController;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PromptLocationCallback implements LocationCallback {

    private final ProxyController controller;

    public PromptLocationCallback(final ProxyController controller) {
        this.controller = controller;
    }

    @Override
    public Location.Name select(final Host bookmark, final String title, final String message, final Set<Location.Name> regions, final Location.Name defaultRegion) throws ConnectionCanceledException {
        final AtomicReference<Location.Name> selected = new AtomicReference<>();
        final AlertController alert = new RegionController(title, message, regions, defaultRegion, selected::set);
        if(controller.alert(alert) == SheetCallback.CANCEL_OPTION) {
            throw new ConnectionCanceledException();
        }
        return selected.get();
    }
}
