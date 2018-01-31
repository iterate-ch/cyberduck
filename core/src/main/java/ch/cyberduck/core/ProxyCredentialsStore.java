package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

public interface ProxyCredentialsStore {

    /**
     * Find proxy credentials for a proxy host
     *
     * @param proxy Proxy hostname
     * @return Credentials if found. Empty values if not found
     */
    Credentials getCredentials(String proxy);

    /**
     * Add proxy credentials
     *
     * @param proxy       Proxy hostname
     * @param accountName Account
     * @param password    Password to save for proxy
     */
    void addCredentials(String proxy, String accountName, String password);

    /**
     * Delete proxy credentials from store
     *
     * @param proxy Proxy hostname
     */
    void deleteCredentials(String proxy);
}
