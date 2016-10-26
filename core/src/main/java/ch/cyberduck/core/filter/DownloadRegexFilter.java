package ch.cyberduck.core.filter;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.log4j.Logger;

import java.util.Objects;
import java.util.regex.Pattern;

public class DownloadRegexFilter extends DownloadDuplicateFilter {
    private static final Logger log = Logger.getLogger(DownloadRegexFilter.class);

    private final Pattern pattern;

    public DownloadRegexFilter() {
        this(Pattern.compile(PreferencesFactory.get().getProperty("queue.download.skip.regex")));
    }

    public DownloadRegexFilter(final Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean accept(final Path file) {
        if(!super.accept(file)) {
            return false;
        }
        if(pattern.matcher(file.getName()).matches()) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Skip %s excluded with regex", file.getAbsolute()));
            }
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DownloadRegexFilter{");
        sb.append("pattern=").append(pattern);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof DownloadRegexFilter)) {
            return false;
        }
        final DownloadRegexFilter that = (DownloadRegexFilter) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}
