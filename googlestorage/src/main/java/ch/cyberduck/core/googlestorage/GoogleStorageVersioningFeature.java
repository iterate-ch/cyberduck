package ch.cyberduck.core.googlestorage;/*
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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledConnectionCallback;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.cache.LRUCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import java.io.IOException;

import com.google.api.services.storage.model.Bucket;

public class GoogleStorageVersioningFeature implements Versioning {

    private final PathContainerService containerService;
    private final GoogleStorageSession session;
    private final LRUCache<Path, VersioningConfiguration> cache = LRUCache.build(10);

    public GoogleStorageVersioningFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path file) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        if(container.isRoot()) {
            return VersioningConfiguration.empty();
        }
        if(cache.contains(container)) {
            return cache.get(container);
        }
        try {
            final Bucket.Versioning versioning = session.getClient().buckets().get(container.getName()).execute().getVersioning();
            final VersioningConfiguration configuration = new VersioningConfiguration(versioning != null && versioning.getEnabled());
            cache.put(container, configuration);
            return configuration;
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to read attributes of {0}", e, container);
        }
    }

    @Override
    public void setConfiguration(final Path file, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        final Path container = containerService.getContainer(file);
        try {
            session.getClient().buckets().patch(container.getName(),
                new Bucket().setVersioning(new Bucket.Versioning().setEnabled(configuration.isEnabled()))).execute().getVersioning();
            cache.remove(container);
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Failure to write attributes of {0}", e, container);
        }
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        new GoogleStorageCopyFeature(session).copy(file, file, new TransferStatus(), new DisabledConnectionCallback(), new DisabledStreamListener());
    }

    @Override
    public boolean isRevertable(final Path file) {
        return file.attributes().isDuplicate();
    }

    @Override
    public Credentials getToken(final String mfaSerial, final PasswordCallback callback) throws ConnectionCanceledException {
        return null;
    }
}
