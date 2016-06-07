package ch.cyberduck.core.identity;

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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.exception.BackgroundException;

public interface IdentityConfiguration {

    /**
     * Remove user
     *
     * @param username Username
     * @param prompt   Callback
     */
    void delete(String username, LoginCallback prompt) throws BackgroundException;

    /**
     * Verify user exists and find access key and secret key in keychain
     *
     * @param username Username Username assigned such as iam.qloudstat
     * @return Access credentials for user or null if not found
     */
    Credentials getCredentials(String username);

    /**
     * Create new user and create access credentials
     *
     * @param username Username
     * @param policy   Policy language document
     * @param prompt   Callback
     */
    void create(String username, String policy, LoginCallback prompt) throws BackgroundException;
}
