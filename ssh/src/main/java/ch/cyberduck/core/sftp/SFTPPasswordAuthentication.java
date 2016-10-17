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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUpdateProvider;
import net.schmizz.sshj.userauth.password.Resource;

public class SFTPPasswordAuthentication implements SFTPAuthentication {
    private static final Logger log = Logger.getLogger(SFTPPasswordAuthentication.class);

    private SFTPSession session;

    public SFTPPasswordAuthentication(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public boolean authenticate(final Host bookmark, final LoginCallback callback, final CancelCallback cancel)
            throws BackgroundException {
        if(StringUtils.isBlank(bookmark.getCredentials().getPassword())) {
            return false;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using password authentication with credentials %s", bookmark.getCredentials()));
        }
        try {
            // Use both password and keyboard-interactive
            session.getClient().authPassword(bookmark.getCredentials().getUsername(), new PasswordFinder() {
                @Override
                public char[] reqPassword(final Resource<?> resource) {
                    return bookmark.getCredentials().getPassword().toCharArray();
                }

                @Override
                public boolean shouldRetry(final Resource<?> resource) {
                    return false;
                }
            }, new PasswordUpdateProvider() {
                @Override
                public char[] provideNewPassword(final Resource<?> resource, final String prompt) {
                    try {
                        final StringAppender message = new StringAppender().append(prompt);
                        final Credentials credentials = bookmark.getCredentials();
                        final Credentials changed = new Credentials(credentials.getUsername());
                        callback.prompt(bookmark, changed, LocaleFactory.localizedString("Change Password", "Credentials"), message.toString(),
                                new LoginOptions(bookmark.getProtocol()).anonymous(false).user(false).publickey(false));
                        return changed.getPassword().toCharArray();
                    }
                    catch(LoginCanceledException e) {
                        // Return null if user cancels
                        return null;
                    }
                }

                @Override
                public boolean shouldRetry(final Resource<?> resource) {
                    return true;
                }
            });
            return session.getClient().isAuthenticated();
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }
}
