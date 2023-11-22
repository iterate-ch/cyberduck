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
 * Default implementation. Will just return fields from passed bookmark.
 */
public class DefaultPasswordStorePrefixService implements PasswordStorePrefixService {

    @Override
    public String getPrefix(final Host bookmark) {
        if(StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            return String.format("%s (%s)", bookmark.getProtocol().getDescription(), bookmark.getCredentials().getUsername());
        }
        return bookmark.getProtocol().getDescription();
    }

    /**
     * @return Hostname from OAuth token URL when available instead of target hostname
     */
    @Override
    public String getHostname(final Host bookmark) {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            if(StringUtils.isNotBlank(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost())) {
                return URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost();
            }
        }
        return bookmark.getHostname();
    }

    /**
     * @return Port from OAuth token URL when available instead of target hostname
     */
    @Override
    public Integer getPort(final Host bookmark) {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            if(-1 != URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getPort()) {
                return URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getPort();
            }
            return Scheme.valueOf(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getScheme()).getPort();
        }
        return bookmark.getPort();
    }

    @Override
    public Scheme getScheme(final Host bookmark) {
        if(bookmark.getCredentials().isOAuthAuthentication()) {
            if(StringUtils.isNotBlank(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getHost())) {
                return Scheme.valueOf(URI.create(bookmark.getProtocol().getOAuthTokenUrl()).getScheme());
            }
        }
        return bookmark.getProtocol().getScheme();
    }

    @Override
    public String getUsername(final Host bookmark) {
        return bookmark.getCredentials().getUsername();
    }
}
