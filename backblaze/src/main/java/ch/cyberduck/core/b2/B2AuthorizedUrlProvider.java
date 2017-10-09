package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.PromptUrlProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.TimeZone;

import synapticloop.b2.exception.B2ApiException;

public class B2AuthorizedUrlProvider implements PromptUrlProvider<Void> {
    private static final Logger log = Logger.getLogger(B2AuthorizedUrlProvider.class);

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;

    public B2AuthorizedUrlProvider(final B2Session session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Void none, final PasswordCallback callback) throws BackgroundException {
        if(file.isVolume()) {
            return DescriptiveUrl.EMPTY;
        }
        if(file.isFile()) {
            final String download = String.format("%s/file/%s/%s", session.getClient().getDownloadUrl(),
                URIEncoder.encode(containerService.getContainer(file).getName()),
                URIEncoder.encode(containerService.getKey(file)));
            try {
                final int seconds = 604800;
                // Determine expiry time for URL
                final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                expiry.add(Calendar.SECOND, seconds);
                final String token = session.getClient().getDownloadAuthorization(new B2FileidProvider(session).getFileid(containerService.getContainer(file), new DisabledListProgressListener()),
                    StringUtils.EMPTY, seconds);
                return new DescriptiveUrl(URI.create(String.format("%s?Authorization=%s", download, token)), DescriptiveUrl.Type.signed,
                    MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                        + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                        UserDateFormatterFactory.get().getMediumFormat(expiry.getTimeInMillis()))
                );
            }
            catch(B2ApiException e) {
                throw new B2ExceptionMappingService().map(e);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e);
            }
        }
        return DescriptiveUrl.EMPTY;
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Void aVoid, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }
}
