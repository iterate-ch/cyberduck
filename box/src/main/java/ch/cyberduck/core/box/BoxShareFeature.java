package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginOptions;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.SharedLinksFilesApi;
import ch.cyberduck.core.box.io.swagger.client.api.SharedLinksFoldersApi;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.FilesFileIdaddSharedLinkBody;
import ch.cyberduck.core.box.io.swagger.client.model.FilesfileIdSharedLinkPermissions;
import ch.cyberduck.core.box.io.swagger.client.model.FilesfileIdaddSharedLinkSharedLink;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersFolderIdaddSharedLinkBody;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersfolderIdaddSharedLinkSharedLink;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersfolderIdaddSharedLinkSharedLinkPermissions;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Share;

import java.net.URI;
import java.text.MessageFormat;

public class BoxShareFeature implements Share {

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxShareFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        switch(type) {
            case download:
                return true;
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        if(file.isDirectory()) {
            return this.createFolderSharedLink(file, callback);
        }
        return this.createFileSharedLink(file, callback);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }

    private DescriptiveUrl createFileSharedLink(final Path file, final PasswordCallback callback) throws BackgroundException {
        try {
            final String password = this.prompt(file, callback);
            final File link = new SharedLinksFilesApi(new BoxApiClient(session.getClient())).putFilesIdAddSharedLink(
                    "shared_link",
                    fileid.getFileId(file),
                    new FilesFileIdaddSharedLinkBody()
                            .sharedLink(new FilesfileIdaddSharedLinkSharedLink().permissions(new FilesfileIdSharedLinkPermissions().canDownload(true))
                                    .password(password)));
            return new DescriptiveUrl(URI.create(link.getSharedLink().getDownloadUrl()), DescriptiveUrl.Type.signed);
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map(e);
        }
    }

    private DescriptiveUrl createFolderSharedLink(final Path file, final PasswordCallback callback) throws BackgroundException {
        try {
            final String password = this.prompt(file, callback);
            final Folder link = new SharedLinksFoldersApi(new BoxApiClient(session.getClient())).putFoldersIdAddSharedLink(
                    "shared_link",
                    fileid.getFileId(file),
                    new FoldersFolderIdaddSharedLinkBody()
                            .sharedLink(new FoldersfolderIdaddSharedLinkSharedLink().permissions(new FoldersfolderIdaddSharedLinkSharedLinkPermissions().canDownload(false))
                                    .password(password)));
            return new DescriptiveUrl(URI.create(link.getSharedLink().getUrl()), DescriptiveUrl.Type.signed);
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map(e);
        }
    }

    private String prompt(final Path file, final PasswordCallback callback) throws LoginCanceledException {
        final Credentials password = callback.prompt(session.getHost(),
                LocaleFactory.localizedString("Passphrase", "Cryptomator"),
                MessageFormat.format(LocaleFactory.localizedString("Create a passphrase required to access {0}", "Credentials"), file.getName()),
                new LoginOptions().anonymous(true).keychain(false).icon(session.getHost().getProtocol().disk()));
        if(password.isPasswordAuthentication()) {
            return password.getPassword();
        }
        return null;
    }
}
