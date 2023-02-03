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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UserDateFormatterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Duration;

public class MantaUrlProviderFeature implements UrlProvider {
    private static final Logger log = LogManager.getLogger(MantaUrlProviderFeature.class);

    private final MantaSession session;

    public MantaUrlProviderFeature(final MantaSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.attributes().getLink() != DescriptiveUrl.EMPTY) {
            list.add(file.attributes().getLink());
        }
        try {
            {
                final Duration expiresIn = Duration.ofMinutes(1);
                list.add(new DescriptiveUrl(
                    session.getClient().getAsSignedURI(file.getAbsolute(), "GET", expiresIn),
                    DescriptiveUrl.Type.signed,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                        + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getMediumFormat(System.currentTimeMillis() + expiresIn.toMillis()))));
            }
            {
                final Duration expiresIn = Duration.ofHours(1);
                list.add(new DescriptiveUrl(
                    session.getClient().getAsSignedURI(file.getAbsolute(), "GET", expiresIn),
                    DescriptiveUrl.Type.signed,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                        + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getMediumFormat(System.currentTimeMillis() + expiresIn.toMillis()))));
            }
            {
                final Duration expiresIn = Duration.ofDays(1);
                list.add(new DescriptiveUrl(
                    session.getClient().getAsSignedURI(file.getAbsolute(), "GET", expiresIn),
                    DescriptiveUrl.Type.signed,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                        + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getMediumFormat(System.currentTimeMillis() + expiresIn.toMillis()))));
            }
        }
        catch(IOException e) {
            log.warn(String.format("Failure creating signed URL for file %s. %s", file, e.getMessage()));
        }
        return list;
    }
}
