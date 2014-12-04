package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * @version $Id$
 */
public final class HostParser {
    private static final Logger log = Logger.getLogger(HostParser.class);

    private HostParser() {
        //
    }

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param url URL
     * @return Bookmark
     */
    public static Host parse(final String url) {
        final String input = url.trim();
        int begin = 0;
        int cut;
        Protocol protocol = null;
        if(input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = ProtocolFactory.forScheme(input.substring(begin, cut));
            if(null == protocol) {
                protocol = ProtocolFactory.forName(input.substring(begin, cut));
            }
            if(null != protocol) {
                begin += cut - begin + 3;
            }
        }
        final Preferences preferences = PreferencesFactory.get();
        if(null == protocol) {
            protocol = ProtocolFactory.forName(
                    preferences.getProperty("connection.protocol.default"));
        }
        String username;
        String password = null;
        if(protocol.isAnonymousConfigurable()) {
            username = preferences.getProperty("connection.login.anon.name");
        }
        else {
            username = preferences.getProperty("connection.login.name");
        }
        if(input.indexOf('@', begin) != -1) {
            if(-1 == input.indexOf(Path.DELIMITER, begin)
                    || input.indexOf('@', begin) < input.indexOf(Path.DELIMITER, begin)) {
                cut = input.indexOf('@', begin);
                // Handle at sign in username
                while(cut < input.lastIndexOf('@')) {
                    if(input.indexOf(Path.DELIMITER, begin) != -1
                            && input.indexOf('@', cut + 1) > input.indexOf(Path.DELIMITER, begin)) {
                        // At sign is part of the path
                        break;
                    }
                    cut = input.indexOf('@', cut + 1);
                }
                if(input.indexOf(':', begin) != -1
                        && cut > input.indexOf(':', begin)) {
                    // ':' is not for the port number but username:pass separator
                    username = input.substring(begin, input.indexOf(':', begin));
                    begin += username.length() + 1;
                    cut = input.indexOf('@', begin);
                    password = input.substring(begin, cut);
                    begin += password.length() + 1;
                }
                else {
                    // No password given
                    username = input.substring(begin, cut);
                    begin += username.length() + 1;
                }
            }
        }
        String hostname = preferences.getProperty("connection.hostname.default");
        if(StringUtils.isNotBlank(input)) {
            hostname = input.substring(begin, input.length());
        }
        String path = null;
        int port = protocol.getDefaultPort();
        if(input.indexOf(':', begin) != -1) {
            cut = input.indexOf(':', begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length() + 1;
            try {
                String portString;
                if(input.indexOf(Path.DELIMITER, begin) != -1) {
                    portString = input.substring(begin, input.indexOf(Path.DELIMITER, begin));
                    begin += portString.length();
                    try {
                        path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
                    }
                    catch(UnsupportedEncodingException e) {
                        log.error(e.getMessage(), e);
                    }
                }
                else {
                    portString = input.substring(begin, input.length());
                }
                port = Integer.parseInt(portString);
            }
            catch(NumberFormatException e) {
                log.warn("Invalid port number given");
            }
        }
        else if(input.indexOf(Path.DELIMITER, begin) != -1) {
            cut = input.indexOf(Path.DELIMITER, begin);
            hostname = input.substring(begin, cut);
            begin += hostname.length();
            try {
                path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
            }
            catch(UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }
        final Host h = new Host(protocol, hostname, port, path, new Credentials(username, password));
        h.configure();
        return h;
    }
}
