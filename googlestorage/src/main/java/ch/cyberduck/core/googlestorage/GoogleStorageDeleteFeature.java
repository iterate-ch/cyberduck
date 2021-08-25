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
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Map;

import com.google.api.services.storage.Storage;

public class GoogleStorageDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(GoogleStorageDeleteFeature.class);

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
                    session.getClient().buckets().delete(file.getName()).execute();
                }
                else if(file.isPlaceholder()) {
                    log.warn(String.format("Do not attempt to delete placeholder %s", file));
                }
                else {
                    final Storage.Objects.Delete request = session.getClient().objects().delete(containerService.getContainer(file).getName(), containerService.getKey(file));
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
                throw new GoogleStorageExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }
}
