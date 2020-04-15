package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.fusesource.jansi.Ansi;

public class TerminalAppender extends AppenderSkeleton {

    private final Console console = new Console();

    @Override
    public void append(final LoggingEvent event) {
        if(null == event.getMessage()) {
            return;
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(layout.format(event));
        console.printf("\r%s%s%s", Ansi.ansi()
                .fg(Ansi.Color.YELLOW)
                .saveCursorPosition()
                .eraseLine(Ansi.Erase.ALL)
                .restoreCursorPosition(), buffer.toString(),
            Ansi.ansi().reset());
    }

    @Override
    public void close() {
        //
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
