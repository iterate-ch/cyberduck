package ch.cyberduck.core.ctera;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;

import java.text.MessageFormat;
import java.util.Locale;

public class CteraUrlProvider implements UrlProvider {

    private final Host host;

    public CteraUrlProvider(final Host host) {
        this.host = host;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        list.add(new DescriptiveUrl(String.format("%s/%s/%s",
                new HostUrlProvider().withUsername(false).withPath(false).get(host),
                "ServicesPortal/#/cloudDrive", URIEncoder.encode(PathRelativizer.relativize(host.getDefaultPath(), file.getAbsolute()))),
                DescriptiveUrl.Type.provider,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), host.getProtocol().getScheme().toString().toUpperCase(Locale.ROOT))));
        return list;
    }
}
