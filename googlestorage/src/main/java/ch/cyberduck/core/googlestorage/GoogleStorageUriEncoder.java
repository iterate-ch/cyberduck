package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleStorageUriEncoder {

    private GoogleStorageUriEncoder() {
        //
    }

    public static String encode(final String p) {
        try {
            final StringBuilder b = new StringBuilder();
            if(StringUtils.startsWith(p, String.valueOf(Path.DELIMITER))) {
                b.append(Path.DELIMITER);
            }
            b.append(URLEncoder.encode(p, StandardCharsets.UTF_8.name()));
            return StringUtils.replaceEach(b.toString(),
                new String[]{"+", "*", "%7E", "%40"},
                new String[]{"%20", "%2A", "~", "@"});
        }
        catch(UnsupportedEncodingException e) {
            return p;
        }
    }
}
