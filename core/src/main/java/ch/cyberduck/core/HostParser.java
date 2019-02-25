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

import ch.cyberduck.core.exception.HostParserException;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.log4j.Logger;

import java.util.Optional;
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

    public Host get(final String url) throws HostParserException {
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
    public static Host parse(final String url) throws HostParserException {
        final Host parsed = new HostParser(ProtocolFactory.get(), ProtocolFactory.get().forName(
            preferences.getProperty("connection.protocol.default"))).get(url);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Parsed %s as %s", url, parsed));
        }
        return parsed;
    }

    private static Host parse(final ProtocolFactory factory, final Protocol defaultScheme, final String url) throws HostParserException {
        final StringReader reader = new StringReader(url);
        Value<String> schemeValue = new Value<>();
        Protocol protocol;
        if(!parseScheme(reader, schemeValue)
            || null == schemeValue.getValue()
            || (protocol = factory.forName(schemeValue.getValue())) == null) {
            protocol = defaultScheme;
        }
        final Host host = new Host(protocol);
        final URITypes uriType = findURIType(reader);
        if(uriType == URITypes.Undefined) {
            // scheme:
            if(StringUtils.isBlank(protocol.getDefaultHostname())) {
                throw new HostParserException(String.format("Missing hostname in URI %s", url));
            }
            return host;
        }
        if(uriType == URITypes.Authority) {
            if(host.getProtocol().isHostnameConfigurable()) {
                parseAuthority(reader, host);
            }
            else {
                parseRootless(reader, host);
            }
        }
        else if(uriType == URITypes.Rootless) {
            parseRootless(reader, host);
        }
        else if(uriType == URITypes.Absolute) {
            parseAbsolute(reader, host);
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
                if(c == '.') {
                    // THIS IS VIOLATION OF RFC.
                    // There can be '.' in URIs.
                    // This works against "s3.amazonaws.com:443".
                    reader.skip(tracker - reader.position);
                    return false;
                }
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

    static void parseAuthority(final StringReader reader, final Host host) throws HostParserException {
        parseUserInfo(reader, host);
        parseHostname(reader, host);
        parsePath(reader, host, false);
    }

    static void parseHostname(final StringReader reader, final Host host) throws HostParserException {
        final StringBuilder buffer = new StringBuilder();

        boolean isPort = false;
        boolean bracketFlag = false;
        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if('/' == c) {
                reader.skip(-1);
                break;
            }
            else if(bracketFlag) {
                if(c == '[') {
                    throw new HostParserException("Illegal character '[' inside IPv6 address");
                }
                else if(c == ']') {
                    bracketFlag = false;
                }
                else if(Character.isLetterOrDigit(c) || c == ':' || c == '%') {
                    buffer.append(c);
                }
                else {
                    throw new HostParserException(String.format("Illegal character '%s' at %d inside IPv6 address", c, reader.position));
                }
            }
            else {
                if(c == ']') {
                    throw new HostParserException("Illegal character ']' outside IPv6 address");
                }
                else if(c == '[') {
                    bracketFlag = true;
                }
                else if(c == '%') {
                    buffer.append(readPercentCharacter(reader));
                }
                else if(c == ':') {
                    isPort = true;
                    break;
                }
                else {
                    buffer.append(c);
                }
            }
        }
        if(bracketFlag) {
            throw new HostParserException("IPv6 bracket not closed in URI");
        }
        if(buffer.length() == 0) {
            if(StringUtils.isEmpty(host.getHostname())) {
                throw new HostParserException("Missing hostname in URI");
            }
        }
        else {
            host.setHostname(buffer.toString());
        }
        if(isPort) {
            parsePort(reader, host);
        }
    }

    static void parsePort(final StringReader reader, final Host host) throws HostParserException {
        Integer port = null;
        int tracker = reader.position;

        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if(Character.isDigit(c)) {
                port = Optional.ofNullable(port).orElse(0) * 10 + Character.getNumericValue(c);
            }
            else {
                if(c != '/') {
                    port = null;
                    log.warn(String.format("Got %s in port. This is unsupported. Continuing with default port", c));
                    reader.skip(tracker - reader.position);
                }
                else {
                    reader.skip(-1);
                }
                break;
            }
        }
        if(port != null && host.getProtocol().isPortConfigurable()) {
            if(port <= 0 || port >= 65536) {
                throw new HostParserException(String.format("Port %d is outside allowed range 0-65536", port));
            }

            host.setPort(port);
        }
    }

    static void parseAbsolute(final StringReader reader, final Host host) {
        parsePath(reader, host, true);
    }

    static void parseRootless(final StringReader reader, final Host host) throws HostParserException {
        // This is not RFC-compliant.
        // * Rootless-path must not include authentication information.
        final boolean userInfoResult = parseUserInfo(reader, host);

        if(host.getProtocol().isHostnameConfigurable() && StringUtils.isWhitespace(host.getHostname())) {
            // This is not RFC-compliant.
            // We assume for hostconfigurable-empty-hostnames a hostname on first path segment
            parseHostname(reader, host);
        }
        parsePath(reader, host, false);
    }

    static boolean parseUserInfo(final StringReader reader, final Host host) throws HostParserException {
        int tracker = reader.position;
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder userBuilder = new StringBuilder();
        StringBuilder passwordBuilder = null;

        boolean atSignFlag = false;
        while(!reader.endOfString()) {
            final char c = (char) reader.read();
            if('@' == c) {
                if(atSignFlag) {
                    buffer.insert(0, c);
                }
                atSignFlag = true;
                final int length = buffer.length();
                for(int i = 0; i < length; i++) {
                    char t = buffer.charAt(i);
                    if(t == ' ') {
                        throw new HostParserException(String.format("Space character in user info part of URL at %d", reader.position));
                    }
                    if(t == '%') {
                        t = (char) Integer.parseInt(buffer.substring(i + 1, i + 3), 16);
                        i += 2;
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
            }
            else if(c == '/') {
                break;
            }
            else {
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
                    log.warn("Username specified on protocol which does not support user credentials. Username will be ignored.");
                }
            }
            userBuilder.setLength(0);
            if(passwordBuilder != null) {
                if(passwordBuilder.length() > 0) {
                    if(host.getProtocol().isPasswordConfigurable()) {
                        host.getCredentials().setPassword(passwordBuilder.toString());
                    }
                    else {
                        log.warn("Password specified on protocol which does not support user credentials. Password will be ignored.");
                    }
                }
                passwordBuilder.setLength(0);
            }
            return true;
        }
        return false;
    }

    static void parsePath(final StringReader reader, final Host host, final boolean assumeRoot) {
        final StringBuilder pathBuilder = new StringBuilder();
        if(assumeRoot) {
            if(reader.peek() == '/') {
                pathBuilder.append((char) reader.read());
            }
            else {
                pathBuilder.append('/');
            }
        }
        while(!reader.endOfString()) {
            final char c = (char) reader.read();

            if(c == '%') {
                pathBuilder.append(readPercentCharacter(reader));
            }
            else {
                // This is a violation of RFC.
                pathBuilder.append(c);
            }
        }
        if(pathBuilder.length() > 0) {
            if(host.getProtocol().isPathConfigurable()) {
                host.setDefaultPath(pathBuilder.toString());
            }
            else {
                if(StringUtils.isNotBlank(host.getDefaultPath())) {
                    if(pathBuilder.indexOf(host.getDefaultPath()) != -1) {
                        host.setDefaultPath(pathBuilder.toString());
                    }
                    else {
                        host.setDefaultPath(String.format("%s%s", host.getDefaultPath(), pathBuilder));
                    }
                }
                else {
                    host.setDefaultPath(pathBuilder.toString());
                }
            }
        }
    }

    static URITypes findURIType(final StringReader reader) {
        final StringReader copy = reader.copy();
        if(!copy.endOfString()) {
            char c = (char) copy.read();
            if(c == '/') {
                reader.skip(1);
                if(!copy.endOfString()) {
                    c = (char) copy.read();
                    if(c == '/') {
                        reader.skip(1);
                        return URITypes.Authority;
                    }
                    reader.skip(-1);
                }
                return URITypes.Absolute;
            }
            return URITypes.Rootless;
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
}
