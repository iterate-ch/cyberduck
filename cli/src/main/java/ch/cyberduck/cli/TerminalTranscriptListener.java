package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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

import ch.cyberduck.core.TranscriptListener;

import org.fusesource.jansi.Ansi;

public class TerminalTranscriptListener implements TranscriptListener {

    private final Console console = new Console();

    @Override
    public void log(final Type request, final String message) {
        switch(request) {
            case request:
                console.printf("%n%s> %s%s", Ansi.ansi().fg(Ansi.Color.GREEN), message, Ansi.ansi().reset());
                break;
            case response:
                console.printf("%n%s< %s%s", Ansi.ansi().fg(Ansi.Color.RED), message, Ansi.ansi().reset());
                break;
        }
    }
}
