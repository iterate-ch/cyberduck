package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
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
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;
import org.jets3t.service.ServiceException;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;

import java.util.Collections;

import static ch.cyberduck.core.s3.S3VersionedObjectListService.KEY_DELETE_MARKER;

public class S3MoveFeature implements Move {
    private static final Logger log = Logger.getLogger(S3MoveFeature.class);

    private final PathContainerService containerService
        = new S3PathContainerService();

    private final S3Session session;
    private final S3AccessControlListFeature accessControlListFeature;

    private Delete delete;

    public S3MoveFeature(final S3Session session) {
        this(session, new S3AccessControlListFeature(session));
    }

    public S3MoveFeature(final S3Session session, final S3AccessControlListFeature accessControlListFeature) {
        this.session = session;
        this.accessControlListFeature = accessControlListFeature;
        this.delete = new S3DefaultDeleteFeature(session);
    }

    @Override
    public Path move(final Path source, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        Path copy;
        if(source.attributes().getCustom().containsKey(KEY_DELETE_MARKER)) {
            // Delete marker, copy not supported but we have to retain the delete marker at the target
            renamed.attributes().setVersionId(null);
            delete.delete(Collections.singletonList(renamed), connectionCallback, callback);
            try {
                // Find version id of moved delete marker
                final VersionOrDeleteMarkersChunk marker = session.getClient().listVersionedObjectsChunked(containerService.getContainer(renamed).getName(), containerService.getKey(renamed),
                    String.valueOf(Path.DELIMITER), 1, null, null, false);
                if(marker.getItems().length == 1) {
                    final BaseVersionOrDeleteMarker markerObject = marker.getItems()[0];
                    renamed.attributes().withVersionId(markerObject.getVersionId()).setCustom(Collections.singletonMap(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
                    copy = new Path(renamed.getParent(), renamed.getName(), renamed.getType(), renamed.attributes());
                    delete.delete(Collections.singletonList(source), connectionCallback, callback);
                }
                else {
                    throw new NotfoundException(String.format("Unable to find delete marker %s", renamed.getName()));
                }
            }
            catch(ServiceException e) {
                throw new S3ExceptionMappingService().map("Failure to read attributes of {0}", e, renamed);
            }
        }
        else {
            try {
                copy = new S3ThresholdCopyFeature(session, accessControlListFeature).copy(source, renamed, status.length(source.attributes().getSize()), connectionCallback);
                delete.delete(Collections.singletonList(source), connectionCallback, callback);
            }
            catch(NotfoundException e) {
                if(source.getType().contains(Path.Type.placeholder)) {
                    // No placeholder object to copy, create a new one at the target
                    copy = session.getFeature(Directory.class).mkdir(renamed, renamed.attributes().getRegion(), new TransferStatus());
                }
                else {
                    throw e;
                }
            }
        }
        return copy;
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return false;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        return !containerService.isContainer(source);
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }

}
