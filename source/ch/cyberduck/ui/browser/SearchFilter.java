package ch.cyberduck.ui.browser;

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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * @version $Id$
 */
public class SearchFilter implements Filter<Path> {

    private String input;

    private Cache<Path> cache;

    public SearchFilter(Cache<Path> cache, String input) {
        this.cache = cache;
        this.input = input;
    }

    @Override
    public boolean accept(final Path file) {
        if(file.getName().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT))) {
            // Matching filename
            return true;
        }
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            if(file.attributes().getVersionId().toLowerCase(Locale.ROOT).contains(input.toLowerCase(Locale.ROOT))) {
                // Matching version
                return true;
            }
        }
        if(file.isDirectory()) {
            // #471. Expanded item children may match search string
            return cache.isCached(file);
        }
        return false;
    }
}
