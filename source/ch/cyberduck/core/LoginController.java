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
     * Display warning sheet. Block connection until decision is made.
     *
     * @param title            Title in alert window
     * @param message          Message in alert window
     * @param continueButton   Button title for default button
     * @param disconnectButton Button title for other button
     * @param preference       Where to save preference if dismissed
     * @throws LoginCanceledException If the other option has been selected.
     */
    void warn(String title, String message, String continueButton, String disconnectButton,
              String preference) throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param protocol    Used to determine login prompt options.
     * @param credentials The credentials to obtain.
     * @param title       The title for the login prompt
     * @param reason      The detail message for the login prompt. Any additional information why the login failed.
     * @throws LoginCanceledException When login is canceled and the prompt dismissed
     */
    void prompt(Protocol protocol, Credentials credentials,
                String title, String reason) throws LoginCanceledException;

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param protocol    Used to determine login prompt options.
     * @param credentials The credentials to obtain.
     * @param title       The title for the login prompt
     * @param reason      The detail message for the login prompt. Any additional information why the login failed.
     * @param options     Enable checkbox to save password in keychain. Enable public key authentication checkbox. Enable anynomous login option checkbox
     * @throws LoginCanceledException When login is canceled and the prompt dismissed
     */
    void prompt(Protocol protocol, Credentials credentials,
                String title, String reason,
                LoginOptions options) throws LoginCanceledException;
}
