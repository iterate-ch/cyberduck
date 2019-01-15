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
import java.util.EnumSet;
import java.util.Iterator;
import java.util.PrimitiveIterator;
import java.util.regex.Pattern;

public final class HostParser {
    private static final Logger log = Logger.getLogger(HostParser.class);

    private static final Preferences preferences = PreferencesFactory.get();

    /**
     * Default scheme if not in URI
     */
    private final Protocol defaultScheme;

    private final ProtocolFactory factory;

    public HostParser() {
        this(ProtocolFactory.get());
    }

    public HostParser(final ProtocolFactory factory) {
        this.factory = factory;
        this.defaultScheme = factory.forName(preferences.getProperty("connection.protocol.default"));
    }

    public HostParser(final ProtocolFactory factory, final Protocol defaultScheme) {
        this.factory = factory;
        this.defaultScheme = defaultScheme;
    }

    public Host get(final String url) {
        final Host parsed = HostParser.parse(factory, defaultScheme, url);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Parsed %s as %s", url, parsed));
        }
        return parsed;
    }

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param url URL
     * @return Bookmark
     */
    public static Host parse(final String url) {
        final Host parsed = parse(ProtocolFactory.get(), ProtocolFactory.get().forName(
            preferences.getProperty("connection.protocol.default")), url);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Parsed %s as %s", url, parsed));
        }
        return parsed;
    }

    public static Host parse(final ProtocolFactory factory, final Protocol defaultScheme, final String url) {
        final StringReader reader = new StringReader(url);

        Value<String> schemeValue = new Value<>();
        Protocol protocol;
        if(!parseScheme(reader, schemeValue) || null == schemeValue.getValue()) {
            protocol = defaultScheme;
        }
        else {
            protocol = factory.forName(schemeValue.getValue());
            if(null == protocol) {
                protocol = defaultScheme;
            }
        }

        final Host host = new Host(protocol);

        final URITypes uriType = findURIType(reader);
        if(uriType == URITypes.Undefined) {
            // scheme:
            if(StringUtils.isBlank(protocol.getDefaultHostname())) {
                // TODO: Error out. This is not supported.
            }

            return host;
        }

        if(uriType == URITypes.Authority || uriType == URITypes.Rootless) {
            final Boolean userInfoResult = parseUserInfo(reader, host);
            if(Boolean.FALSE.equals(userInfoResult)) {
                // TODO: Error out.
            }

            if(uriType == URITypes.Authority && !host.getProtocol().isHostnameConfigurable()) {
                if(userInfoResult == null) {
                    reader.skip(-1);
                }
            }
            else {
                if(!parseHostname(reader, host)) {
                    // TODO: Error out.
                }
            }
            if(!parsePath(reader, host)) {
                // TODO: Error out.
            }
        }
        else if(uriType == URITypes.Absolute) {
            if(!parseAbsolute(reader, host)) {
                // TODO: Error out.
            }
        }
        else {
            // TODO: Error out.
        }

        return host;
    }

    static final String URI_SCHEME = "+-.";
    static final String URI_UNRESERVED = "-._~";
    static final String URI_SUBDELIMS = "!$&'()*+,;=";
    static final String URI_PCHAR = ":@";

    static boolean parseScheme(final StringReader reader, final Value<String> scheme) {
        final StringBuilder stringBuilder = new StringBuilder();
        int tracker = reader.position;
        while(!reader.endOfString()) {
            final char c = (char) reader.read();
            if(Character.isAlphabetic(c)
                || Character.isDigit(c)
                || URI_SCHEME.indexOf(c) != -1) {
                stringBuilder.append(c);
            }
            else if(c == ':') {
                tracker = reader.position;
                break;
            }
            else {
                if(c == ' ' && stringBuilder.length() == 0) {
                    continue;
                }
                // Invalid character inside scheme.
                reader.skip(tracker - reader.position);
                return false;
            }
        }
        reader.skip(tracker - reader.position);
        scheme.setValue(stringBuilder.toString());
        return true; // valid. Break to return stringbuilder
    }

    static boolean parseAuthority(final StringReader reader, final Host host) {
        if(Boolean.FALSE.equals(parseUserInfo(reader, host))) {
            return false;
        }
        return parseHostname(reader, host);
    }

    static boolean parseHostname(final StringReader reader, final Host host) {
        final StringBuilder buffer = new StringBuilder();

        boolean isPort = false;
        boolean bracketFlag = false;
        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if('/' == c) {
                reader.skip(-1);
                break;
            }
            else if('%' == c) {
                if(bracketFlag) {
                    buffer.append(c);
                }
                else {
                    buffer.append(readPercentCharacter(reader));
                }
            }
            else if(':' == c) {
                if(bracketFlag) {
                    buffer.append(c);
                }
                else {
                    isPort = true;
                    break;
                }
            }
            else {
                if(c == '[' && !bracketFlag) {
                    bracketFlag = true;
                }
                else if(c == ']' && bracketFlag) {
                    bracketFlag = false;
                }
                else {
                    buffer.append(c);
                }
            }
        }

        if(bracketFlag) {
            return false;
        }

        if(host.getProtocol().isHostnameConfigurable()) {
            if(buffer.length() == 0) {
                return false;
            }
            host.setHostname(buffer.toString());
        }

        if(isPort) {
            return parsePort(reader, host);
        }

        return true;
    }

    static boolean parsePort(final StringReader reader, final Host host) {
        int port = 0;
        int tracker = reader.position;
        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if(Character.isDigit(c)) {
                port = port * 10 + Character.getNumericValue(c);
            }
            else if(c == '/') {
                // Move Reader one symbol back
                // used in parseAbsolute (requires "/" at beginning)
                reader.skip(-1);
                break;
            }
            else {
                reader.skip(tracker - reader.position);
                return false;
            }
        }

        if(port <= 0 || port >= 65536) {
            return false;
        }

        if(host.getProtocol().isPortConfigurable()) {
            host.setPort(port);
        }

        return true;
    }

    static boolean parseAbsolute(final StringReader reader, final Host host) {
        return parsePath(reader, host);
    }

    static boolean parseRootless(final StringReader reader, final Host host) {
        // This is not RFC-compliant.
        // * Rootless-path must not include authentication information.

        if(Boolean.FALSE.equals(parseUserInfo(reader, host))) {
            return false;
        }
        return parsePath(reader, host);
    }

    static Boolean parseUserInfo(final StringReader reader, final Host host) {
        int tracker = reader.position;
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder userBuilder = new StringBuilder();
        StringBuilder passwordBuilder = null;

        boolean atSignFlag = false;
        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if('@' == c) {
                atSignFlag = true;
                final int length = buffer.length();
                for(int i = 0; i < length; i++) {
                    final char t = buffer.charAt(i);
                    if(t == ' ') {
                        return false;
                    }
                    if(t == ':' && passwordBuilder == null) {
                        passwordBuilder = new StringBuilder();
                        continue;
                    }
                    if(passwordBuilder != null) {
                        passwordBuilder.append(t);
                    }
                    else {
                        userBuilder.append(t);
                    }
                }
                tracker = reader.position;
                buffer.setLength(0);
                // found @-sign.
                // Breaking out. Nothing more to check.
                break;
            }
            else if('%' == c) {
                buffer.append(readPercentCharacter(reader));
            }
            else if(c == '/') {
                break;
            }
            else {
                // TODO: Error on invalid characters. (flag for User information/Authority-Part
                buffer.append(c);
            }
        }
        reader.skip(tracker - reader.position);
        if(host.getProtocol().isAnonymousConfigurable()) {
            host.getCredentials().setUsername(preferences.getProperty("connection.login.anon.name"));
        }
        else {
            host.getCredentials().setUsername(preferences.getProperty("connection.login.name"));
        }
        host.getCredentials().setPassword(null);
        if(atSignFlag) {
            if(userBuilder.length() > 0) {
                if(host.getProtocol().isUsernameConfigurable()) {
                    host.getCredentials().setUsername(userBuilder.toString());
                }
                else {
                    // TODO: Log warning.
                }
            }
            userBuilder.setLength(0);
            if(passwordBuilder != null) {
                if(passwordBuilder.length() > 0) {
                    if(host.getProtocol().isPasswordConfigurable()) {
                        host.getCredentials().setPassword(passwordBuilder.toString());
                    }
                    else {
                        // TODO: Log warning.
                    }
                }
                passwordBuilder.setLength(0);
            }

            return true;
        }
        return null;
    }

    static boolean parsePath(final StringReader reader, final Host host) {
        final StringBuilder pathBuilder = new StringBuilder();
        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if(isPChar(c) || c == '/') {
                pathBuilder.append(c);
            }
            else if(c == '%') {
                pathBuilder.append(readPercentCharacter(reader));
            }
            else if(c == ' ') {
                // This is a violation of RFC.
                // There must not be a space inside path
                pathBuilder.append(c);
            }
            else {
                // TODO: Error out. This is not supported.
                return false;
            }
        }

        if(pathBuilder.length() > 0) {
            if(host.getProtocol().isPathConfigurable()) {
                host.setDefaultPath(pathBuilder.toString());
            }
            else {
                // TODO: Log warning.
            }
        }
        return true;
    }

    static URITypes findURIType(final StringReader reader) {
        final StringReader copy = reader.copy();
        if(!copy.endOfString()) {
            char c = (char) copy.read();
            if(c == '/') {
                if(!copy.endOfString()) {
                    c = (char) copy.read();
                    if(c == '/') {
                        reader.skip(2);
                        if(!copy.endOfString()) {
                            c = (char) copy.read();
                            if(c == '/') {
                                return URITypes.Absolute;
                            }
                        }
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

    private static String readPercentCharacter(final StringReader reader) {
        final StringBuilder string = new StringBuilder();
        for(int i = 0; i < 2 && !reader.endOfString(); i++) {
            final char c = (char) reader.read();
            if(c == '%') {
                return "%%";
            }
            string.append(c);
        }
        if(string.length() != 2) {
            return Character.toString(Character.MIN_VALUE);
        }
        return Character.toString((char) Integer.parseUnsignedInt(string.toString(), 16));
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
        private final CharSequence text;
        private int position = 0;

        public StringReader(final CharSequence text) {
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
