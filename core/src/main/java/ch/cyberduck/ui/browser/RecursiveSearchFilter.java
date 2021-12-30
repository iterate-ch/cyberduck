package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecursiveSearchFilter extends NullFilter<Path> {
    private static final Logger log = LogManager.getLogger(RecursiveSearchFilter.class);

    /**
     * List of accepted files
     */
    private final AttributedList<Path> list;

    public RecursiveSearchFilter(final AttributedList<Path> list) {
        this.list = list;
    }

    @Override
    public boolean accept(final Path file) {
        if(list.find(new SimplePathPredicate(file)) != null) {
            return true;
        }
        for(Path f : list) {
            if(f.isChild(file)) {
                return true;
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Filter %s", file));
        }
        return false;
    }
}
