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

import java.net.URI;

public class DescriptiveUrl {

    public static final DescriptiveUrl EMPTY = new DescriptiveUrl(null);

    private final URI url;

    private final Type type;

    private String help = StringUtils.EMPTY;

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
        torrent,
        authenticated,
        analytics
    }

    public DescriptiveUrl(final URI url) {
        this(url, Type.http, LocaleFactory.localizedString("Open in Web Browser"));
    }

    public DescriptiveUrl(final URI url, Type type) {
        this(url, type, LocaleFactory.localizedString("Open in Web Browser"));
    }

    public DescriptiveUrl(final URI url, Type type, final String help) {
        this.url = url;
        this.type = type;
        this.help = help;
    }

    public String getUrl() {
        if(null == url) {
            return null;
        }
        return url.toString();
    }

    public Type getType() {
        return type;
    }

    public String getHelp() {
        return help;
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
        if(type != that.type) {
            return false;
        }
        if(url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return this.getUrl();
    }
}
