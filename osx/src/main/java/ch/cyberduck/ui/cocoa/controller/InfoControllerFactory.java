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

import ch.cyberduck.binding.WindowController;
import ch.cyberduck.binding.application.NSApplication;
import ch.cyberduck.binding.foundation.NSNotification;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InfoControllerFactory {

    private static final Map<WindowController, InfoController> open
            = new HashMap<>();

    private static final Preferences preferences = PreferencesFactory.get();

    private InfoControllerFactory() {
        //
    }

    public static InfoController create(final BrowserController parent, final List<Path> selected) {
        synchronized(NSApplication.sharedApplication()) {
            if(preferences.getBoolean("browser.info.inspector")) {
                if(open.containsKey(parent)) {
                    final InfoController c = open.get(parent);
                    c.setFiles(selected);
                    return c;
                }
            }
            final InfoController info = new InfoController(parent, parent.getSession(), selected, new ReloadCallback() {
                @Override
                public void done(final List<Path> files) {
                    parent.reload(parent.workdir(), selected, selected);
                }
            }) {
                @Override
                public void invalidate() {
                    open.remove(parent);
                    super.invalidate();
                }
            };
            open.put(parent, info);
            return info;
        }
    }

    /**
     * @param controller Browser
     * @return Null if the browser does not have an Info window.
     */
    public static InfoController get(final BrowserController controller) {
        return open.get(controller);
    }

    public static void remove(final BrowserController controller) {
        open.remove(controller);
    }
}
