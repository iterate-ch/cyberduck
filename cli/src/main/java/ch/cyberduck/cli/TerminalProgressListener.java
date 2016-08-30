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

import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.StringAppender;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

public class TerminalProgressListener implements ProgressListener {

    private final Console console = new Console();

    @Override
    public void message(final String message) {
        if(StringUtils.isBlank(message)) {
            return;
        }
        final StringAppender appender = new StringAppender('…');
        appender.append(message);
        // Clear the line and append message. Used instead of \r because the line width may vary
        console.printf("\r%s%s%s", Ansi.ansi()
                .saveCursorPosition()
                .eraseLine(Ansi.Erase.ALL)
                        .restoreCursorPosition(), appender.toString(),
                Ansi.ansi().reset());
    }
}
