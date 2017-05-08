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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UrlProvider;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import synapticloop.b2.exception.B2ApiException;

public class B2UrlProvider implements UrlProvider {
    private static final Logger log = Logger.getLogger(B2UrlProvider.class);

    private final PathContainerService containerService
            = new PathContainerService();

    private final B2Session session;

    public B2UrlProvider(final B2Session session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrlBag toUrl(final Path file) {
        if(file.isVolume()) {
            return DescriptiveUrlBag.empty();
        }
        final DescriptiveUrlBag list = new DescriptiveUrlBag();
        if(file.isFile()) {
            final String download = String.format("%s/file/%s/%s", session.getClient().getDownloadUrl(),
                    URIEncoder.encode(containerService.getContainer(file).getName()),
                    URIEncoder.encode(containerService.getKey(file)));
            list.add(new DescriptiveUrl(URI.create(download), DescriptiveUrl.Type.http,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), Scheme.https.name().toUpperCase(Locale.ROOT))));
            try {
                final int seconds = 604800;
                // Determine expiry time for URL
                final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                expiry.add(Calendar.SECOND, seconds);
                final String token = session.getClient().getDownloadAuthorization(new B2FileidProvider(session).getFileid(containerService.getContainer(file)),
                        StringUtils.EMPTY, seconds);
                list.add(new DescriptiveUrl(URI.create(String.format("%s?Authorization=%s", download, token)), DescriptiveUrl.Type.signed,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                                + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                                UserDateFormatterFactory.get().getMediumFormat(expiry.getTimeInMillis()))
                ));
            }
            catch(B2ApiException | IOException | BackgroundException e) {
                log.warn(String.format("Failure getting download authorization token %s", e.getMessage()));
            }
        }
        return list;
    }
}
