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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionTimeoutException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class DataConnectionActionExecutor {
    private static final Logger log = LogManager.getLogger(DataConnectionActionExecutor.class);

    private final FTPSession session;
    private final boolean enabled;

    public DataConnectionActionExecutor(final FTPSession session) {
        this(session, PreferencesFactory.get().getBoolean("ftp.connectmode.fallback"));
    }

    public DataConnectionActionExecutor(final FTPSession session, final boolean enabled) {
        this.session = session;
        this.enabled = enabled;
    }

    /**
     * @param action Action that needs to open a data connection
     * @return True if action was successful
     */
    public <T> T data(final DataConnectionAction<T> action) throws IOException, BackgroundException {
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
            log.warn("Timeout opening data socket {}", failure.getMessage());
            // Expect 421 response
            session.getClient().completePendingCommand();
            // Fallback handling
            if(enabled) {
                try {
                    return this.fallback(action);
                }
                catch(BackgroundException e) {
                    log.warn("Connect mode fallback failed with {}", e.getMessage());
                    // Throw original error message
                }
            }
            throw failure;
        }
        catch(InteroperabilityException | NotfoundException | AccessDeniedException failure) {
            log.warn("Server denied data socket operation with {}", failure.getMessage());
            // Fallback handling
            if(enabled) {
                try {
                    return this.fallback(action);
                }
                catch(BackgroundException e) {
                    log.warn("Connect mode fallback failed with {}", e.getMessage());
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
