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
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.RewriteResponse;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageStorageClassFeature implements Redundancy {

    private final GoogleStorageSession session;
    private final PathContainerService containerService;

    public GoogleStorageStorageClassFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public String getDefault() {
        return new HostPreferences(session.getHost()).getProperty("googlestorage.storage.class");
    }

    @Override
    public Set<String> getClasses() {
        return new LinkedHashSet<>(
                PreferencesFactory.get().getList("googlestorage.storage.class.options"));
    }

    @Override
    public String getClass(final Path file) throws BackgroundException {
        return new GoogleStorageAttributesFinderFeature(session).find(file).getStorageClass();
    }

    @Override
    public void setClass(final Path file, final String redundancy) throws BackgroundException {
        try {
            if(containerService.isContainer(file)) {
                // Changing the default storage class of a bucket
                final Storage.Buckets.Patch request = session.getClient().buckets().patch(containerService.getContainer(file).getName(),
                        new Bucket().setStorageClass(redundancy));
                if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    request.setUserProject(session.getHost().getCredentials().getUsername());
                }
                request.execute();
            }
            else {
                final Storage.Objects.Rewrite request = session.getClient().objects().rewrite(containerService.getContainer(file).getName(),
                        containerService.getKey(file), containerService.getContainer(file).getName(), containerService.getKey(file),
                        new StorageObject().setStorageClass(redundancy)
                );
                if(containerService.getContainer(file).attributes().getCustom().containsKey(GoogleStorageAttributesFinderFeature.KEY_REQUESTER_PAYS)) {
                    request.setUserProject(session.getHost().getCredentials().getUsername());
                }
                final RewriteResponse response = request.execute();
                file.attributes().setVersionId(String.valueOf(response.getResource().getGeneration()));
            }
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to write attributes of {0}", e, file);
        }
    }
}
