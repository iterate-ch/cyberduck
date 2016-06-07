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
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.threading.CancelCallback;

public interface LoginService {

    /**
     * Login and prompt on failure
     *
     * @param session  Session
     * @param cache    Directory listing cache
     * @param listener Authentication message callback
     * @param cancel   Cancel callback while authentication is in progress
     */
    void authenticate(Session session, Cache<Path> cache, ProgressListener listener,
                      CancelCallback cancel) throws BackgroundException;

    /**
     * Obtain password from keychain or prompt panel
     *
     * @param bookmark Credentials
     * @param message  Prompt message
     * @param options  Login mechanism features
     */
    void validate(Host bookmark, String message, LoginOptions options) throws LoginCanceledException;
}
