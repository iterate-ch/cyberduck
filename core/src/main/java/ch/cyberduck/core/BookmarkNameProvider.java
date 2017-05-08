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
            if(StringUtils.equals(bookmark.getHostname(), bookmark.getProtocol().getDefaultHostname())) {
                return bookmark.getProtocol().getName();
            }
            if(StringUtils.isBlank(bookmark.getHostname())) {
                return bookmark.getProtocol().getName();
            }
            if(username) {
                if(!bookmark.getCredentials().isAnonymousLogin()
                        && StringUtils.isNotBlank(bookmark.getCredentials().getUsername())) {
                    return String.format("%s@%s \u2013 %s", bookmark.getCredentials().getUsername(),
                            StringUtils.strip(bookmark.getHostname()), bookmark.getProtocol().getName());
                }
            }
            return String.format("%s \u2013 %s", StringUtils.strip(bookmark.getHostname()), bookmark.getProtocol().getName());
        }
        return bookmark.getNickname();
    }
}
