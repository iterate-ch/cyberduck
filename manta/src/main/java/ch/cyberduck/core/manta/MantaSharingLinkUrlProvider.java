package ch.cyberduck.core.manta;

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

import java.io.IOException;
import java.net.URI;

public class MantaSharingLinkUrlProvider implements UrlProvider {
    private static final Logger log = Logger.getLogger(MantaSharingLinkUrlProvider.class);

    private final MantaSession session;

    public MantaSharingLinkUrlProvider(final MantaSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        try {

            if(Integer.BYTES == 0) {
                final DescriptiveUrlBag list = new DescriptiveUrlBag();
                list.add(new DescriptiveUrl(
                        URI.create(
                                ""
//                            session.toFile(file)
//                                    .createSharedLink(MantaSharingLink.Type.VIEW)
//                                    .getLink().
//                                    getWebUrl()
                        ),
                        DescriptiveUrl.Type.signed));
                return list;
            }
            else {
                throw new IOException();
            }

        }
        catch(IOException e) {
            log.warn(String.format("Failure creating shared link. %s", e.getMessage()));
            return DescriptiveUrlBag.empty();
        }
    }
}
