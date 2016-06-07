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
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Pattern;

public final class HostParser {
    private static final Logger log = Logger.getLogger(HostParser.class);

    private static final Preferences preferences = PreferencesFactory.get();

    /**
     * Default scheme if not in URI
     */
    private final Protocol scheme;

    private final ProtocolFactory factory;

    public HostParser() {
        this(ProtocolFactory.global);
    }

    public HostParser(final ProtocolFactory factory) {
        this.factory = factory;
        this.scheme = factory.find(
                preferences.getProperty("connection.protocol.default"));
    }

    public HostParser(final ProtocolFactory factory, final Protocol scheme) {
        this.factory = factory;
        this.scheme = scheme;
    }

    public Host get(final String url) {
        return HostParser.parse(factory, scheme, url);
    }

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param url URL
     * @return Bookmark
     */
    public static Host parse(final String url) {
        return parse(ProtocolFactory.global, ProtocolFactory.global.find(
                preferences.getProperty("connection.protocol.default")), url);
    }

    public static Host parse(final ProtocolFactory factory, final Protocol scheme, final String url) {
        final String input = url.trim();
        int begin = 0;
        int cut;
        Protocol protocol = null;
        if(input.indexOf("://", begin) != -1) {
            cut = input.indexOf("://", begin);
            protocol = factory.find(input.substring(begin, cut));
            begin += cut - begin + 3;
        }
        if(null == protocol) {
            protocol = scheme;
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
        String path = null;
        int port = protocol.getDefaultPort();
        // Handle IPv6
        if(input.indexOf('[', begin) != -1 && input.indexOf(']', begin) != -1) {
            if(input.indexOf(']', begin) > input.indexOf('[', begin)) {
                begin = input.indexOf('[', begin) + 1;
                cut = input.indexOf(']', begin);
                String address = input.substring(begin, cut);
                if(isv6Address(address)) {
                    hostname = address;
                    begin += hostname.length();
                }
            }
        }
        else if(input.indexOf(Path.DELIMITER, begin) != -1) {
            cut = input.indexOf(Path.DELIMITER, begin);
            String address = input.substring(begin, cut);
            if(isv6Address(address)) {
                hostname = address;
                begin += hostname.length();
            }
        }
        else {
            if(isv6Address(input)) {
                hostname = input;
                begin += hostname.length();
            }
        }
        if(StringUtils.isBlank(hostname)) {
            // Handle DNS name or IPv4
            if(StringUtils.isNotBlank(input)) {
                if(input.indexOf(':', begin) != -1
                        && (input.indexOf(Path.DELIMITER, begin) == -1 || input.indexOf(':', begin) < input.indexOf(Path.DELIMITER, begin))) {
                    cut = input.indexOf(':', begin);
                }
                else if(input.indexOf(Path.DELIMITER, begin) != -1) {
                    cut = input.indexOf(Path.DELIMITER, begin);
                }
                else {
                    cut = input.length();
                }
                hostname = input.substring(begin, cut);
                begin += hostname.length();
            }
        }
        if(input.indexOf(':', begin) != -1
                && (input.indexOf(Path.DELIMITER, begin) == -1 || input.indexOf(':', begin) < input.indexOf(Path.DELIMITER, begin))) {
            begin = input.indexOf(':', begin) + 1;
            String portString;
            if(input.indexOf(Path.DELIMITER, begin) != -1) {
                cut = input.indexOf(Path.DELIMITER, begin);
                portString = input.substring(begin, cut);
                try {
                    port = Integer.parseInt(portString);
                    begin += portString.length();
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid port number given");
                }
                try {
                    path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
                    begin += path.length();
                }
                catch(UnsupportedEncodingException | IllegalArgumentException e) {
                    log.error(e.getMessage(), e);
                }
            }
            else {
                portString = input.substring(begin, input.length());
                try {
                    port = Integer.parseInt(portString);
                    begin += portString.length();
                }
                catch(NumberFormatException e) {
                    log.warn("Invalid port number given");
                }
            }
        }
        if(input.indexOf(Path.DELIMITER, begin) != -1) {
            try {
                path = URLDecoder.decode(input.substring(begin, input.length()), "UTF-8");
            }
            catch(UnsupportedEncodingException | IllegalArgumentException e) {
                log.error(e.getMessage(), e);
            }
        }
        switch(protocol.getType()) {
            case s3:
            case googlestorage:
            case swift:
                if(StringUtils.isNotBlank(protocol.getDefaultHostname())) {
                    if(StringUtils.isNotBlank(hostname)) {
                        // Replace with static hostname and prefix path with bucket
                        if(StringUtils.isBlank(path)) {
                            path = PathNormalizer.normalize(hostname);
                        }
                        else {
                            path = PathNormalizer.normalize(hostname) + path;
                        }
                        hostname = protocol.getDefaultHostname();
                    }
                }
        }
        final Host host = new Host(protocol, hostname, port, path, new Credentials(username, password));
        host.configure();
        return host;
    }

    private static final Pattern IPV6_STD_PATTERN = Pattern.compile(
            "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"
    );

    private static boolean isv6Address(final String address) {
        if(IPV6_STD_PATTERN.matcher(address).matches()) {
            return true;
        }
        return InetAddressUtils.isIPv6Address(address);
    }
}
