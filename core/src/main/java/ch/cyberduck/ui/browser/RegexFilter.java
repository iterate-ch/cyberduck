package ch.cyberduck.ui.browser;

/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.util.Objects;
import java.util.regex.Pattern;

public class RegexFilter implements Filter<Path> {

    private Pattern pattern = Pattern.compile(
            PreferencesFactory.get().getProperty("browser.hidden.regex"));

    @Override
    public boolean accept(final Path file) {
        if(null == pattern) {
            return true;
        }
        if(pattern.matcher(file.getName()).matches()) {
            return false;
        }
        return !file.attributes().isDuplicate();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegexFilter{");
        sb.append("pattern=").append(pattern);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof RegexFilter)) {
            return false;
        }
        final RegexFilter that = (RegexFilter) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}
