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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class HostParser {
    private static final Logger log = LogManager.getLogger(HostParser.class);

    private static final Preferences preferences = PreferencesFactory.get();

    /**
     * Parses URL in the format ftp://username:pass@hostname:portnumber/path/to/file
     *
     * @param url URL
     * @return Bookmark
     */
    public static Host parse(final String url) throws HostParserException {
        final Host parsed = new HostParser().get(url);
        if(log.isDebugEnabled()) {
            log.debug("Parsed {} as {}", url, parsed);
        }
        return parsed;
    }

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

    private static <T> T decorate(final T t, final Consumer<T> decorator) {
        if(decorator != null) {
            decorator.accept(t);
        }
        return t;
    }

    public Host get(final String url) throws HostParserException {
        final StringReader reader = new StringReader(url);
        final Protocol parsedProtocol, protocol;
        if((parsedProtocol = findProtocol(reader, factory)) != null) {
            protocol = parsedProtocol;
        }
        else {
            protocol = defaultScheme;
        }
        final Consumer<HostParserException> parsedProtocolDecorator = e -> e.withProtocol(parsedProtocol);
        final Host host = new Host(protocol);
        final URITypes uriType = findURIType(reader);
        if(uriType == URITypes.Undefined) {
            // scheme:
            if(StringUtils.isBlank(protocol.getDefaultHostname())) {
                throw decorate(new HostParserException(String.format("Missing hostname in URI %s", url)), parsedProtocolDecorator);
            }
            return host;
        }

        if(uriType == URITypes.Authority) {
            if(host.getProtocol().isHostnameConfigurable()) {
                parseAuthority(reader, host, parsedProtocolDecorator);
            }
            else {
                parseRootless(reader, host, parsedProtocolDecorator);
            }
        }
        else if(uriType == URITypes.Rootless) {
            parseRootless(reader, host, parsedProtocolDecorator);
        }
        else if(uriType == URITypes.Absolute) {
            parseAbsolute(reader, host, parsedProtocolDecorator);
        }
        if(log.isDebugEnabled()) {
            log.debug("Parsed {} as {}", url, host);
        }
        return host;
    }

    private static Protocol findProtocol(final StringReader reader, final ProtocolFactory factory) {
        Value<String> schemeValue = new Value<>();
        if(!parseScheme(reader, schemeValue)) {
            return null;
        }
        if(schemeValue.getValue() == null) {
            return null;
        }
        return factory.forName(schemeValue.getValue());
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

    static void parseAuthority(final StringReader reader, final Host host, final Consumer<HostParserException> decorator) throws HostParserException {
        parseUserInfo(reader, host, decorator);
        parseHostname(reader, host, decorator);
        parsePath(reader, host, false, decorator);
    }

    static void parseHostname(final StringReader reader, final Host host, final Consumer<HostParserException> decorator) throws HostParserException {
        final StringBuilder buffer = new StringBuilder();

        boolean isPort = false;
        boolean bracketFlag = false;
        while(!reader.endOfString()) {
            final char c = (char) reader.peek();

            if('/' == c) {
                break;
            }

            if(bracketFlag) {
                reader.skip(1);
                if(c == '[') {
                    throw decorate(new HostParserException("Illegal character '[' inside IPv6 address"), decorator);
                }
                else if(c == ']') {
                    bracketFlag = false;
                }
                else if(Character.isLetterOrDigit(c) || c == ':' || c == '%') {
                    buffer.append(c);
                }
                else {
                    throw decorate(new HostParserException(String.format("Illegal character '%s' at %d inside IPv6 address", c, reader.position)), decorator);
                }
            }
            else {
                if(!readPercentEscapedSequence(reader, buffer)) {
                    reader.skip(1);
                    if(c == ']') {
                        throw decorate(new HostParserException("Illegal character ']' outside IPv6 address"), decorator);
                    }
                    else if(c == '[') {
                        bracketFlag = true;
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
        }
        if(bracketFlag) {
            throw decorate(new HostParserException("IPv6 bracket not closed in URI"), decorator);
        }
        if(buffer.length() == 0) {
            if(StringUtils.isEmpty(host.getHostname())) {
                throw decorate(new HostParserException("Missing hostname in URI"), decorator);
            }
        }
        else {
            host.setHostname(buffer.toString());
        }
        if(isPort) {
            parsePort(reader, host, decorator);
        }
    }

    static void parsePort(final StringReader reader, final Host host, final Consumer<HostParserException> decorator) throws HostParserException {
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
                    log.warn("Got {} in port. This is unsupported. Continuing with default port", c);
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
                throw decorate(new HostParserException(String.format("Port %d is outside allowed range 0-65536", port)), decorator);
            }

            host.setPort(port);
        }
    }

    static void parseAbsolute(final StringReader reader, final Host host, final Consumer<HostParserException> decorator) {
        parsePath(reader, host, true, decorator);
    }

    static void parseRootless(final StringReader reader, final Host host, final Consumer<HostParserException> decorator) throws HostParserException {
        // This is not RFC-compliant.
        // * Rootless-path must not include authentication information.
        final boolean userInfoResult = parseUserInfo(reader, host, decorator);

        if(host.getProtocol().isHostnameConfigurable() && StringUtils.isWhitespace(host.getHostname())) {
            // This is not RFC-compliant.
            // We assume for hostconfigurable-empty-hostnames a hostname on first path segment
            parseHostname(reader, host, decorator);
        }
        parsePath(reader, host, false, decorator);
    }

    static boolean parseUserInfo(final StringReader reader, final Host host, final Consumer<HostParserException> decorator) throws HostParserException {
        int tracker = reader.position;
        final StringBuilder buffer = new StringBuilder();
        final StringBuilder userBuilder = new StringBuilder();
        StringBuilder passwordBuilder = null;

        boolean atSignFlag = false;
        while(!reader.endOfString()) {
            if(!readPercentEscapedSequence(reader, buffer)) {
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
                            throw decorate(new HostParserException(
                                    String.format("Space character in user info part of URL at %d", reader.position)),
                                decorator);
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
                host.getCredentials().setUsername(userBuilder.toString());
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

    static void parsePath(final StringReader reader, final Host host, final boolean assumeRoot, final Consumer<HostParserException> decorator) {
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
            if(!readPercentEscapedSequence(reader, pathBuilder)) {
                // This is a violation of RFC.
                pathBuilder.append((char) reader.read());
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

    private static boolean readPercentEscapedSequence(final StringReader reader, final StringBuilder builder) {
        Integer b = readPercentCharacter(reader);
        if(b == null) {
            return false;
        }
        final char append;
        if(b < 0x80) {
            append = (char) b.byteValue();
        }
        else if((b & 0xE0) == 0xC0) {
            final Character c = handleTwoByteSequence(b.byteValue(), reader);
            if(c == null) {
                return false;
            }
            append = c;
        }
        else if((b & 0xF0) == 0xE0) {
            final Character c = handleThreeByteSequence(b.byteValue(), reader);
            if(c == null) {
                return false;
            }
            append = c;
        }
        else if((b & 0xf8) == 0xf0 && b <= 0xf4) {
            final Character c = handleFourByteSequence(b.byteValue(), reader);
            if(c == null) {
                return false;
            }
            append = c;
        }
        else {
            return false;
        }
        if(append >= 0xD800 && append <= 0xDFFF) {
            // surrogate
            return false;
        }

        builder.append(append);
        return true;
    }

    private static Character handleTwoByteSequence(final byte b, final StringReader reader) {
        final Integer b2 = readPercentCharacter(reader);
        if(b2 == null) {
            return null;
        }

        return (char) ((b & 0x1f) << 6 | (b2.byteValue() & 0x3f));
    }

    private static Character handleThreeByteSequence(final byte b, final StringReader reader) {
        final Integer b2 = readPercentCharacter(reader);
        if(b2 == null) {
            return null;
        }
        final Integer b3 = readPercentCharacter(reader);
        if(b3 == null) {
            return null;
        }

        return (char) ((b & 0x0f) << 12 | (b2.byteValue() & 0x3f) << 6 | (b3.byteValue() & 0x3f));
    }

    private static Character handleFourByteSequence(final byte b, final StringReader reader) {
        final Integer b2 = readPercentCharacter(reader);
        if(b2 == null) {
            return null;
        }
        final Integer b3 = readPercentCharacter(reader);
        if(b3 == null) {
            return null;
        }
        final Integer b4 = readPercentCharacter(reader);
        if(b4 == null) {
            return null;
        }

        return (char) ((b & 0x07) << 18 | (b2.byteValue() & 0x3f) << 12 | (b3.byteValue() & 0x3f) << 6
            | (b4.byteValue() & 0x4f));
    }

    private static Integer readPercentCharacter(final StringReader reader) {
        int start = reader.position;
        if(reader.read() != '%') {
            reader.skip(start - reader.position);
            return null;
        }

        int value = 0;
        for(int i = 0; i < 2 && !reader.endOfString(); i++) {
            final int cv = Character.digit((char) reader.read(), 16);
            if(cv < 0) {
                reader.skip(start - reader.position);
                return null;
            }
            value = value * 16 + cv;
        }
        return value;
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
