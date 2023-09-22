package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

/**
 * Opaque service to configure OAuth password store entries
 */
public interface OAuthPrefixService {
    /**
     * Gets a description to insert as User-component into PasswordStore
     */
    String getDescription();

    /**
     * Returns the OAuth configured hostname of this bookmark.
     */
    String getHostname();

    /**
     * Retrieves an identifier, used to identify Credential Manager entries on
     * Windows (i.e. onedrive, sharepoint, dropbox, etc.)
     */
    String getIdentifier();

    /**
     * Some PasswordStores don't need to store all ports, thus allow for nullable
     * return value.
     * 
     * @return If default port, returns {@code null}, otherwise returns
     *         {@link #getPort()}.
     */
    Integer getNonDefaultPort();

    /**
     * Retrieves a port configured with this OAuth bookmark.
     */
    int getPort();

    /**
     * Returns Scheme for PasswordStore to save entry with
     */
    Scheme getScheme();

    /**
     * Retrieves username information for this OAuth bookmark
     */
    String getUsername();
}
