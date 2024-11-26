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

import java.text.MessageFormat;
import java.util.Objects;

public class DescriptiveUrl {

    public static final DescriptiveUrl EMPTY = new DescriptiveUrl(StringUtils.EMPTY);

    private final String url;
    private final Type type;
    private final String help;

    public enum Type {
        /**
         * Native protocol
         */
        provider,
        /**
         * Web URL
         */
        http,
        cdn,
        origin,
        cname,
        signed,
        authenticated,
        analytics,
        encrypted
    }

    public DescriptiveUrl(final String url) {
        this(url, Type.http, MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                StringUtils.upperCase(StringUtils.substringBefore(url, "://"))));
    }

    public DescriptiveUrl(final String url, Type type) {
        this(url, type, MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                StringUtils.upperCase(StringUtils.substringBefore(url, "://"))));
    }

    public DescriptiveUrl(final String url, Type type, final String help) {
        this.url = url;
        this.type = type;
        this.help = help;
    }

    public DescriptiveUrl(final DescriptiveUrl other) {
        this.url = other.url;
        this.type = other.type;
        this.help = other.help;
    }

    public String getUrl() {
        return url.toString();
    }

    public Type getType() {
        return type;
    }

    public String getHelp() {
        return help;
    }

    public String getPreview() {
        return this.getUrl();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final DescriptiveUrl that = (DescriptiveUrl) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DescriptiveUrl{");
        sb.append("url=").append(url);
        sb.append(", type=").append(type);
        sb.append(", help='").append(help).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
