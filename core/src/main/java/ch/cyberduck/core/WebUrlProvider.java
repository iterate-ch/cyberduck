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
        final DescriptiveUrl base = this.toUrl();
        list.add(new DescriptiveUrl(URI.create(String.format("%s%s", base.getUrl(), URIEncoder.encode(
            PathNormalizer.normalize(PathRelativizer.relativize(PathNormalizer.normalize(host.getDefaultPath(), true), file.getAbsolute()))
            ))).normalize(),
                base.getType(),
                base.getHelp())
        );
        return list;
    }

    public DescriptiveUrl toUrl() {
        final String base;
        if(StringUtils.isBlank(host.getWebURL())) {
            switch(host.getProtocol().getScheme()) {
                case https:
                    base = String.format("https://%s/", StringUtils.strip(host.getHostname()));
                    break;
                default:
                    base = String.format("http://%s/", StringUtils.strip(host.getHostname()));
                    break;
            }
        }
        else {
            if(host.getWebURL().matches("^http(s)?://.*$")) {
                base = host.getWebURL();
            }
            else {
                base = String.format("http://%s/", host.getWebURL());
            }
        }
        final URI uri = URI.create(base);
        return new DescriptiveUrl(uri,
            DescriptiveUrl.Type.http,
            MessageFormat.format(LocaleFactory.localizedString("{0} URL"), StringUtils.upperCase(uri.getScheme())));
    }
}
