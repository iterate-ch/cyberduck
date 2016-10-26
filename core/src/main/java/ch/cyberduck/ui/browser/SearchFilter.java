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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;
import java.util.Objects;

public class SearchFilter implements Filter<Path> {

    private final String input;

    public SearchFilter(final String input) {
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
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SearchFilter{");
        sb.append("input='").append(input).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof SearchFilter)) {
            return false;
        }
        final SearchFilter that = (SearchFilter) o;
        return Objects.equals(input, that.input);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input);
    }
}
