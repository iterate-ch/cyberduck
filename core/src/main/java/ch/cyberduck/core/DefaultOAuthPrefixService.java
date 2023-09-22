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

/**
 * Default OAuth Prefix Service. Will just return fields from passed bookmark.
 */
public class DefaultOAuthPrefixService implements OAuthPrefixService {
    private final Host bookmark;

    public DefaultOAuthPrefixService(Host bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public String getDescription() {
        if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            return String.format("%s (%s)", bookmark.getProtocol().getDescription(),
                    bookmark.getCredentials().getUsername());
        }
        return bookmark.getProtocol().getDescription();
    }

    @Override
    public String getHostname() {
        return bookmark.getHostname();
    }

    @Override
    public String getIdentifier() {
        return bookmark.getProtocol().getIdentifier();
    }

    @Override
    public Integer getNonDefaultPort() {
        if(!protocol.isPortConfigurable()) {
            return null;
        }
        final int port = this.getPort();
        if(port == protocol.getDefaultPort()) {
            return null;
        }
        return port;
    }

    @Override
    public int getPort() {
        return bookmark.getPort();
    }

    @Override
    public Scheme getScheme() {
        return bookmark.getProtocol().getScheme();
    }

    @Override
    public String getUsername() {
        return bookmark.getCredentials().getUsername();
    }
}
