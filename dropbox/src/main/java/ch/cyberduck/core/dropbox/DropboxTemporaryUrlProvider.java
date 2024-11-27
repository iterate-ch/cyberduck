package ch.cyberduck.core.dropbox;

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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.UserDateFormatterFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Calendar;
import java.util.TimeZone;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.files.CommitInfo;
import com.dropbox.core.v2.files.DbxUserFilesRequests;

public class DropboxTemporaryUrlProvider implements Share<Void, Void> {
    private static final Logger log = LogManager.getLogger(DropboxTemporaryUrlProvider.class);

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxTemporaryUrlProvider(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService();
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        try {
            log.debug("Create temporary link for {}", file);
            // This link will expire in four hours and afterwards you will get 410 Gone.
            final String link = new DbxUserFilesRequests(session.getClient(file)).getTemporaryLink(containerService.getKey(file)).getLink();
            // Determine expiry time for URL
            final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            expiry.add(Calendar.HOUR, 4);
            return new DescriptiveUrl(link, DescriptiveUrl.Type.signed,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Temporary", "S3"))
                    + " (" + MessageFormat.format(LocaleFactory.localizedString("Expires {0}", "S3") + ")",
                    UserDateFormatterFactory.get().getMediumFormat(expiry.getTimeInMillis()))
            );
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        try {
            log.debug("Create temporary upload link for {}", file);
            final String link = new DbxUserFilesRequests(session.getClient(file)).getTemporaryUploadLink(new CommitInfo(containerService.getKey(file))).getLink();
            return new DescriptiveUrl(link, DescriptiveUrl.Type.signed, MessageFormat.format(LocaleFactory.localizedString("{0} URL"), LocaleFactory.localizedString("Temporary", "S3")));
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return file.isFile();
        }
        return true;
    }
}
