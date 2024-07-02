package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.HostUrlProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;

import java.net.URI;

public class DeepboxUrlProvider implements UrlProvider {
    private final DeepboxSession session;

    public DeepboxUrlProvider(final DeepboxSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile() && file.attributes().getFileId() != null) {
            // For now, use pattern https://{env}.deepbox.swiss/node/{nodeId}/preview, API forthcoming
            list.add(new DescriptiveUrl(URI.create(new HostUrlProvider()
                    .withPath(true).withUsername(false)
                    .get(session.getHost().getProtocol().getScheme(),
                            session.getHost().getPort(),
                            null,
                            String.format("%sdeepbox.swiss", session.getStage()),
                            String.format("/node/%s/preview", file.attributes().getFileId())))));
        }
        return list;
    }
}
