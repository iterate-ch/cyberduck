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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledHostKeyCallback;
import ch.cyberduck.core.DisabledLoginController;
import ch.cyberduck.core.DisabledPasswordStore;
import ch.cyberduck.core.LoginConnectionService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * @version $Id$
 */
public class FTPDataFallback {
    private static final Logger log = Logger.getLogger(FTPSession.class);

    private FTPSession session;

    public FTPDataFallback(final FTPSession session) {
        this.session = session;
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    protected <T> T data(final Path file, final DataConnectionAction<T> action)
            throws IOException, BackgroundException {
        try {
            // Make sure to always configure data mode because connect event sets defaults.
            if(session.getConnectMode().equals(FTPConnectMode.passive)) {
                session.getClient().enterLocalPassiveMode();
            }
            else if(session.getConnectMode().equals(FTPConnectMode.active)) {
                session.getClient().enterLocalActiveMode();
            }
            return action.execute();
        }
        catch(BackgroundException failure) {
            if(failure.getCause() instanceof FTPException) {
                log.warn(String.format("Server denied data socket %s", failure.getMessage()));
                // Fallback handling
                if(Preferences.instance().getBoolean("ftp.connectmode.fallback")) {
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
            if(failure.getCause() instanceof SocketTimeoutException) {
                log.warn(String.format("Timeout opening data socket %s", failure.getMessage()));
                // Fallback handling
                if(Preferences.instance().getBoolean("ftp.connectmode.fallback")) {
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
                            new LoginConnectionService(new DisabledLoginController(), new DisabledHostKeyCallback(),
                                    new DisabledPasswordStore(), session).connect(session, Cache.<Path>empty());
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
