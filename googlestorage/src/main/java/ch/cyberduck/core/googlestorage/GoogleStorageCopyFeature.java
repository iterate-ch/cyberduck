package ch.cyberduck.core.googlestorage;

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
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageCopyFeature implements Copy {

    private final PathContainerService containerService
        = new GoogleStoragePathContainerService();

    private final GoogleStorageSession session;

    public GoogleStorageCopyFeature(final GoogleStorageSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            final StorageObject object = session.getClient().objects().copy(containerService.getContainer(source).getName(), containerService.getKey(source),
                containerService.getContainer(target).getName(), containerService.getKey(target),
                session.getClient().objects().get(
                    containerService.getContainer(source).getName(), containerService.getKey(source)).execute()).execute();
            return new Path(target.getParent(), target.getName(), target.getType(),
                new GoogleStorageAttributesFinderFeature(session).toAttributes(object));
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source) && !containerService.isContainer(target);
    }
}
