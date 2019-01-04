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

import java.util.ArrayDeque;
import java.util.Deque;
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
        this(ProtocolFactory.get());
    }

    public HostParser(final ProtocolFactory factory) {
        this.factory = factory;
        this.scheme = factory.forName(preferences.getProperty("connection.protocol.default"));
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
        return parse(ProtocolFactory.get(), ProtocolFactory.get().forName(
            preferences.getProperty("connection.protocol.default")), url);
    }

    public static Host parse(final ProtocolFactory factory, final Protocol scheme, final String url) {
        final StringReader reader = new StringReader(url);

        Value<String> schemeValue = new Value<>();
        if(!parseScheme(reader, schemeValue)) {
            // TODO: Error out. Scheme was invalid.
        }

        Protocol protocol = factory.forName(schemeValue.getValue());
        if(null == protocol) {
            protocol = scheme;
        }

        final Host host = new Host(protocol);

        final URITypes uriType = resolveURIType(findURIType(reader), protocol);
        if(uriType == URITypes.Undefined) {
            // scheme:
            if(StringUtils.isBlank(protocol.getDefaultHostname())) {
                // TODO: Error out. This is not supported.
            }
            if(StringUtils.isBlank(protocol.getDefaultPath())) {
                // TODO: Error out.This is not supported.
            }

            return host;
        }

        if(uriType == URITypes.Authority) {
            if(!parseAuthority(reader, host)) {
                // TODO: Error out.
            }
            if(!parseAbsolute(reader, host)) {
                // TODO: Error out.
            }
        }
        else if(uriType == URITypes.Absolute) {
            if(!parseAbsolute(reader, host)) {
                // TODO: Error out.
            }
        }
        else if(uriType == URITypes.Rootless) {
            if(!parseRootless(reader, host)) {
                // TODO: Error out.
            }
        }
        else {
            // TODO Error. This should not happen.
        }

        return host;
    }

    static final String URI_SCHEME = "+-.";
    static final String URI_UNRESERVED = "-._~";
    static final String URI_SUBDELIMS = "!$&'()*+,;=";
    static final String URI_PCHAR = ":@";

    static boolean parseScheme(final StringReader reader, final Value<String> scheme) {
        final StringBuilder stringBuilder = new StringBuilder();
        while(!reader.endOfString()) {
            final char c = (char) reader.read();
            if(Character.isAlphabetic(c)
                || Character.isDigit(c)
                || URI_SCHEME.indexOf(c) != -1) {
                stringBuilder.append(c);
            }
            else if(Character.isWhitespace(c)) {
                if(stringBuilder.length() != 0) {
                    // Whitespace inside scheme. Break.
                    // TODO: Error out.
                    return false; // invalid. Return empty.
                }
            }
            else if(c == ':') {
                scheme.setValue(stringBuilder.toString());
                return true; // valid. Break to return stringbuilder
            }
        }
        // TODO: EOF, should this be considered safe? ("scheme" instead of "scheme:")
        return false;
    }

    static boolean parseAuthority(final StringReader reader, final Host host) {

        final StringBuilder userBuilder = new StringBuilder();
        final StringBuilder passwordBuilder = new StringBuilder();
        final StringBuilder authorityBuilder = new StringBuilder();
        final StringBuilder portBuilder = new StringBuilder();

        String userComponent = StringUtils.EMPTY;
        String passwordComponent = StringUtils.EMPTY;
        String authorityComponent = StringUtils.EMPTY;
        String portComponent = StringUtils.EMPTY;

        boolean validUser = true;
        boolean validAuthority = true;
        boolean validPort = true;

        boolean closeUser = false;
        boolean closePassword = false;

        while(!reader.endOfString()) { // find User and Authority.
            final char c = (char) reader.read();

            if(':' == c) {
                // If : encountered, restart builder and concat authority

                if(validUser) {
                    if(closeUser) {
                        if(!closePassword) {
                            passwordBuilder.append(c);
                        }
                    }
                    else {
                        closeUser = true;
                    }
                }

                portBuilder.setLength(0);

                validPort = true; // overwrites Port state
            }
            else if('@' == c) {
                // there must not be any at-sign after user-info
                if(!validUser) {
                    return false;
                }
                // (move anything from Authority to User)
                // Finish User Information
                // Authority may include username … builder includes Password.

                validUser = false;
                closeUser = true;
                closePassword = true;

                authorityBuilder.setLength(0); // reset authority
                portBuilder.setLength(0); // reset port
            }
            else if('/' == c) {
                // finish this thing up.

                if(!closeUser) {
                    userBuilder.setLength(0);
                }
                if(!closePassword) {
                    passwordBuilder.setLength(0);
                }
                host.setHostname(authorityBuilder.toString());
                if(validPort) {
                    host.setPort(Integer.parseInt(portBuilder.toString()));
                }

                return true;
            }
            else if('%' == c) {
                validPort = false;

                final char percented = readPercentCharacter(reader);
                if(!closeUser) {
                    userBuilder.append(percented);
                }
                if(!closePassword) {
                    passwordBuilder.append(percented);
                }
                authorityBuilder.append(percented);
                portBuilder.setLength(0);
            }
            else {
                if(!Character.isDigit(c)) {
                    validPort = false;
                }
                if(!closeUser) {
                    userBuilder.append(c);
                }
                if(!closePassword) {
                    passwordBuilder.append(c);
                }
            }
        }

        return false;
    }

    static boolean parseAbsolute(final StringReader reader, final Host host) {
        final StringBuilder pathBuilder = new StringBuilder();
        while(!reader.endOfString()) {
            final char c = (char) reader.read();
            if(isPChar(c) || c == '/') {
                pathBuilder.append(c);
            }
            else if(c == '%') {
                pathBuilder.append(readPercentCharacter(reader));
            }
            else {
                // TODO: Error out. This is not supported.
                return false;
            }
        }
        host.setDefaultPath(pathBuilder.toString());
        return true;
    }

    static boolean parseRootless(final StringReader reader, final Host host) {
        // This is not RFC-compliant.
        // * Rootless-path must not include authentication information.

        String userComponent = StringUtils.EMPTY;
        String passwordComponent = StringUtils.EMPTY;
        String pathComponent = StringUtils.EMPTY;

        boolean validUser = true;
        boolean continuePassword = false;

        final StringBuilder stringBuilder = new StringBuilder();
        while(reader.peek() != -1) {
            final char c = (char) reader.read();
            if(c == '/') {
                // TODO: Starts new segment.
                validUser = false;
                stringBuilder.append(c);
            }
            else if(isPChar(c)) {
                if(!isValidUserInfo(c)) {
                    if(validUser && c == '@') {
                        if(continuePassword) {
                            passwordComponent = stringBuilder.toString();
                        }
                        else {
                            userComponent = stringBuilder.toString();
                        }
                        stringBuilder.setLength(0);
                    }
                    else {
                        stringBuilder.append(c);
                    }
                    validUser = false;
                }
                else if(c == ':') {
                    if(validUser) {
                        if(continuePassword) {
                            // TODO: Error out.
                            //  This is not supported (two ":" in UserInfo)
                        }
                        userComponent = stringBuilder.toString();
                        stringBuilder.setLength(0);
                        continuePassword = true;
                    }
                    else {
                        stringBuilder.append(c);
                    }
                }
                else {
                    stringBuilder.append(c);
                }
            }
            else if(c == '%') {
                stringBuilder.append(readPercentCharacter(reader));
            }
            else if(Character.isWhitespace(c)) {
                // TODO We do allow whitespace in path and user component!
                //  this is a violation to RFC 3986.
                stringBuilder.append(c);
            }
            else {
                // TODO This is not supported!
                //  Error out.
                break;
            }
        }
        pathComponent = stringBuilder.toString();

        return true;
    }

    static URITypes findURIType(final StringReader reader) {
        final StringReader copy = reader.copy();
        if(copy.peek() != -1) {
            char c = (char) copy.read();
            if(c == '/') {
                if(copy.peek() != -1) {
                    c = (char) copy.read();
                    if(c == '/') {
                        reader.skip(2);
                        return URITypes.Authority;
                    }
                    else {
                        reader.skip(1);
                        return URITypes.Absolute;
                    }
                }
            }
            else {
                return URITypes.Rootless;
            }
        }
        return URITypes.Undefined;
    }

    static URITypes resolveURIType(final URITypes from, final Protocol protocol) {
        if(!protocol.isHostnameConfigurable()) {
            if(URITypes.Authority == from) {
                return URITypes.Absolute;
            }
        }
        return from;
    }

    private static boolean isUnreservedCharacter(final char c) {
        return Character.isAlphabetic(c) || Character.isDigit(c) || URI_UNRESERVED.indexOf(c) != -1;
    }

    private static boolean isSubDelimsCharacter(final char c) {
        return URI_SUBDELIMS.indexOf(c) != -1;
    }

    private static boolean isPChar(final char c) {
        return isUnreservedCharacter(c) || isSubDelimsCharacter(c) || URI_PCHAR.indexOf(c) != -1;
    }

    private static boolean isValidUserInfo(final char c) {
        return isUnreservedCharacter(c) || isSubDelimsCharacter(c) || c == ':';
    }

    private static char readPercentCharacter(final StringReader reader) {
        final StringBuilder string = new StringBuilder();
        for(int i = 0; i < 2 && reader.peek() != -1; i++) {
            string.append((char) reader.read());
        }
        if(string.length() != 2) {
            return Character.MIN_VALUE;
        }
        return (char) Integer.parseUnsignedInt(string.toString(), 16);
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

    final static class StringReader {
        private final int eof;
        private final String text;
        private int position = 0;

        public StringReader(final String text) {
            this.text = text;
            eof = this.text.length();
        }

        private StringReader(final StringReader reader) {
            this.text = reader.text;
            this.position = reader.position;
            this.eof = reader.eof;
        }

        public boolean endOfString() {
            return position >= eof;
        }

        public int read() {
            if(position >= eof) {
                return -1;
            }
            char c = text.charAt(position);
            position++;
            return c;
        }

        public int peek() {
            if(position >= eof) {
                return -1;
            }
            return text.charAt(position);
        }

        public void skip(final int chars) {
            position += chars;
            if(chars > 0 && position > eof) {
                position = eof;
            }
            if(chars < 0 && position < 0) {
                position = 0;
            }
        }

        public StringReader copy() {
            return new StringReader(this);
        }
    }

    final static class Value<T> {
        private T value;

        public T getValue() {
            return value;
        }

        public T setValue(final T value) {
            this.value = value;
            return value;
        }
    }

    enum URITypes {
        Authority,
        Absolute,
        Rootless,
        Undefined
    }

    final static class URIComponent {
        private final String value;
        private final URIComponents uriComponent;

        URIComponent(final String value, final URIComponents uriComponent) {
            this.value = value;
            this.uriComponent = uriComponent;
        }

        public URIComponents getUriComponent() {
            return uriComponent;
        }

        public String getValue() {
            return value;
        }
    }

    enum URIComponents {
        Scheme,
        UserInformation,
        PasswordInformation,
        Target,
        Port,
        Path
    }
}
