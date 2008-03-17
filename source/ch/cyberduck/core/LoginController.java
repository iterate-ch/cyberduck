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
     * @param credentials
     * @param protocol
     * @param hostname
     * @throws LoginCanceledException
     */
    void check(Credentials credentials, Protocol protocol, String hostname)
            throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete sublcass could eg. display a panel.
     *
     * @param protocol
     * @param credentials
     * @param reason
     */
    void fail(final Protocol protocol, final Credentials credentials, final String reason)
            throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete sublcass could eg. display a panel.
     *
     * @param message Any additional information why the login failed.
     */
    void prompt(Protocol protocol, Credentials credentials, String reason, String message)
            throws LoginCanceledException;
}
