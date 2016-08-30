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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainLoginService;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordStoreFactory;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.lang3.StringUtils;

public class TerminalLoginService extends KeychainLoginService {

    private CommandLine input;

    public TerminalLoginService(final CommandLine input, final LoginCallback prompt) {
        super(prompt, input.hasOption(TerminalOptionsBuilder.Params.nokeychain.name())
                ? new DisabledPasswordStore() : PasswordStoreFactory.get());
        this.input = input;
    }

    @Override
    public void validate(final Host bookmark, final String message, final LoginOptions options) throws LoginCanceledException {
        final Credentials credentials = bookmark.getCredentials();
        if(input.hasOption(TerminalOptionsBuilder.Params.username.name())) {
            credentials.setUsername(input.getOptionValue(TerminalOptionsBuilder.Params.username.name()));
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.password.name())) {
            credentials.setPassword(input.getOptionValue(TerminalOptionsBuilder.Params.password.name()));
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.identity.name())) {
            credentials.setIdentity(LocalFactory.get(input.getOptionValue(TerminalOptionsBuilder.Params.identity.name())));
        }
        if(StringUtils.isNotBlank(credentials.getUsername())
                && StringUtils.isNotBlank(credentials.getPassword())) {
            return;
        }
        super.validate(bookmark, message, options);
    }
}
