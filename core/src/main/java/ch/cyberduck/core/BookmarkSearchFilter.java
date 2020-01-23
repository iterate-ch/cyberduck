package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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

import java.util.Locale;

public class BookmarkSearchFilter implements HostFilter {

    private final String searchString;

    public BookmarkSearchFilter(final String searchString) {
        this.searchString = searchString;
    }

    @Override
    public boolean accept(final Host bookmark) {
        final String[] elements = StringUtils.split(StringUtils.lowerCase(searchString, Locale.ROOT));
        for(String search : elements) {
            if(StringUtils.lowerCase(BookmarkNameProvider.toString(bookmark), Locale.ROOT).contains(search)) {
                return true;
            }
            if(null != bookmark.getCredentials().getUsername()) {
                if(StringUtils.lowerCase(bookmark.getCredentials().getUsername(), Locale.ROOT).contains(search)) {
                    return true;
                }
            }
            if(null != bookmark.getComment()) {
                if(StringUtils.lowerCase(bookmark.getComment(), Locale.ROOT).contains(search)) {
                    return true;
                }
            }
            if(StringUtils.lowerCase(bookmark.getHostname(), Locale.ROOT).contains(search)) {
                return true;
            }
            for(String label : bookmark.getLabels()) {
                if(StringUtils.lowerCase(label, Locale.ROOT).contains(search)) {
                    return true;
                }
            }
        }
        return false;
    }
}
