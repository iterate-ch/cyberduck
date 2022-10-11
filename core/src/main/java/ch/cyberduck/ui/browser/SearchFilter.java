package ch.cyberduck.ui.browser;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Filter;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class SearchFilter implements Filter<Path> {
    private static final Logger log = LogManager.getLogger(SearchFilter.class);

    private final String input;

    /**
     * @param input Glob search pattern supporting wildcards * and ?
     */
    public SearchFilter(final String input) {
        this.input = input;
    }

    @Override
    public boolean accept(final Path file) {
        if(StringUtils.containsIgnoreCase(file.getName(), input)) {
            // Matching in filename
            return true;
        }
        final Pattern pattern = this.toPattern();
        if(pattern.matcher(file.getName()).matches()) {
            // Matching pattern
            return true;
        }
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            if(StringUtils.containsIgnoreCase(file.attributes().getVersionId(), input)) {
                // Matching version
                return true;
            }
        }
        if(log.isDebugEnabled()) {
            log.debug(String.format("Skip %s not matching %s", file.getAbsolute(), input));
        }
        return false;
    }

    @Override
    public String toString() {
        return input;
    }

    /**
     * Compile glob to regular expression
     */
    @Override
    public Pattern toPattern() {
        final StringBuilder pattern = new StringBuilder();
        pattern.append("\\A");
        for(int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);
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
        try {
            return Pattern.compile(pattern.toString());
        }
        catch(PatternSyntaxException e) {
            log.warn(String.format("Failure %s compiling pattern %s", e, pattern));
            return Pattern.compile(Pattern.quote(input));
        }
    }

}
