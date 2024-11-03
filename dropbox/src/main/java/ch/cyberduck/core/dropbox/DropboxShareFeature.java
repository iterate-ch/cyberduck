package ch.cyberduck.core.dropbox;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.features.Share;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.filerequests.DbxUserFileRequestsRequests;
import com.dropbox.core.v2.filerequests.FileRequest;
import com.dropbox.core.v2.sharing.DbxUserSharingRequests;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;

public class DropboxShareFeature implements Share<Void, Void> {
    private static final Logger log = LogManager.getLogger(DropboxShareFeature.class);

    private final DropboxSession session;
    private final PathContainerService containerService;

    public DropboxShareFeature(final DropboxSession session) {
        this.session = session;
        this.containerService = new DropboxPathContainerService(session);
    }

    protected SharedLinkSettings.Builder toSettings(final Path file, final PasswordCallback callback) throws LoginCanceledException {
        final SharedLinkSettings.Builder settings = SharedLinkSettings.newBuilder();
        settings.withRequestedVisibility(RequestedVisibility.PUBLIC);
        return settings;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        final SharedLinkSettings settings = this.toSettings(file, callback).build();
        try {
            try {
                final SharedLinkMetadata share = new DbxUserSharingRequests(session.getClient(file))
                        .createSharedLinkWithSettings(containerService.getKey(file),
                                settings);
                log.debug("Created shared link {}", share);
                return new DescriptiveUrl(URI.create(share.getUrl()), DescriptiveUrl.Type.signed,
                        MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                                LocaleFactory.localizedString("Password Share", "Dropbox"))
                );
            }
            catch(DbxException e) {
                throw new DropboxExceptionMappingService().map(e);
            }
        }
        catch(ConflictException e) {
            // Shared link already exists
            try {
                final List<SharedLinkMetadata> links = new DbxUserSharingRequests(session.getClient(file))
                        .listSharedLinksBuilder().withDirectOnly(true).withPath(containerService.getKey(file)).start().getLinks();
                for(SharedLinkMetadata link : links) {
                    log.debug("Return existing shared link {}", link);
                    final SharedLinkMetadata share = new DbxUserSharingRequests(session.getClient(file)).modifySharedLinkSettings(link.getUrl(),
                            settings);
                    return new DescriptiveUrl(URI.create(share.getUrl()), DescriptiveUrl.Type.signed,
                            MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                                    LocaleFactory.localizedString("Password Share", "Dropbox"))
                    );
                }
                throw e;
            }
            catch(DbxException f) {
                throw e;
            }
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        try {
            final FileRequest request = new DbxUserFileRequestsRequests(session.getClient())
                    .create(file.getName(), file.isRoot() ? file.getAbsolute() : containerService.getKey(file));
            return new DescriptiveUrl(URI.create(request.getUrl()), DescriptiveUrl.Type.signed);
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return true;
            case upload:
                if(file.isRoot()) {
                    return false;
                }
                return file.isDirectory();
        }
        return false;
    }
}
