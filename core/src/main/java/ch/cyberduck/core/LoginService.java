package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.LoginFailureException;
import ch.cyberduck.core.proxy.Proxy;
import ch.cyberduck.core.threading.CancelCallback;

public interface LoginService {
    /**
     * Obtain password from keychain or prompt panel
     *
     * @param bookmark Credentials
     * @param prompt    Login prompt
     * @param options  Login mechanism features
     */
    void validate(Host bookmark, LoginCallback prompt, LoginOptions options) throws ConnectionCanceledException, LoginFailureException;

    /**
     * Login and prompt on failure
     *
     * @param proxy    Proxy configuration
     * @param session  Session
     * @param listener Authentication message callback
     * @param prompt    Login prompt
     * @param cancel   Cancel callback while authentication is in progress
     * @return False if authentication fails
     * @throws LoginCanceledException Login prompt canceled by user
     * @throws LoginFailureException  Login attempt failed
     */
    boolean authenticate(Proxy proxy, Session session, ProgressListener listener, LoginCallback prompt, CancelCallback cancel) throws BackgroundException;
}
