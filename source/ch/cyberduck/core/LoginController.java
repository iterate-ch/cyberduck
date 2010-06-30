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
     * @throws LoginCanceledException
     */
    void check(Host host) throws LoginCanceledException;

    /**
     * @param host
     * @param reason
     * @throws LoginCanceledException
     */
    void check(Host host, String reason) throws LoginCanceledException;


    /**
     * Check the credentials for validity and prompt the user for the password if not found
     * in the login keychain
     *
     * @param host
     */
    void success(Host host);

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete sublcass could eg. display a panel.
     *
     * @param credentials
     * @param reason
     */
    void fail(Credentials credentials, String reason) throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete sublcass could eg. display a panel.
     *
     * @param credentials
     * @param message     Any additional information why the login failed.
     * @param reason
     * @throws LoginCanceledException
     */
    void prompt(Credentials credentials, String reason, String message) throws LoginCanceledException;

    /**
     * @param credentials
     * @param publickeyoption Enable public key authentication for SSH <code>publickey</code authentication method.
     * @param reason
     * @param message
     * @throws LoginCanceledException
     */
    void prompt(Credentials credentials, boolean publickeyoption, String reason, String message) throws LoginCanceledException;
}
