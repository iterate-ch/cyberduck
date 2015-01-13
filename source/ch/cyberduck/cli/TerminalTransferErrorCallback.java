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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.transfer.TransferErrorCallback;

/**
 * @version $Id$
 */
public class TerminalTransferErrorCallback implements TransferErrorCallback {

    private Console console = new Console();

    @Override
    public boolean prompt(final BackgroundException failure) throws BackgroundException {
        final StringAppender appender = new StringAppender();
        appender.append(failure.getMessage());
        appender.append(failure.getDetail());
        console.printf("%n%s", appender.toString());
        return this.print(failure);
    }

    private boolean print(final BackgroundException failure) throws BackgroundException {
        final String input = console.readLine(" %s? (y/n): ", LocaleFactory.localizedString("Continue", "Credentials"));
        switch(input) {
            case "y":
                return true;
            case "n":
                throw failure;
            default:
                console.printf("Please type 'y' or 'n'");
                return this.print(failure);
        }
    }
}
