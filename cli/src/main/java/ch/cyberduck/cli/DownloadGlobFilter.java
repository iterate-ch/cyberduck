package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.filter.DownloadDuplicateFilter;
import ch.cyberduck.ui.browser.SearchFilter;

import java.util.regex.Pattern;

public class DownloadGlobFilter extends DownloadDuplicateFilter {

    private final SearchFilter proxy;

    public DownloadGlobFilter(final String input) {
        proxy = new SearchFilter(input);
    }

    @Override
    public boolean accept(final Path file) {
        if(!super.accept(file)) {
            return false;
        }
        return proxy.accept(file);
    }

    @Override
    public Pattern toPattern() {
        return proxy.toPattern();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DownloadGlobFilter{");
        sb.append("proxy=").append(proxy);
        sb.append('}');
        return sb.toString();
    }
}
