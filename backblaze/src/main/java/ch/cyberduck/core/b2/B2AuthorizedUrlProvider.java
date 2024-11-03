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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.Calendar;
import java.util.TimeZone;

import synapticloop.b2.exception.B2ApiException;

public class B2AuthorizedUrlProvider implements Share<Void, Void> {
    private static final Logger log = LogManager.getLogger(B2AuthorizedUrlProvider.class);

    private final PathContainerService containerService
        = new B2PathContainerService();

    private final B2Session session;
    private final B2VersionIdProvider fileid;

    public B2AuthorizedUrlProvider(final B2Session session, final B2VersionIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return file.isFile();
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Void none, final PasswordCallback callback) throws BackgroundException {
        final String download = String.format("%s/file/%s/%s", session.getClient().getDownloadUrl(),
                URIEncoder.encode(containerService.getContainer(file).getName()),
                URIEncoder.encode(containerService.getKey(file)));
        try {
            log.debug("Create download authorization for {}", file);
            final int seconds = 604800;
            // Determine expiry time for URL
            final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            expiry.add(Calendar.SECOND, seconds);
            final String token = session.getClient().getDownloadAuthorization(fileid.getVersionId(containerService.getContainer(file)),
                StringUtils.EMPTY, seconds);
            return new DescriptiveUrl(URI.create(String.format("%s?Authorization=%s", download, token)), DescriptiveUrl.Type.signed,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Pre-Signed", "S3"))
                    + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getMediumFormat(expiry.getTimeInMillis()))
            );
        }
        catch(B2ApiException e) {
            throw new B2ExceptionMappingService(fileid).map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Void none, final PasswordCallback callback) throws BackgroundException {
        return DescriptiveUrl.EMPTY;
    }
}
