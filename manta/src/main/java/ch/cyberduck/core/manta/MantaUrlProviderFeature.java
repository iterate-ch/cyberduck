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

/**
 * Created by tomascelaya on 5/23/17.
 */

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DescriptiveUrlBag;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;

import java.io.IOException;
import java.time.Duration;

public class MantaUrlProviderFeature implements UrlProvider {

    private final MantaSession session;

    public MantaUrlProviderFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.attributes().getLink() != null) {
            list.add(file.attributes().getLink());
        }

        try {
            list.add(new DescriptiveUrl(
                    session.getClient().getAsSignedURI(file.getAbsolute(), "GET", Duration.ofMinutes(1)),
                    DescriptiveUrl.Type.signed,
                    "Expiring link (1 minute)"));
            list.add(new DescriptiveUrl(
                    session.getClient().getAsSignedURI(file.getAbsolute(), "GET", Duration.ofHours(1)),
                    DescriptiveUrl.Type.signed,
                    "Expiring link (1 hour)"));
            list.add(new DescriptiveUrl(
                    session.getClient().getAsSignedURI(file.getAbsolute(), "GET", Duration.ofDays(1)),
                    DescriptiveUrl.Type.signed,
                    "Expiring link (1 day)"));
        }
        catch(IOException e) {
        }

        return list;
    }
}