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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.LoginCanceledException;

import java.text.MessageFormat;

import com.dropbox.core.v2.sharing.RequestedVisibility;
import com.dropbox.core.v2.sharing.SharedLinkSettings;

public class DropboxPasswordShareFeature extends DropboxShareFeature {

    private final DropboxSession session;

    public DropboxPasswordShareFeature(final DropboxSession session) {
        super(session);
        this.session = session;
    }

    protected SharedLinkSettings.Builder toSettings(final Path file, final PasswordCallback callback) throws LoginCanceledException {
        final SharedLinkSettings.Builder settings = super.toSettings(file, callback);
        final Host bookmark = session.getHost();
        final Credentials password = callback.prompt(bookmark,
                LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk()));
        if(password.isPasswordAuthentication()) {
            settings.withLinkPassword(password.getPassword());
            settings.withRequestedVisibility(RequestedVisibility.PASSWORD);
        }
        return settings;
    }
}
