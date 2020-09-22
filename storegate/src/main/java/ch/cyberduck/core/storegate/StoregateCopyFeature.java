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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.CopyFileRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.transfer.TransferStatus;

public class StoregateCopyFeature implements Copy {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateCopyFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final CopyFileRequest copy = new CopyFileRequest()
                .name(target.getName())
                .parentID(fileid.getFileid(target.getParent(), new DisabledListProgressListener()))
                .mode(1); // Overwrite
            final File file = new FilesApi(session.getClient()).filesCopy(
                fileid.getFileid(source, new DisabledListProgressListener()), copy);
            return new Path(target.getParent(), target.getName(), target.getType(),
                new StoregateAttributesFinderFeature(session, fileid).toAttributes(file));
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }
}
