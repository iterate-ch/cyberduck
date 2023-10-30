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
 * 
 * <p>On Windows (Windows Credential Manager) the format is
 * <pre>${application.container.name}${:getIdentifier()}${:getOptionalPort()${?user=getUsername()}</pre>
 * For DefaultHostPasswordStore the format is
 * <pre>${getScheme()}://${getDescription()} $EntryName@${getHostname()}:${getPort()}</pre>
 * <p>GetDescription can return "Protocol Description", or "Protocol Description (${getUsername()})".</p>
 * <p>EntryName can be "OAuth2 Access Token", "OAuth2 Refresh Token", "OIDC Id Token", "OAuth2 Token Expiry"</p>
 * </p>
 */
public interface PasswordStorePrefixService {
    /**
     * Gets a description to insert as User-component into password store
     */
    String getPrefix(Host bookmark);

    /**
     * Returns the configured hostname used for authentication of this bookmark.
     */
    String getHostname(Host bookmark);

    /**
     * Retrieves a port configured with this bookmark.
     */
    Integer getPort(Host bookmark);

    /**
     * Returns Scheme for password store to save entry with
     */
    Scheme getScheme(Host bookmark);

    /**
     * Retrieves username information for this bookmark
     */
    String getUsername(Host bookmark);
}
