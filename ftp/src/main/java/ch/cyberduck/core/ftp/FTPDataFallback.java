package ch.cyberduck.core.ftp;

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

import ch.cyberduck.core.DisabledCancelCallback;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginCallback;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.DisabledTranscriptListener;
import ch.cyberduck.core.HostPasswordStore;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.ProgressListener;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.io.IOException;

public class FTPDataFallback {
    private static final Logger log = Logger.getLogger(FTPDataFallback.class);

    private final FTPSession session;

    private final HostPasswordStore keychain;

    private final LoginCallback prompt;

    private final Preferences preferences = PreferencesFactory.get();

    public FTPDataFallback(final FTPSession session) {
        this(session, new DisabledPasswordStore(), new DisabledLoginCallback());
    }

    public FTPDataFallback(final FTPSession session, final HostPasswordStore keychain, final LoginCallback prompt) {
        this.session = session;
        this.keychain = keychain;
        this.prompt = prompt;
    }

    /**
     * @param action   Action that needs to open a data connection
     * @param listener Progress callback
     * @return True if action was successful
     */
    protected <T> T data(final DataConnectionAction<T> action, final ProgressListener listener)
            throws IOException, BackgroundException {
        try {
            // Make sure to always configure data mode because connect event sets defaults.
            final FTPConnectMode mode = session.getConnectMode();
            switch(mode) {
                case active:
                    session.getClient().enterLocalActiveMode();
                    break;
                case passive:
                    session.getClient().enterLocalPassiveMode();
                    break;
            }
            return action.execute();
        }
        catch(ConnectionTimeoutException failure) {
            log.warn(String.format("Timeout opening data socket %s", failure.getMessage()));
            // Fallback handling
            if(preferences.getBoolean("ftp.connectmode.fallback")) {
                try {
                    try {
                        session.getClient().completePendingCommand();
                        // Expect 421 response
                        log.warn(String.format("Aborted connection %d %s",
                                session.getClient().getReplyCode(), session.getClient().getReplyString()));
                    }
                    catch(IOException e) {
                        log.warn(String.format("Ignore failure completing pending command %s", e.getMessage()));
                        // Reconnect
                        new LoginConnectionService(
                                prompt,
                                new DisabledHostKeyCallback(),
                                keychain,
                                listener,
                                new DisabledTranscriptListener()
                        ).connect(session, PathCache.empty(), new DisabledCancelCallback());
                    }
                    return this.fallback(action);
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Connect mode fallback failed with %s", e.getMessage()));
                    // Throw original error message
                }
            }
            throw failure;
        }
        catch(InteroperabilityException | NotfoundException | AccessDeniedException failure) {
            log.warn(String.format("Server denied data socket operation with %s", failure.getMessage()));
            // Fallback handling
            if(preferences.getBoolean("ftp.connectmode.fallback")) {
                try {
                    return this.fallback(action);
                }
                catch(BackgroundException e) {
                    log.warn(String.format("Connect mode fallback failed with %s", e.getMessage()));
                    // Throw original error message
                }
            }
            throw failure;
        }
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    protected <T> T fallback(final DataConnectionAction<T> action) throws BackgroundException {
        // Fallback to other connect mode
        if(session.getClient().getDataConnectionMode() == FTPClient.PASSIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to active data connection");
            session.getClient().enterLocalActiveMode();
        }
        else if(session.getClient().getDataConnectionMode() == FTPClient.ACTIVE_LOCAL_DATA_CONNECTION_MODE) {
            log.warn("Fallback to passive data connection");
            session.getClient().enterLocalPassiveMode();
        }
        return action.execute();
    }
}
