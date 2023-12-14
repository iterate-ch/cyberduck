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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Map;

import com.google.api.services.storage.Storage;

public class GoogleStorageDeleteFeature implements Delete {

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    public GoogleStorageDeleteFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            try {
                callback.delete(file);
                if(containerService.isContainer(file)) {
                    final Storage.Buckets.Delete request = session.getClient().buckets().delete(file.getName());
                    if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                        request.setUserProject(session.getHost().getCredentials().getUsername());
                    }
                    request.execute();
                }
                else {
                    final Storage.Objects.Delete request = session.getClient().objects().delete(containerService.getContainer(file).getName(), containerService.getKey(file));
                    if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                        request.setUserProject(session.getHost().getCredentials().getUsername());
                    }
                    final VersioningConfiguration versioning = null != session.getFeature(Versioning.class) ? session.getFeature(Versioning.class).getConfiguration(
                            containerService.getContainer(file)
                    ) : VersioningConfiguration.empty();
                    if(versioning.isEnabled()) {
                        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
                            // You permanently delete versions of objects by including the generation number in the deletion request
                            request.setGeneration(Long.parseLong(file.attributes().getVersionId()));
                        }
                    }
                    request.execute();
                }
            }
            catch(IOException e) {
                final BackgroundException failure = new GoogleStorageExceptionMappingService().map("Cannot delete {0}", e, file);
                if(file.isDirectory()) {
                    if(failure instanceof NotfoundException) {
                        // No placeholder file may exist but we just have a common prefix
                        continue;
                    }
                }
                throw failure;
            }
        }
    }
}
