package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.aquaticprime.License;
import ch.cyberduck.core.aquaticprime.LicenseFactory;
import ch.cyberduck.core.preferences.Preferences;

public final class TerminalVersionPrinter {

    private TerminalVersionPrinter() {
        //
    }

    public static void print(final Preferences preferences) {
        final Console console = new Console();
        final License l = LicenseFactory.find();
        console.printf("%s %s (%s). %s%n",
                preferences.getProperty("application.name"),
                preferences.getProperty("application.version"),
                preferences.getProperty("application.revision"),
                l.verify() ? l.toString() : "Not registered. Purchase a donation key to support the development of this software.");
    }
}
