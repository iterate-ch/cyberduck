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

public class HostWebUrlProvider implements UrlProvider {

    private final Host host;

    public HostWebUrlProvider(final Host host) {
        this.host = host;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        final DescriptiveUrl base = new DefaultWebUrlProvider().toUrl(host);
        list.add(new DescriptiveUrl(String.format("%s%s", StringUtils.stripEnd(base.getUrl(), String.valueOf(Path.DELIMITER)), URIEncoder.encode(
                PathNormalizer.normalize(PathRelativizer.relativize(PathNormalizer.normalize(host.getDefaultPath(), true), file.getAbsolute()))
        )), base.getType(), base.getHelp()));
        return list;
    }
}
