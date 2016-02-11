package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

public class SearchFilterFactory {

    /**
     * No file filter.
     */
    public static final Filter<Path> NULL_FILTER = new NullFilter<Path>();

    /**
     * Filter hidden files.
     */
    public static final Filter<Path> HIDDEN_FILTER = new RegexFilter();

    public static Filter<Path> create(final String input, final boolean hidden) {
        if(StringUtils.isBlank(input)) {
            // Revert to the last used default filter
            return create(hidden);
        }
        else {
            // Setting up a custom filter for the directory listing
            return new SearchFilter(input);
        }
    }

    public static Filter<Path> create(final boolean hidden) {
        // Revert to the last used default filter
        if(hidden) {
            return NULL_FILTER;
        }
        else {
            return HIDDEN_FILTER;
        }
    }
}
