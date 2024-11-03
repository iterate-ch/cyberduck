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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

public final class URIEncoder {
    private static final Logger log = LogManager.getLogger(URIEncoder.class);

    private URIEncoder() {
        //
    }

    /**
     * URL encode a path
     *
     * @param input Path
     * @return URI encoded
     * @see java.net.URLEncoder#encode(String, String)
     */
    public static String encode(final String input) {
        try {
            final StringBuilder b = new StringBuilder();
            final StringTokenizer t = new StringTokenizer(input, "/");
            if(!t.hasMoreTokens()) {
                return input;
            }
            if(StringUtils.startsWith(input, String.valueOf(Path.DELIMITER))) {
                b.append(Path.DELIMITER);
            }
            while(t.hasMoreTokens()) {
                b.append(URLEncoder.encode(t.nextToken(), StandardCharsets.UTF_8.name()));
                if(t.hasMoreTokens()) {
                    b.append(Path.DELIMITER);
                }
            }
            if(StringUtils.endsWith(input, String.valueOf(Path.DELIMITER))) {
                b.append(Path.DELIMITER);
            }
            // Because URLEncoder uses <code>application/x-www-form-urlencoded</code> we have to replace these
            // for proper URI percented encoding.
            return StringUtils.replaceEach(b.toString(),
                new String[]{"+", "*", "%7E", "%40"},
                new String[]{"%20", "%2A", "~", "@"});
        }
        catch(UnsupportedEncodingException e) {
            log.warn("Failure {} encoding input {}", e, input);
            return input;
        }
    }

    public static String decode(final String input) {
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8.name());
        }
        catch(UnsupportedEncodingException | IllegalArgumentException e) {
            log.warn("Failure {} decoding input {}", e, input);
            return input;
        }
    }
}
