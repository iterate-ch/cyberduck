package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.HostWebUrlProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Locale;

public class DefaultUrlProvider implements UrlProvider {

    private final Host host;

    public DefaultUrlProvider(final Host host) {
        this.host = host;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file, final EnumSet<DescriptiveUrl.Type> types) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(types.contains(DescriptiveUrl.Type.http)) {
            if(file.attributes().getLink() != DescriptiveUrl.EMPTY) {
                list.add(file.attributes().getLink());
            }
        }
        if(types.contains(DescriptiveUrl.Type.provider)) {
            list.add(new DescriptiveUrl(String.format("%s%s",
                    new HostUrlProvider().withUsername(false).get(host), URIEncoder.encode(file.getAbsolute())),
                    DescriptiveUrl.Type.provider,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), host.getProtocol().getScheme().toString().toUpperCase(Locale.ROOT))));
        }
        if(types.contains(DescriptiveUrl.Type.http)) {
            list.addAll(new HostWebUrlProvider(host).toUrl(file, EnumSet.of(DescriptiveUrl.Type.http)));
        }
        return list;
    }
}
