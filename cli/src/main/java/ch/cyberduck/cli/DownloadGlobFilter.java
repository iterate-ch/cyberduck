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

import org.apache.log4j.Logger;

import java.util.Objects;
import java.util.regex.Pattern;

public class DownloadGlobFilter extends DownloadDuplicateFilter {
    private static final Logger log = Logger.getLogger(DownloadGlobFilter.class);

    private final Pattern pattern;

    public DownloadGlobFilter(final String glob) {
        this.pattern = this.compile(glob);
    }

    /**
     * Compile glob to regular expression
     *
     * @param glob Glob pattern of files to include
     * @return Regular expression that excludes all other files
     */
    protected Pattern compile(final String glob) {
        final StringBuilder pattern = new StringBuilder();
        pattern.append("\\A");
        for(int i = 0; i < glob.length(); i++) {
            char ch = glob.charAt(i);
            if(ch == '?') {
                pattern.append('.');
            }
            else if(ch == '*') {
                pattern.append(".*");
            }
            else if("\\[]^.-$+(){}|".indexOf(ch) != -1) {
                pattern.append('\\');
                pattern.append(ch);
            }
            else {
                pattern.append(ch);
            }
        }
        pattern.append("\\z");
        return Pattern.compile(pattern.toString());
    }

    @Override
    public boolean accept(final Path file) {
        if(!super.accept(file)) {
            return false;
        }
        if(pattern.matcher(file.getName()).matches()) {
            return true;
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Skip %s excluded with regex", file.getAbsolute()));
        }
        return false;
    }

    @Override
    public Pattern toPattern() {
        return pattern;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DownloadGlobFilter{");
        sb.append("pattern=").append(pattern);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(!(o instanceof DownloadGlobFilter)) {
            return false;
        }
        final DownloadGlobFilter that = (DownloadGlobFilter) o;
        return Objects.equals(pattern, that.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}
