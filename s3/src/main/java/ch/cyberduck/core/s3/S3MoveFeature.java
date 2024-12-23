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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.jets3t.service.ServiceException;
import org.jets3t.service.VersionOrDeleteMarkersChunk;
import org.jets3t.service.model.BaseVersionOrDeleteMarker;

import java.util.Collections;
import java.util.Optional;

import static ch.cyberduck.core.s3.S3VersionedObjectListService.KEY_DELETE_MARKER;

public class S3MoveFeature implements Move {

    private final S3Session session;
    private final PathContainerService containerService;
    private final S3ThresholdCopyFeature proxy;
    private final S3DefaultDeleteFeature delete;

    public S3MoveFeature(final S3Session session, final S3AccessControlListFeature acl) {
        this.session = session;
        this.containerService = session.getFeature(PathContainerService.class);
        this.proxy = new S3ThresholdCopyFeature(session, acl);
        this.delete = new S3DefaultDeleteFeature(session, acl);
    }

    @Override
    public Path move(final Path source, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        Path target;
        if(source.attributes().getCustom().containsKey(KEY_DELETE_MARKER)) {
            // Delete marker, copy not supported but we have to retain the delete marker at the target
            target = new Path(renamed);
            target.attributes().setVersionId(null);
            delete.delete(Collections.singletonMap(target, status), connectionCallback, callback);
            try {
                // Find version id of moved delete marker
                final Path bucket = containerService.getContainer(renamed);
                final VersionOrDeleteMarkersChunk marker = session.getClient().listVersionedObjectsChunked(
                        bucket.isRoot() ? StringUtils.EMPTY : bucket.getName(), containerService.getKey(renamed),
                        String.valueOf(Path.DELIMITER), 1, null, null, false);
                if(marker.getItems().length == 1) {
                    final BaseVersionOrDeleteMarker markerObject = marker.getItems()[0];
                    target.attributes().withVersionId(markerObject.getVersionId()).setCustom(Collections.singletonMap(KEY_DELETE_MARKER, Boolean.TRUE.toString()));
                    delete.delete(Collections.singletonMap(source, status), connectionCallback, callback);
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
                target = proxy.copy(source, renamed, status.withLength(source.attributes().getSize()), connectionCallback, new DisabledStreamListener());
                // Copy source path and nullify version id to add a delete marker
                delete.delete(Collections.singletonMap(new Path(source).withAttributes(new PathAttributes(source.attributes()).withVersionId(null)), status),
                        connectionCallback, callback);
            }
            catch(NotfoundException e) {
                if(source.getType().contains(Path.Type.placeholder)) {
                    // No placeholder object to copy, create a new one at the target
                    target = session.getFeature(Directory.class).mkdir(renamed, new TransferStatus().withRegion(source.attributes().getRegion()));
                }
                else {
                    throw e;
                }
            }
        }
        return target;
    }

    @Override
    public void preflight(final Path source, final Optional<Path> target) throws BackgroundException {
        proxy.preflight(source, target);
        delete.preflight(source);
    }
}
