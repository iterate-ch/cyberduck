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

/**
 * Default OAuth Prefix Service. Will just return fields from passed bookmark.
 */
public class DefaultPasswordStorePrefixService implements PasswordStorePrefixService {

    private final Host bookmark;

    public DefaultPasswordStorePrefixService(final Host bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public String getPrefix() {
        if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            return String.format("%s (%s)", bookmark.getProtocol().getDescription(),
                    bookmark.getCredentials().getUsername());
        }
        return bookmark.getProtocol().getDescription();
    }

    @Override
    public String getHostname() {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            if(StringUtils.isNotBlank(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost())) {
                return URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost();
            }
        }
        return bookmark.getHostname();
    }

    @Override
    public String getIdentifier() {
        return bookmark.getProtocol().getIdentifier();
    }

    @Override
    public Integer getPort() {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            if(StringUtils.isNotBlank(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost())) {
                return URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getPort();
            }
        }
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
