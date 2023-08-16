package ch.cyberduck.core.googlestorage;

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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Share;

import org.jets3t.service.acl.Permission;

public class GoogleStoragePublicUrlProvider implements Share<Void, Void> {

    private final GoogleStorageSession session;

    public GoogleStoragePublicUrlProvider(final GoogleStorageSession session) {
        this.session = session;
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
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) throws BackgroundException {
        final GoogleStorageAccessControlListFeature acl = new GoogleStorageAccessControlListFeature(session);
        final Acl permission = acl.getPermission(file);
        final Acl.GroupUser everyone = new Acl.GroupUser(Acl.GroupUser.EVERYONE);
        final Acl.Role read = new Acl.Role(Permission.PERMISSION_READ.toString());
        if(!permission.asList().contains(new Acl.UserAndRole(everyone, read))) {
            permission.addAll(everyone, read);
            acl.setPermission(file, permission);
        }
        return new GoogleStorageUrlProvider(session).toUrl(file).find(DescriptiveUrl.Type.provider);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Void options, final PasswordCallback callback) {
        return DescriptiveUrl.EMPTY;
    }
}
