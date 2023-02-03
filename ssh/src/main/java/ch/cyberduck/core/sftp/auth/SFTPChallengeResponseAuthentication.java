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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;

public class SFTPChallengeResponseAuthentication implements AuthenticationProvider<Boolean> {
    private static final Logger log = LogManager.getLogger(SFTPChallengeResponseAuthentication.class);

    private static final Pattern DEFAULT_PROMPT_PATTERN = Pattern.compile(".*[pP]assword.*", Pattern.DOTALL);

    private final SSHClient client;

    public SFTPChallengeResponseAuthentication(final SSHClient client) {
        this.client = client;
    }

    @Override
    public Boolean authenticate(final Host bookmark, final LoginCallback callback, final CancelCallback cancel) throws BackgroundException {
        if(log.isDebugEnabled()) {
            log.debug(String.format("Login using challenge response authentication for %s", bookmark));
        }
        final AtomicBoolean canceled = new AtomicBoolean();
        final AtomicBoolean publickey = new AtomicBoolean();
        try {
            final Credentials credentials = bookmark.getCredentials();
            client.auth(credentials.getUsername(), new AuthKeyboardInteractive(new ChallengeResponseProvider() {
                private String name = StringUtils.EMPTY;
                private String instruction = StringUtils.EMPTY;

                @Override
                public List<String> getSubmethods() {
                    return Collections.emptyList();
                }

                @Override
                public void init(final Resource resource, final String name, final String instruction) {
                    if(StringUtils.isNotBlank(instruction)) {
                        this.instruction = instruction;
                    }
                    if(StringUtils.isNotBlank(name)) {
                        this.name = name;
                    }
                }

                @Override
                public char[] getResponse(final String prompt, final boolean echo) {
                    if(log.isDebugEnabled()) {
                        log.debug(String.format("Reply to challenge name %s with instruction %s", name, instruction));
                    }
                    if(DEFAULT_PROMPT_PATTERN.matcher(prompt).matches()) {
                        if(StringUtils.isBlank(credentials.getPassword())) {
                            try {
                                final Credentials input = callback.prompt(bookmark, credentials.getUsername(),
                                    String.format("%s %s", LocaleFactory.localizedString("Login", "Login"), bookmark.getHostname()),
                                    MessageFormat.format(LocaleFactory.localizedString(
                                        "Login {0} with username and password", "Credentials"), BookmarkNameProvider.toString(bookmark)),
                                    // Change of username or service not allowed
                                    new LoginOptions(bookmark.getProtocol()).user(false));
                                if(input.isPublicKeyAuthentication()) {
                                    credentials.setIdentity(input.getIdentity());
                                    publickey.set(true);
                                    // Return null to cancel if user wants to use public key auth
                                    return StringUtils.EMPTY.toCharArray();
                                }
                                credentials.setSaved(input.isSaved());
                                credentials.setPassword(input.getPassword());
                            }
                            catch(LoginCanceledException e) {
                                canceled.set(true);
                                // Return null if user cancels
                                return StringUtils.EMPTY.toCharArray();
                            }
                        }
                        return credentials.getPassword().toCharArray();
                    }
                    else {
                        final StringAppender message = new StringAppender().append(instruction).append(prompt);
                        // Properly handle an instruction field with embedded newlines.  They should also
                        // be able to display at least 30 characters for the name and prompts.
                        final Credentials additional;
                        try {
                            final StringAppender title = new StringAppender().append(
                                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials")
                            ).append(name);
                            additional = callback.prompt(bookmark, title.toString(),
                                message.toString(), new LoginOptions()
                                    .icon(bookmark.getProtocol().disk())
                                    .password(true)
                                    .user(false)
                                    .keychain(false)
                            );
                        }
                        catch(LoginCanceledException e) {
                            canceled.set(true);
                            // Return null if user cancels
                            return StringUtils.EMPTY.toCharArray();
                        }
                        // Responses are encoded in ISO-10646 UTF-8.
                        return additional.getPassword().toCharArray();
                    }
                }

                @Override
                public boolean shouldRetry() {
                    return false;
                }
            }));
        }
        catch(IOException e) {
            if(publickey.get()) {
                return new SFTPPublicKeyAuthentication(client).authenticate(bookmark, callback, cancel);
            }
            if(canceled.get()) {
                throw new LoginCanceledException();
            }
            throw new SFTPExceptionMappingService().map(e);
        }
        return client.isAuthenticated();
    }

    @Override
    public String getMethod() {
        return "keyboard-interactive";
    }
}
