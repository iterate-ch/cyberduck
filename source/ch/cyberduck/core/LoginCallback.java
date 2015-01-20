package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.exception.LoginCanceledException;

public interface LoginCallback extends ConnectionCallback {

    /**
     * Call this to allow the user to reenter the new login credentials.
     * A concrete subclass should display a login prompt.
     *
     * @param bookmark    Used to determine login prompt options.
     * @param credentials The credentials to obtain.
     * @param title       The title for the login prompt
     * @param reason      The detail message for the login prompt. Any additional information why the login failed.
     * @param options     Enable checkbox to save password in keychain. Enable public key authentication checkbox. Enable anynomous login option checkbox
     * @throws LoginCanceledException When login is canceled and the prompt dismissed
     */
    void prompt(Host bookmark, Credentials credentials,
                String title, String reason,
                LoginOptions options) throws LoginCanceledException;

    /**
     * Prompt to open file for reading.
     *
     * @param identity File
     * @return Selected file or null
     */
    Local select(final Local identity) throws LoginCanceledException;
}
