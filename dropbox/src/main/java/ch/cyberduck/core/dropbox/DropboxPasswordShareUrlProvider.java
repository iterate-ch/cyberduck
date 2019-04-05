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
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.PromptUrlProvider;

import org.apache.log4j.Logger;

import java.net.URI;
import java.text.MessageFormat;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.sharing.DbxUserSharingRequests;
import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkMetadata;
import com.dropbox.core.v2.sharing.SharedLinkSettings;

public class DropboxPasswordShareUrlProvider implements PromptUrlProvider<Void, Void> {
    private static final Logger log = Logger.getLogger(DropboxPasswordShareUrlProvider.class);

    private final DropboxSession session;

    public DropboxPasswordShareUrlProvider(final DropboxSession session) {
        this.session = session;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Void options, final PasswordCallback callback) throws BackgroundException {
        try {
            final SharedLinkMetadata share = new DbxUserSharingRequests(session.getClient()).createSharedLinkWithSettings(file.getAbsolute(),
                SharedLinkSettings.newBuilder().withRequestedVisibility(RequestedVisibility.PASSWORD).withLinkPassword(callback.prompt(
                    session.getHost(), LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                    LocaleFactory.localizedString("Provide additional login credentials", "Credentials"), new LoginOptions().user(false).password(true)
                ).getPassword()).build());
            if(log.isDebugEnabled()) {
                log.debug(String.format("Created shared link %s", share));
            }
            return new DescriptiveUrl(URI.create(share.getUrl()), DescriptiveUrl.Type.http,
                MessageFormat.format(LocaleFactory.localizedString("{0} URL"),
                    LocaleFactory.localizedString("Password Share", "Dropbox"))
            );
        }
        catch(DbxException e) {
            throw new DropboxExceptionMappingService().map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Void options, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return file.isFile();
        }
        return false;
    }
}
