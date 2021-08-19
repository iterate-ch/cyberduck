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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.idna.PunycodeConverter;

import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.text.MessageFormat;
import java.util.Arrays;

public class CustomSchemeUrlProvider implements UrlProvider {

    private final Host host;

    public CustomSchemeUrlProvider(final Host host) {
        this.host = host;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        for(String scheme : host.getProtocol().getSchemes()) {
            if(Arrays.stream(Scheme.values()).noneMatch(s -> s.name().equals(scheme))) {
                list.add(new DescriptiveUrl(URI.create(String.format("%s://%s%s",
                    scheme, new PunycodeConverter().convert(host.getHostname()), PathNormalizer.normalize(file.getAbsolute()))),
                    DescriptiveUrl.Type.provider,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), StringUtils.capitalize(scheme))));
            }
        }
        return list;
    }
}
