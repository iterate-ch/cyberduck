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

import org.apache.commons.lang3.StringUtils;

public final class BookmarkNameProvider {

    private BookmarkNameProvider() {
        //
    }

    public static String toString(final Host bookmark) {
        return toString(bookmark, false);
    }

    public static String toString(final Host bookmark, final boolean username) {
        if(StringUtils.isEmpty(bookmark.getNickname())) {
            if(StringUtils.isNotBlank(bookmark.getProtocol().getDefaultNickname())) {
                return bookmark.getProtocol().getDefaultNickname();
            }
            // Return default bookmark name
            final String hostname = toHostname(bookmark, username);
            if(StringUtils.isBlank(hostname)) {
                return bookmark.getProtocol().getName();
            }
            return hostname + StringUtils.SPACE + '\u2013' + StringUtils.SPACE + bookmark.getProtocol().getName();
        }
        // Return custom bookmark name set
        return bookmark.getNickname();
    }

    public static String toProtocol(final Host bookmark) {
        if(StringUtils.isEmpty(bookmark.getNickname())) {
            if(StringUtils.isNotBlank(bookmark.getProtocol().getDefaultNickname())) {
                return bookmark.getProtocol().getDefaultNickname();
            }
            return bookmark.getProtocol().getName();
        }
        // Return custom bookmark name set
        return bookmark.getNickname();
    }

    public static String toHostname(final Host bookmark) {
        return toHostname(bookmark, false);
    }

    public static String toHostname(final Host bookmark, final boolean username) {
        final StringBuilder prefix = new StringBuilder();
        if(username && !bookmark.getCredentials().isAnonymousLogin() && StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
            prefix.append(String.format("%s@", bookmark.getCredentials().getUsername()));
        }
        if(StringUtils.isNotBlank(bookmark.getHostname())) {
            prefix.append(StringUtils.strip(bookmark.getHostname()));
        }
        else if(StringUtils.isNotBlank(bookmark.getProtocol().getDefaultHostname())) {
            prefix.append(StringUtils.strip(bookmark.getProtocol().getDefaultHostname()));
        }
        return prefix.toString();
    }
}
