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
import ch.cyberduck.core.exception.LoginCanceledException;

import org.apache.log4j.Logger;

import java.io.IOException;

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
                        private int promptCount = 0;

                        /**
                         * The callback may be invoked several times, depending on how
                         * many questions-sets the server sends
                         */
                        @Override
                        public String[] replyToChallenge(String name, String instruction, int numPrompts, String[] prompt,
                                                         boolean[] echo) throws LoginCanceledException {
                            log.debug("replyToChallenge:" + name);
                            // In its first callback the server prompts for the password
                            if(0 == promptCount) {
                                if(log.isDebugEnabled()) {
                                    log.debug("First callback returning provided credentials");
                                }
                                promptCount++;
                                return new String[]{credentials.getPassword()};
                            }
                            String[] response = new String[numPrompts];
                            for(int i = 0; i < numPrompts; i++) {
                                controller.prompt(host.getProtocol(), credentials,
                                        LocaleFactory.localizedString("Provide additional login credentials", "Credentials"), prompt[i], new LoginOptions());
                                response[i] = credentials.getPassword();
                                promptCount++;
                            }
                            return response;
                        }
                    });
        }
        return false;
    }
}
