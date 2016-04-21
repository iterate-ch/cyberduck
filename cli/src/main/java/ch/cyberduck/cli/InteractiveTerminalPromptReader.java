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

import ch.cyberduck.core.exception.ConnectionCanceledException;

public class InteractiveTerminalPromptReader implements TerminalPromptReader {

    private final Console console = new Console();

    @Override
    public boolean prompt(final String message) {
        final String input;
        try {
            input = console.readLine("%n%s (y/n): ", message);
        }
        catch(ConnectionCanceledException e) {
            return false;
        }
        switch(input) {
            case "y":
                return true;
            case "n":
                return false;
            default:
                console.printf("Please type 'y' or 'n'.");
                return this.prompt(message);
        }
    }
}
