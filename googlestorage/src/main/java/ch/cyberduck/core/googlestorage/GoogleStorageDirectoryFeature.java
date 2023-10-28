package ch.cyberduck.core.googlestorage;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
 * http://cyberduck.ch/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.utils.ServiceUtils;

import java.io.IOException;
import java.util.EnumSet;

import com.google.api.services.storage.Storage;
import com.google.api.services.storage.model.Bucket;
import com.google.api.services.storage.model.StorageObject;

public class GoogleStorageDirectoryFeature implements Directory<StorageObject> {

    private static final String MIMETYPE = "application/x-directory";

    private final PathContainerService containerService;
    private final GoogleStorageSession session;

    private Write<StorageObject> writer;

    public GoogleStorageDirectoryFeature(final GoogleStorageSession session) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.writer = new GoogleStorageWriteFeature(session);
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(containerService.isContainer(folder)) {
                final Storage.Buckets.Insert request = session.getClient().buckets().insert(session.getHost().getCredentials().getUsername(),
                        new Bucket()
                                .setLocation(status.getRegion())
                                .setStorageClass(status.getStorageClass())
                                .setName(containerService.getContainer(folder).getName()));
                final Bucket bucket = request.execute();
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.volume);
                return folder.withType(type).withAttributes(new GoogleStorageAttributesFinderFeature(session).toAttributes(bucket));
            }
            else {
                final EnumSet<Path.Type> type = EnumSet.copyOf(folder.getType());
                type.add(Path.Type.placeholder);
                // Add placeholder object
                return new GoogleStorageTouchFeature(session).withWriter(writer).touch(folder.withType(type),
                        status.withMime(MIMETYPE));
            }
        }
        catch(IOException e) {
            throw new GoogleStorageExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            if(StringUtils.isNotBlank(filename)) {
                if(StringUtils.startsWith(filename, "goog")) {
                    throw new InvalidFilenameException();
                }
                if(StringUtils.contains(filename, "google")) {
                    throw new InvalidFilenameException();
                }
                if(!ServiceUtils.isBucketNameValidDNSName(filename)) {
                    throw new InvalidFilenameException();
                }
            }
        }
    }

    @Override
    public Directory<StorageObject> withWriter(final Write<StorageObject> writer) {
        this.writer = writer;
        return this;
    }
}
