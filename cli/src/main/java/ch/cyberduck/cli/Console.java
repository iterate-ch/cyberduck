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

import ch.cyberduck.core.Factory;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

public class Console {

    private final java.io.Console console
            = System.console();

    private final PrintStream out
            = AnsiConsole.out();

    private static final Semaphore lock
            = new Semaphore(1);

    public String readLine(String format, Object... args) throws ConnectionCanceledException {
        if(console != null) {
            return this.wrap(console.readLine(format, args));
        }
        this.printf(format, args);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try {
            return this.wrap(reader.readLine());
        }
        catch(IOException e) {
            throw new ConnectionCanceledException(e);
        }
    }

    public char[] readPassword(String format, Object... args) throws ConnectionCanceledException {
        if(console != null) {
            return this.wrap(console.readPassword(format, args));
        }
        final String line = this.readLine(format, args);
        if(StringUtils.isBlank(line)) {
            throw new LoginCanceledException();
        }
        return line.toCharArray();
    }

    public void printf(final String format, Object... args) {
        if(StringUtils.isEmpty(format)) {
            return;
        }
        try {
            lock.acquire();
            if(console != null) {
                switch(Factory.Platform.getDefault()) {
                    case windows:
                        break;
                    default:
                        final PrintWriter writer = console.writer();
                        if(Arrays.asList(args).isEmpty()) {
                            writer.print(format);
                        }
                        else {
                            writer.printf(format, args);
                        }
                        writer.flush();
                        return;
                }
            }
            if(Arrays.asList(args).isEmpty()) {
                out.printf(format);
            }
            else {
                out.printf(format, args);
            }
            out.flush();
        }
        catch(InterruptedException e) {
            //
        }
        finally {
            lock.release();
        }
    }

    private String wrap(final String input) throws ConnectionCanceledException {
        if(null == input) {
            throw new ConnectionCanceledException();
        }
        return input;
    }

    private char[] wrap(final char[] input) throws ConnectionCanceledException {
        if(null == input) {
            throw new ConnectionCanceledException();
        }
        return input;
    }
}