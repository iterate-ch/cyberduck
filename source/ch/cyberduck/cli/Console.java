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

import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;

/**
 * @version $Id$
 */
public class Console {

    private final java.io.Console console = System.console();

    public String readLine(String format, Object... args) throws ConnectionCanceledException {
        if(console != null) {
            return console.readLine(format, args);
        }
        System.out.print(String.format(format, args));
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return reader.readLine();
        }
        catch(IOException e) {
            throw new ConnectionCanceledException(e);
        }
    }

    public char[] readPassword(String format, Object... args) throws ConnectionCanceledException {
        if(console != null) {
            return console.readPassword(format, args);
        }
        final String line = this.readLine(format, args);
        if(StringUtils.isBlank(line)) {
            throw new LoginCanceledException();
        }
        return line.toCharArray();
    }

    public void printf(final String format, String... args) {
        if(console != null) {
            final PrintWriter writer = console.writer();
            if(Arrays.asList(args).isEmpty()) {
                writer.print(format);
            }
            else {
                writer.printf(format, args);
            }
            writer.flush();
        }
        else {
            final PrintStream writer = System.out;
            writer.printf(format, args);
            writer.flush();
        }
    }
}