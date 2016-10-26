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

import java.net.URI;
import java.text.MessageFormat;

public class WebUrlProvider implements UrlProvider {

    private final Host host;

    public WebUrlProvider(final Host host) {
        this.host = host;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        list.add(new DescriptiveUrl(URI.create(String.format("%s/%s", host.getWebURL(), URIEncoder.encode(PathRelativizer.relativize(
                        PathNormalizer.normalize(host.getDefaultPath(), true), file.getAbsolute())
        ))).normalize(),
                DescriptiveUrl.Type.http,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), "HTTP")));
        return list;
    }
}
