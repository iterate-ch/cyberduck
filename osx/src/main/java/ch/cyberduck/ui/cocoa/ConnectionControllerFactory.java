package ch.cyberduck.ui.cocoa;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.binding.application.NSApplication;

import java.util.HashMap;
import java.util.Map;

public final class ConnectionControllerFactory {

    private static final Map<WindowController, ConnectionController> open
            = new HashMap<WindowController, ConnectionController>();

    private ConnectionControllerFactory() {
        //
    }

    public static ConnectionController create(final WindowController parent) {
        synchronized(NSApplication.sharedApplication()) {
            if(!open.containsKey(parent)) {
                final ConnectionController c = new ConnectionController(parent) {
                    @Override
                    public void invalidate() {
                        open.remove(parent);
                        super.invalidate();
                    }
                };
                open.put(parent, c);
            }
            return open.get(parent);
        }
    }
}
