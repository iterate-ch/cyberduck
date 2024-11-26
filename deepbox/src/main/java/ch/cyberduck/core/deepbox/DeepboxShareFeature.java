package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.ShareLinkRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.ShareLinkAdd;
import ch.cyberduck.core.deepbox.io.swagger.client.model.ShareLinkResource;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Share;

public class DeepboxShareFeature implements Share<Object, Object> {

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxShareFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public boolean isSupported(final Path file, final Type type) {
        if(type == Type.download) {
            return file.getType().contains(AbstractPath.Type.file);
        }
        return false;
    }

    @Override
    public DescriptiveUrl toDownloadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        return this.createFileSharedLink(file, callback);
    }

    @Override
    public DescriptiveUrl toUploadUrl(final Path file, final Sharee sharee, final Object options, final PasswordCallback callback) throws BackgroundException {
        throw new UnsupportedException();
    }

    private DescriptiveUrl createFileSharedLink(final Path file, final PasswordCallback callback) throws BackgroundException {
        try {
            final ShareLinkAdd body = new ShareLinkAdd();
            body.setNodeId(fileid.getFileId(file));
            final ShareLinkResource link = new ShareLinkRestControllerApi(session.getClient()).createShareLinkResource(body, null);
            return new DescriptiveUrl(link.getShareUrl(), DescriptiveUrl.Type.signed);
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map(e);
        }
    }
}
