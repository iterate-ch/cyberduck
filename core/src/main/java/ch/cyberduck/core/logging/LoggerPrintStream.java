package ch.cyberduck.core.logging;

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

import org.apache.commons.io.output.NullOutputStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;

import java.io.OutputStream;
import java.io.PrintStream;

public class LoggerPrintStream extends PrintStream {
    private static final Logger log = LogManager.getLogger(LoggerPrintStream.class);

    static {
        StatusLogger.getLogger().setLevel(Level.OFF);
    }

    private final Level level;

    public LoggerPrintStream() {
        this(NullOutputStream.NULL_OUTPUT_STREAM);
    }

    public LoggerPrintStream(final OutputStream out) {
        this(out, Level.WARN);
    }

    public LoggerPrintStream(final OutputStream out, final Level level) {
        super(out);
        this.level = level;
    }

    @Override
    public void println(final String message) {
        log.log(level, message);
    }

    /**
     * Print stacktrace
     */
    @Override
    public void println(final Object x) {
        this.println(x != null ? x.toString() : null);
    }
}
