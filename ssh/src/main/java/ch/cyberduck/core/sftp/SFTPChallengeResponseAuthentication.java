package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AuthenticationProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;

public class SFTPChallengeResponseAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = Logger.getLogger(SFTPChallengeResponseAuthentication.class);

    private final SFTPSession session;

    private static final char[] EMPTY_RESPONSE = new char[0];

    public SFTPChallengeResponseAuthentication(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final HostPasswordStore keychain, final LoginCallback callback, final CancelCallback cancel) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using challenge response authentication for %s", bookmark));
        }
        try {
            session.getClient().auth(bookmark.getCredentials().getUsername(), new AuthKeyboardInteractive(new ChallengeResponseProvider() {
                private String name = StringUtils.EMPTY;
                private String instruction = StringUtils.EMPTY;

                @Override
                public List<String> getSubmethods() {
                    return Collections.emptyList();
                }

                @Override
                public void init(final Resource resource, final String name, final String instruction) {
                    if(StringUtils.isNoneBlank(instruction)) {
                        this.instruction = instruction;
                    }
                    if(StringUtils.isNoneBlank(name)) {
                        this.name = name;
                    }
                }

                @Override
                public char[] getResponse(final String prompt, final boolean echo) {
                    // For each prompt, the corresponding echo field indicates whether the user input should be echoed as characters are typed
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Reply to challenge name %s with instruction %s", name, instruction));
                    }
                    final StringAppender message = new StringAppender().append(instruction).append(prompt);
                    // Properly handle an instruction field with embedded newlines.  They should also
                    // be able to display at least 30 characters for the name and prompts.
                    final Credentials additional;
                    try {
                        final StringAppender title = new StringAppender().append(name).append(
                            LocaleFactory.localizedString("Provide additional login credentials", "Credentials")
                        );
                        additional = callback.prompt(bookmark, bookmark.getCredentials().getUsername(), title.toString(),
                            message.toString(), new LoginOptions(bookmark.getProtocol()).user(false).publickey(false).keychain(false)
                                .usernamePlaceholder(bookmark.getCredentials().getUsername())
                        );
                    }
                    catch(LoginCanceledException e) {
                        return EMPTY_RESPONSE;
                    }
                    // Responses are encoded in ISO-10646 UTF-8.
                    return additional.getPassword().toCharArray();
                }

                @Override
                public boolean shouldRetry() {
                    return false;
                }
            }));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
        return session.getClient().isAuthenticated();
    }
}
