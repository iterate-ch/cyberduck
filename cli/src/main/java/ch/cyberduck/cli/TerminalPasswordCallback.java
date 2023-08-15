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
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.commons.lang3.StringUtils;

public class TerminalPasswordCallback implements PasswordCallback {

    private final Console console = new Console();
    private final TerminalPromptReader prompt;

    public TerminalPasswordCallback() {
        this(new InteractiveTerminalPromptReader());
    }

    public TerminalPasswordCallback(final TerminalPromptReader prompt) {
        this.prompt = prompt;
    }

    @Override
    public void close(final String input) {
        console.printf("%s%n", input);
    }

    @Override
    public Credentials prompt(final Host bookmark, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
        console.printf("%n%s", new StringAppender().append(title).append(reason));
        try {
            final char[] input = console.readPassword("%n%s: ", options.getPasswordPlaceholder());
            final Credentials credentials = new Credentials();
            credentials.setPassword(StringUtils.strip(String.valueOf(input)));
            return this.options(options, credentials);
        }
        catch(ConnectionCanceledException e) {
            throw new LoginCanceledException(e);
        }
    }

    /**
     * Handle options and configure credentials accordingly
     */
    protected Credentials options(final LoginOptions options, final Credentials credentials) {
        if(options.keychain) {
            if(!PreferencesFactory.get().getBoolean("keychain.secure")) {
                console.printf(String.format("WARNING! Passwords are stored in plain text in %s.",
                        LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "credentials").getAbbreviatedPath()));
            }
            credentials.setSaved(prompt.prompt(LocaleFactory.get().localize("Save password", "Credentials")));
        }
        else {
            credentials.setSaved(false);
        }
        return credentials;
    }
}
