package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;

public class TerminalPasswordCallback implements PasswordCallback {

    private final Console console = new Console();
    private final TerminalPromptReader prompt;

    public TerminalPasswordCallback() {
        this.prompt = new InteractiveTerminalPromptReader();
    }

    public TerminalPasswordCallback(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    @Override
    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        console.printf("%n%s", new StringAppender().append(title).append(reason));
        try {
            final char[] input = console.readPassword("%n%s: ", options.getPasswordPlaceholder());
            final Credentials credentials = new Credentials(String.valueOf(input));
            if(options.save && options.keychain) {
                credentials.setSaved(prompt.prompt(LocaleFactory.get().localize("Save password", "Credentials")));
            }
            else {
                credentials.setSaved(options.save);
            }
            return credentials;
        }
        catch(ConnectionCanceledException e) {
            throw new LoginCanceledException(e);
        }
    }
}
