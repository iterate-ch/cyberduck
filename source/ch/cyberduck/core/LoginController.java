package ch.cyberduck.core;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

public interface LoginController {

    /**
     * Check the credentials for validity and prompt the user for the password if not found
     * in the login keychain
     *
     * @param host
     * @param title  The title for the login prompt
     * @param reason The detail message for the login prompt. Any additional information about the domain.
     * @throws LoginCanceledException
     */
    void check(Host host, String title, String reason) throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param protocol    Used to determine login prompt options.
     * @param credentials
     */
    void fail(Protocol protocol, Credentials credentials) throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param protocol    Used to determine login prompt options.
     * @param credentials
     * @param reason      The detail message for the login prompt. Any additional information why the login failed.
     */
    void fail(Protocol protocol, Credentials credentials, String reason) throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param protocol    Used to determine login prompt options.
     * @param credentials The credentials to obtain.
     * @param title       The title for the login prompt
     * @param reason      The detail message for the login prompt. Any additional information why the login failed.
     * @throws LoginCanceledException
     */
    void prompt(Protocol protocol, Credentials credentials, String title, String reason) throws LoginCanceledException;
}
