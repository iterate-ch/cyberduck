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

import org.apache.commons.lang3.StringUtils;

import java.net.URI;

public class CredentialManagerPasswordStorePrefixService extends DefaultPasswordStorePrefixService {

    @Override
    public String getPrefix(final Host bookmark) {
        return bookmark.getProtocol().getIdentifier();
    }

    @Override
    public String getHostname(final Host bookmark) {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            // duck:identifier?user=
            return null;
        }
        if(bookmark.getProtocol().isHostnameConfigurable()) {
            if(bookmark.getProtocol().getDefaultHostname().equals(bookmark.getHostname())) {
                return null;
            }
            // duck:identifier:fqdn?user=
            return bookmark.getHostname();
        }
        // duck:identifier?user=
        return null;
    }

    @Override
    public Integer getPort(final Host bookmark) {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            if(StringUtils.isNotBlank(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost())) {
                return URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getPort();
            }
        }
        if(bookmark.getProtocol().isPortConfigurable()) {
            if(bookmark.getProtocol().getDefaultPort() == bookmark.getPort()) {
                return null;
            }
            // duck:identifier:fqdn:port?user=
            return bookmark.getPort();
        }
        return null;
    }
}
