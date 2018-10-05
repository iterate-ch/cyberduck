package ch.cyberduck.core.onedrive.features;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePatchOperation;
import org.nuxeo.onedrive.client.facets.FileSystemInfoFacet;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;

public class GraphMoveFeature implements Move {

    private final GraphSession session;
    private Delete delete;

    private final PathContainerService containerService
        = new PathContainerService();

    public GraphMoveFeature(final GraphSession session) {
        this.session = session;
        this.delete = new GraphDeleteFeature(session);
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        if(status.isExists()) {
            delete.delete(Collections.singletonList(renamed), connectionCallback, callback);
        }
        final OneDrivePatchOperation patchOperation = new OneDrivePatchOperation();
        if(!StringUtils.equals(file.getName(), renamed.getName())) {
            patchOperation.rename(renamed.getName());
        }
        if(!file.getParent().equals(renamed.getParent())) {
            final OneDriveFolder moveTarget = session.toFolder(renamed.getParent());
            patchOperation.move(moveTarget);
        }
        // Keep curent timestamp set
        final FileSystemInfoFacet info = new FileSystemInfoFacet();
        info.setLastModifiedDateTime(Instant.ofEpochMilli(file.attributes().getModificationDate()).atOffset(ZoneOffset.UTC));
        patchOperation.facet("fileSystemInfo", info);
        final OneDriveItem item = session.toItem(file);
        try {
            item.patch(patchOperation);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        return new Path(renamed.getParent(), renamed.getName(), renamed.getType(),
            new GraphAttributesFinderFeature(session).find(renamed));
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(containerService.isContainer(source)) {
            return false;
        }
        if(!containerService.getContainer(source).equals(containerService.getContainer(target))) {
            return false;
        }
        if(source.getType().contains(Path.Type.shared)) {
            return false;
        }
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }
}
