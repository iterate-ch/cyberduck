package ch.cyberduck.core.storegate;

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
import ch.cyberduck.core.Host;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FileSharesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateFileShareRequest;

import java.text.MessageFormat;

public class StoregateShareFeature implements Share<Void, Void> {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateShareFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case upload:
                return file.isDirectory();
        }
        return true;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        try {
            final Host bookmark = session.getHost();
            final CreateFileShareRequest request = new CreateFileShareRequest()
                    .fileId(fileid.getFileId(file));
            request.setPassword(callback.prompt(bookmark,
                    LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                    MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                    new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk())).getPassword());
            return new DescriptiveUrl(
                    new FileSharesApi(session.getClient()).fileSharesPost_0(request).getUrl(), DescriptiveUrl.Type.signed);
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map(e);
        }
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        try {
            final Host bookmark = session.getHost();
            final CreateFileShareRequest request = new CreateFileShareRequest()
                    .fileId(fileid.getFileId(file))
                    .allowUpload(true);
            request.setPassword(callback.prompt(bookmark,
                    LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                    MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                    new LoginOptions().anonymous(true).keychain(false).icon(bookmark.getProtocol().disk())).getPassword());
            return new DescriptiveUrl(
                    new FileSharesApi(session.getClient()).fileSharesPost_0(request).getUrl(), DescriptiveUrl.Type.signed);
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map(e);
        }
    }
}
