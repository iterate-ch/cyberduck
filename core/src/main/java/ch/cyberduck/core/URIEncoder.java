package ch.cyberduck.core;

/*
 * Copyright (c) 2012 David Kocher. All rights reserved.
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

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;

/**
 * @version $Id$
 */
public final class URIEncoder {

    private URIEncoder() {
        //
    }

    /**
     * URL encode a path
     *
     * @param p Path
     * @return URI encoded
     * @see java.net.URLEncoder#encode(String, String)
     */
    public static String encode(final String p) {
        try {
            final StringBuilder b = new StringBuilder();
            final StringTokenizer t = new StringTokenizer(p, "/");
            if(!t.hasMoreTokens()) {
                return p;
            }
            if(StringUtils.startsWith(p, String.valueOf(Path.DELIMITER))) {
                b.append(Path.DELIMITER);
            }
            while(t.hasMoreTokens()) {
                b.append(URLEncoder.encode(t.nextToken(), "UTF-8"));
                if(t.hasMoreTokens()) {
                    b.append(Path.DELIMITER);
                }
            }
            if(StringUtils.endsWith(p, String.valueOf(Path.DELIMITER))) {
                b.append(Path.DELIMITER);
            }
            // Because URLEncoder uses <code>application/x-www-form-urlencoded</code> we have to replace these
            // for proper URI percented encoding.
            return b.toString().replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        }
        catch(UnsupportedEncodingException e) {
            return p;
        }
    }
}