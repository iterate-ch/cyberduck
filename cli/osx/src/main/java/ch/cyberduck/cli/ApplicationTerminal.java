/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

package ch.cyberduck.cli;

import ch.cyberduck.core.preferences.ApplicationTerminalPreferences;
import ch.cyberduck.core.preferences.Preferences;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;

import java.io.IOException;

public class ApplicationTerminal extends Terminal {
    public ApplicationTerminal(final Preferences defaults, final Options options, final CommandLine input) {
        super(defaults, options, input);
    }

    public static void main(final String... args) throws IOException {
        open(args, new ApplicationTerminalPreferences());
    }
}
