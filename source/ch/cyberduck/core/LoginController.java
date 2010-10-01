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
     * Using default Continue and Disconnct button.
     * if disabled permamently.
     *
     * @param title      Title in alert window
     * @param message    Message in alert window
     * @param preference Where to save preference if dismissed
     * @throws LoginCanceledException If the other option has been selected.
     */
    void warn(String title, String message, String preference) throws LoginCanceledException;

    /**
     * Display warning sheet. Block connection until decision is made.
     *
     * @param title         Title in alert window
     * @param message       Message in alert window
     * @param continueButton Button title for default button
     * @param disconnectButton   Button title for other button
     * @param preference    Where to save preference if dismissed
     * @throws LoginCanceledException If the other option has been selected.
     */
    void warn(String title, String message, String continueButton, String disconnectButton, String preference) throws LoginCanceledException;

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
     * Callback upon successful login. Save credentials.
     *
     * @param host
     */
    void success(Host host);

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

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param protocol        Used to determine login prompt options.
     * @param credentials     The credentials to obtain.
     * @param title           The title for the login prompt
     * @param reason          The detail message for the login prompt. Any additional information why the login failed.
     * @param enableKeychain  Enable checkbox to save password in keychain
     * @param enablePublicKey Enable public key authentication checkbox
     * @param enableAnonymous
     * @throws LoginCanceledException
     */
    void prompt(final Protocol protocol, final Credentials credentials,
                final String title, final String reason,
                final boolean enableKeychain, final boolean enablePublicKey, boolean enableAnonymous) throws LoginCanceledException;
}
