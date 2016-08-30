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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class TerminalLoginCallback implements LoginCallback {

    private final Console console = new Console();

    private TerminalPromptReader prompt;

    public TerminalLoginCallback() {
        this.prompt = new InteractiveTerminalPromptReader();
    }

    public TerminalLoginCallback(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    @Override
    public void warn(final Protocol protocol, final String title, final String reason,
                     final String defaultButton, final String cancelButton, final String preference) throws ConnectionCanceledException {
        console.printf("%n%s", reason);
        if(!prompt.prompt(String.format("%s (y) or %s (n): ", defaultButton, cancelButton))) {
            // Switch protocol
            throw new LoginCanceledException();
        }
    }

    @Override
    public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason,
                       final LoginOptions options) throws LoginCanceledException {
        console.printf("%n%s", new StringAppender().append(title).append(reason));
        try {
            if(options.user) {
                if(StringUtils.isBlank(credentials.getUsername())) {
                    final String user = console.readLine("%n%s: ", credentials.getUsernamePlaceholder());
                    credentials.setUsername(user);
                }
                else {
                    final String user = console.readLine("%n%s (%s): ", credentials.getUsernamePlaceholder(), credentials.getUsername());
                    if(StringUtils.isNotBlank(user)) {
                        credentials.setUsername(user);
                    }
                }
                console.printf("Login as %s", credentials.getUsername());
            }
            if(options.password) {
                final char[] input = console.readPassword("%n%s: ", credentials.getPasswordPlaceholder());
                credentials.setPassword(String.valueOf(input));
                Arrays.fill(input, ' ');
                if(!credentials.validate(bookmark.getProtocol(), options)) {
                    this.prompt(bookmark, credentials, title, reason, options);
                }
            }
        }
        catch(ConnectionCanceledException e) {
            throw new LoginCanceledException(e);
        }
    }

    @Override
    public Local select(final Local identity) throws LoginCanceledException {
        return identity;
    }
}
