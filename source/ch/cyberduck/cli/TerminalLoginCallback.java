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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * @version $Id$
 */
public class TerminalLoginCallback implements LoginCallback {

    private final Console console = new Console();

    @Override
    public void warn(final Protocol protocol, final String title, final String reason,
                     final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
        console.printf("%s\n", reason);
        final String input = console.readLine("%s (y) or %s (n): ", defaultButton, cancelButton);
        switch(input) {
            case "y":
                break;
            case "n":
                throw new ConnectionCanceledException();
            default:
                console.printf("Please type 'y' or 'n'");
                this.warn(protocol, title, reason, defaultButton, cancelButton, preference);
        }
    }

    @Override
    public void prompt(final Protocol protocol, final Credentials credentials, final String title, final String reason,
                       final LoginOptions options) throws LoginCanceledException {
        credentials.setSaved(false);
        console.printf("%s\n", reason);
        if(StringUtils.isBlank(credentials.getUsername())) {
            final String user = console.readLine("%s: ", protocol.getUsernamePlaceholder());
            credentials.setUsername(user);
        }
        else {
            final String user = console.readLine("%s (%s): ", protocol.getUsernamePlaceholder(), credentials.getUsername());
            if(StringUtils.isNotBlank(user)) {
                credentials.setUsername(user);
            }
        }
        console.printf("Login as %s\n", credentials.getUsername());
        final char[] password = console.readPassword("%s: ", protocol.getPasswordPlaceholder());
        credentials.setPassword(String.valueOf(password));
        Arrays.fill(password, ' ');
        if(!credentials.validate(protocol, options)) {
            this.prompt(protocol, credentials, title, reason, options);
        }
    }

    @Override
    public Local select(final Local identity) throws LoginCanceledException {
        return identity;
    }
}
