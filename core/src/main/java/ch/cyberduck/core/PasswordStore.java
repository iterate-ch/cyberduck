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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;

public interface PasswordStore {

    /**
     * Find generic password for application
     *
     * @param serviceName Service
     * @param accountName Account
     * @return Password if found or null otherwise
     */
    String getPassword(String serviceName, String accountName) throws AccessDeniedException;

    /**
     * Add generic password for application
     *
     * @param serviceName Service
     * @param accountName Account
     * @param password    Password to save for service
     * @throws LocalAccessDeniedException Failure accessing keychain
     */
    void addPassword(String serviceName, String accountName, String password) throws AccessDeniedException;

    /**
     * Find internet password
     *
     * @param scheme   Protocol scheme
     * @param port     Port
     * @param hostname Hostname
     * @param user     Credentials  @return Password if found or null otherwise
     */
    String getPassword(Scheme scheme, int port, String hostname, String user) throws AccessDeniedException;

    /**
     * Save internet password
     *
     * @param scheme   Protocol scheme
     * @param port     Port
     * @param hostname Service name
     * @param user     Credentials
     * @param password Password to save for service
     * @throws LocalAccessDeniedException Failure accessing keychain
     */
    void addPassword(Scheme scheme, int port, String hostname, String user, String password) throws AccessDeniedException;

    /**
     * @param serviceName Service
     * @param user        Credentials
     * @throws LocalAccessDeniedException Failure accessing keychain
     */
    void deletePassword(String serviceName, String user) throws AccessDeniedException;

    /**
     * @param scheme   Protocol scheme
     * @param port     Port
     * @param hostname Service name
     * @param user     Credentials
     * @throws LocalAccessDeniedException Failure accessing keychain
     */
    void deletePassword(Scheme scheme, int port, String hostname, String user) throws AccessDeniedException;
}
