package ch.cyberduck.core;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * feedback@cyberduck.io
 */

import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;

public class DefaultWebUrlProvider implements WebUrlProvider {

    @Override
    public DescriptiveUrl toUrl(final Host bookmark) {
        final String base;
        if(StringUtils.isBlank(bookmark.getWebURL())) {
            switch(bookmark.getProtocol().getScheme()) {
                case https:
                    base = String.format("https://%s/", StringUtils.strip(bookmark.getHostname()));
                    break;
                default:
                    base = String.format("http://%s/", StringUtils.strip(bookmark.getHostname()));
                    break;
            }
        }
        else {
            if(bookmark.getWebURL().matches("^http(s)?://.*$")) {
                base = bookmark.getWebURL();
            }
            else {
                base = String.format("http://%s/", bookmark.getWebURL());
            }
        }
        return new DescriptiveUrl(base,
                DescriptiveUrl.Type.http,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                        StringUtils.upperCase(StringUtils.substringBefore(base, "://"))));
    }
}
