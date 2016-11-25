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

import ch.cyberduck.binding.application.NSApplication;

public final class PreferencesControllerFactory {

    private static PreferencesController shared = null;

    private PreferencesControllerFactory() {
        //
    }

    public static PreferencesController instance() {
        synchronized(NSApplication.sharedApplication()) {
            if(null == shared) {
                shared = new PreferencesController() {
                    @Override
                    public void invalidate() {
                        shared = null;
                    }
                };
                shared.loadBundle();
            }
            return shared;
        }
    }
}
