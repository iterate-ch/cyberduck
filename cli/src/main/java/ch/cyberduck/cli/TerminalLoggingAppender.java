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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.util.Strings;
import org.fusesource.jansi.Ansi;

import java.nio.charset.StandardCharsets;

public class TerminalLoggingAppender extends AbstractAppender {

    private final Console console = new Console();

    public TerminalLoggingAppender(final Layout layout) {
        super(TerminalAppender.class.getName(), null, layout, true, Property.EMPTY_ARRAY);
    }

    @Override
    public void append(final LogEvent event) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(new String(getLayout().toByteArray(event), StandardCharsets.UTF_8));
        if(ignoreExceptions()) {
            final Throwable thrown = event.getThrown();
            if(thrown != null) {
                buffer.append(Strings.LINE_SEPARATOR);
                final String[] trace = ExceptionUtils.getStackFrames(thrown);
                for(final String t : trace) {
                    buffer.append(t).append(Strings.LINE_SEPARATOR);
                }
            }
        }
        console.printf("\r%s%s%s", Ansi.ansi()
                .saveCursorPosition()
                .eraseLine(Ansi.Erase.ALL)
                .fg(Ansi.Color.MAGENTA)
                .restoreCursorPosition(),
            buffer.toString(), Ansi.ansi().reset());
    }
}
