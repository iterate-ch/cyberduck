package ch.cyberduck.core.proxy;

/*
 * Copyright (c) 2002-2009 David Kocher. All rights reserved.
 *
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

import org.apache.log4j.Logger;

import java.util.regex.PatternSyntaxException;

public abstract class AbstractProxyFinder implements ProxyFinder {
    private static final Logger log = Logger.getLogger(AbstractProxyFinder.class);

    /**
     * @param wildcard Pattern
     * @param hostname Server
     * @return True if hostname matches wildcard
     */
    protected boolean matches(final String wildcard, final String hostname) {
        final String host = wildcard.replace("*", ".*").replace("?", ".");
        final String regex = String.format("^%s$", host);
        try {
            return hostname.matches(regex);
        }
        catch(PatternSyntaxException e) {
            log.warn("Failed converting wildcard to regular expression:" + e.getMessage());
        }
        return false;
    }
}