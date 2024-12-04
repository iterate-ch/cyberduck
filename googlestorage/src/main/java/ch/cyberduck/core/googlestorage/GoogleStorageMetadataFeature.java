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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Headers;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageMetadataFeature implements Headers {
    private static final Logger log = LogManager.getLogger(GoogleStorageMetadataFeature.class);

    private final GoogleStorageSession session;
    private final PathContainerService containerService;

    public GoogleStorageMetadataFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = new GoogleStoragePathContainerService();
    }

    @Override
    public Map<String, String> getDefault() {
        return new HostPreferences(session.getHost()).getMap("googlestorage.metadata.default");
    }

    @Override
    public Map<String, String> getMetadata(final Path file) throws BackgroundException {
        try {
            return new GoogleStorageAttributesFinderFeature(session).find(file).getMetadata();
        }
        catch(NotfoundException e) {
            if(file.isDirectory()) {
                // No placeholder file may exist but we just have a common prefix
                return Collections.emptyMap();
            }
            throw e;
        }
    }

    @Override
    public void setMetadata(final Path file, final TransferStatus status) throws BackgroundException {
        log.debug("Write metadata {} for file {}", status, file);
        try {
            final Storage.Objects.Patch request = session.getClient().objects().patch(containerService.getContainer(file).getName(), containerService.getKey(file),
                    new StorageObject().setMetadata(status.getMetadata()));
            if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                request.setUserProject(session.getHost().getCredentials().getUsername());
            }
            final StorageObject object = request.execute();
            status.setResponse(new GoogleStorageAttributesFinderFeature(session).toAttributes(object));
        }
        catch(IOException e) {
            final BackgroundException failure = new GoogleStorageExceptionMappingService().map("Failure to write attributes of {0}", e, file);
            if(file.isDirectory()) {
                if(failure instanceof NotfoundException) {
                    // No placeholder file may exist but we just have a common prefix
                    return;
                }
            }
            throw failure;
        }
    }
}
