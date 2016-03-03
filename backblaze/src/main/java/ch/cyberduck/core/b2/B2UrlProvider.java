package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.shared.DefaultUrlProvider;

import java.net.URI;

public class B2UrlProvider extends DefaultUrlProvider {

    private final PathContainerService containerService
            = new B2PathContainerService();

    private final B2Session session;

    public B2UrlProvider(final B2Session session) {
        super(session.getHost());
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag urls = super.toUrl(file);
        final String download = String.format("%s/file/%s/%s", session.getClient().getDownloadUrl(),
                URIEncoder.encode(containerService.getContainer(file).getName()),
                URIEncoder.encode(containerService.getKey(file)));
        urls.add(new DescriptiveUrl(URI.create(download), DescriptiveUrl.Type.http));
        return urls;
    }
}
