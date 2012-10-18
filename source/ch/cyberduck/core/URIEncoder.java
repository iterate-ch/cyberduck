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
     * @param p Absolute path
     * @return URI encoded
     * @see java.net.URLEncoder#encode(String, String)
     */
    public static String encode(final String p) {
        try {
            StringBuilder b = new StringBuilder();
            StringTokenizer t = new StringTokenizer(p, "/");
            if(!t.hasMoreTokens()) {
                return p;
            }
            while(t.hasMoreTokens()) {
                b.append(Path.DELIMITER).append(URLEncoder.encode(t.nextToken(), "UTF-8"));
            }
            // Becuase URLEncoder uses <code>application/x-www-form-urlencoded</code> we have to replace these
            // for proper URI percented encoding.
            return b.toString().replace("+", "%20").replace("*", "%2A").replace("%7E", "~");
        }
        catch(UnsupportedEncodingException e) {
            return p;
        }
    }
}