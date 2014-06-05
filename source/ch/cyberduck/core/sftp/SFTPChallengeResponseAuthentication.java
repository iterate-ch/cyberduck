package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.ethz.ssh2.InteractiveCallback;

/**
 * @version $Id$
 */
public class SFTPChallengeResponseAuthentication implements SFTPAuthentication {
    private static final Logger log = Logger.getLogger(SFTPChallengeResponseAuthentication.class);

    private SFTPSession session;

    public SFTPChallengeResponseAuthentication(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean authenticate(final Host host, final LoginCallback controller) throws IOException, LoginCanceledException {
        return this.authenticate(host, host.getCredentials(), controller);
    }

    public boolean authenticate(final Host host, final Credentials credentials, final LoginCallback controller)
            throws IOException, LoginCanceledException {
        if(StringUtils.isBlank(host.getCredentials().getPassword())) {
            return false;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using challenge response authentication with credentials %s", credentials));
        }
        if(session.getClient().isAuthMethodAvailable(credentials.getUsername(), "keyboard-interactive")) {
            return session.getClient().authenticateWithKeyboardInteractive(credentials.getUsername(),
                    /**
                     * The logic that one has to implement if "keyboard-interactive" authentication shall be
                     * supported.
                     */
                    new InteractiveCallback() {
                        /**
                         * Password sent flag
                         */
                        private final AtomicBoolean password = new AtomicBoolean();

                        /**
                         * The callback may be invoked several times, depending on how
                         * many questions-sets the server sends
                         */
                        @Override
                        public String[] replyToChallenge(final String name, final String instruction,
                                                         final int numPrompts, final String[] prompt,
                                                         boolean[] echo) throws LoginCanceledException {
                            if(log.isDebugEnabled()) {
                                log.debug(String.format("Reply to challenge name %s with instruction %s", name, instruction));
                            }
                            final String[] response = new String[numPrompts];
                            // The num-prompts field may be `0', in which case there will be no
                            // prompt/echo fields in the message, but the client should still
                            // display the name and instruction fields
                            if(0 == numPrompts) {
                                // In the case that the server sends a `0' num-prompts field in the request message, the
                                // client must send a response message with a `0' num-responses field to complete the exchange.
                                return response;
                            }
                            else {
                                for(int i = 0; i < numPrompts; i++) {
                                    // For each prompt, the corresponding echo field indicates whether the user input should
                                    // be echoed as characters are typed
                                    if(!password.get()) {
                                        // In its first callback the server prompts for the password
                                        if(log.isDebugEnabled()) {
                                            log.debug("First callback returning provided credentials");
                                        }
                                        response[i] = credentials.getPassword();
                                        password.set(true);
                                    }
                                    else {
                                        final StringAppender message = new StringAppender()
                                                .append(instruction).append(prompt[i]);
                                        // Properly handle an instruction field with embedded newlines.  They should also
                                        // be able to display at least 30 characters for the name and prompts.
                                        final Credentials additional = new Credentials();
                                        controller.prompt(host.getProtocol(), additional,
                                                String.format("%s. %s",
                                                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"),
                                                        name), message.toString(), new LoginOptions().user(false).keychain(false));
                                        response[i] = additional.getPassword();
                                    }
                                }
                            }
                            // Responses are encoded in ISO-10646 UTF-8.
                            return response;
                        }
                    });
        }
        return false;
    }
}
