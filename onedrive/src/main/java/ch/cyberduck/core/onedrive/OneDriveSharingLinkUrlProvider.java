package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.UrlProvider;

import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveSharingLink;

import java.io.IOException;
import java.net.URI;

public class OneDriveSharingLinkUrlProvider implements UrlProvider {
    private static final Logger log = Logger.getLogger(OneDriveSharingLinkUrlProvider.class);

    private final OneDriveSession session;

    public OneDriveSharingLinkUrlProvider(final OneDriveSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        try {
            final DescriptiveUrlBag list = new DescriptiveUrlBag();
            list.add(new DescriptiveUrl(URI.create(session.toFile(file).createSharedLink(OneDriveSharingLink.Type.VIEW).getLink().getWebUrl()),
                    DescriptiveUrl.Type.signed));
            return list;

        }
        catch(IOException e) {
            log.warn(String.format("Failure creating shared link. %s", e.getMessage()));
            return DescriptiveUrlBag.empty();
        }
    }
}
