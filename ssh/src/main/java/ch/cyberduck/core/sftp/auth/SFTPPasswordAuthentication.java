package ch.cyberduck.core.sftp.auth;

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
import ch.cyberduck.core.BookmarkNameProvider;
import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.StringAppender;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.sftp.SFTPExceptionMappingService;
import ch.cyberduck.core.threading.CancelCallback;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.method.AuthPassword;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUpdateProvider;
import net.schmizz.sshj.userauth.password.Resource;

public class SFTPPasswordAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPPasswordAuthentication.class);

    private final SSHClient client;

    public SFTPPasswordAuthentication(final SSHClient client) {
        this.client = client;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final LoginCallback callback, final CancelCallback cancel)
            throws BackgroundException {
        final Credentials credentials = bookmark.getCredentials();
        if(StringUtils.isBlank(credentials.getPassword())) {
            final Credentials input = callback.prompt(bookmark, credentials.getUsername(),
                    String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                    MessageFormat.format(LocaleFactory.localizedString(
                            "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(bookmark)),
                    // Change of username or service not allowed
                    new LoginOptions(bookmark.getProtocol()).user(false));
            if(input.isPublicKeyAuthentication()) {
                credentials.setIdentity(input.getIdentity());
                return new SFTPPublicKeyAuthentication(client).authenticate(bookmark, callback, cancel);
            }
            credentials.setSaved(input.isSaved());
            credentials.setPassword(input.getPassword());
        }
        return this.authenticate(bookmark, credentials, callback, cancel);
    }

    @Override
    public String getMethod() {
        return "password";
    }

    private boolean authenticate(final Host host, final Credentials credentials, final LoginCallback callback, final CancelCallback cancel)
            throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug("Login using password authentication with credentials {}", credentials);
        }
        try {
            // Use both password and keyboard-interactive
            client.auth(credentials.getUsername(), new AuthPassword(new PasswordFinder() {
                @Override
                public char[] reqPassword(final Resource<?> resource) {
                    return credentials.getPassword().toCharArray();
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
                        final Credentials changed = callback.prompt(host, credentials.getUsername(), LocaleFactory.localizedString("Change Password", "Credentials"), message.toString(),
                                new LoginOptions(host.getProtocol()).anonymous(false).user(false).publickey(false));
                        return changed.getPassword().toCharArray();
                    }
                    catch(LoginCanceledException e) {
                        // Return null if user cancels
                        return StringUtils.EMPTY.toCharArray();
                    }
                }

                @Override
                public boolean shouldRetry(final Resource<?> resource) {
                    return false;
                }
            }));
            return client.isAuthenticated();
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }
}
