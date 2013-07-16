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

import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public class DescriptiveUrl {

    public static DescriptiveUrl EMPTY = new DescriptiveUrl(null, null);

    private String url = StringUtils.EMPTY;

    private String help = StringUtils.EMPTY;

    public DescriptiveUrl(final String url) {
        this(url, Locale.localizedString("Open in Web Browser"));
    }

    public DescriptiveUrl(final String url, final String help) {
        this.url = url;
        this.help = help;
    }

    public String getUrl() {
        return url;
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
        if(url != null ? !url.equals(that.url) : that.url != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }
}
