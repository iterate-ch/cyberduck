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
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.RewriteResponse;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageCopyFeature implements Copy {
    private static final Logger log = Logger.getLogger(GoogleStorageMoveFeature.class);

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    public GoogleStorageCopyFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final Storage.Objects.Get request = session.getClient().objects().get(containerService.getContainer(source).getName(), containerService.getKey(source));
            if(StringUtils.isNotBlank(source.attributes().getVersionId())) {
                request.setGeneration(Long.parseLong(source.attributes().getVersionId()));
            }
            final StorageObject storageObject = request.execute();
            final Storage.Objects.Rewrite rewrite = session.getClient().objects().rewrite(containerService.getContainer(source).getName(), containerService.getKey(source),
                containerService.getContainer(target).getName(), containerService.getKey(target), storageObject);
            final RewriteResponse response = rewrite.execute();
            listener.sent(status.getLength());
            // Include this field (from the previous rewrite response) on each rewrite request after the first one,
            // until the rewrite response 'done' flag is true.
            while(!rewrite.setRewriteToken(response.getRewriteToken()).execute().getDone()) {
                log.warn(String.format("Pending rewrite for object %s", storageObject));
            }
            final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                containerService.getContainer(target)
            ) : VersioningConfiguration.empty();
            return target.withAttributes(new GoogleStorageAttributesFinderFeature(session).toAttributes(response.getResource(), versioning));
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
