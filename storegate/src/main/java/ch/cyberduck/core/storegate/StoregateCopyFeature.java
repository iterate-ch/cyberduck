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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.CopyFileRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.EnumSet;

import static ch.cyberduck.core.features.Copy.validate;

public class StoregateCopyFeature implements Copy {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateCopyFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final CopyFileRequest copy = new CopyFileRequest()
                .name(target.getName())
                .parentID(fileid.getFileId(target.getParent()))
                .mode(1); // Overwrite
            final File file = new FilesApi(session.getClient()).filesCopy(
                fileid.getFileId(source), copy);
            listener.sent(status.getLength());
            fileid.cache(target, file.getId());
            return target.withAttributes(new StoregateAttributesFinderFeature(session, fileid).toAttributes(file));
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        Copy.super.preflight(source, target);
        validate(session.getCaseSensitivity(), source, target);
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
