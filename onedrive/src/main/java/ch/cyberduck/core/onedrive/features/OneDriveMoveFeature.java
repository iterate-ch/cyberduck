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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.onedrive.OneDriveExceptionMappingService;
import ch.cyberduck.core.onedrive.OneDriveSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;
import org.nuxeo.onedrive.client.OneDrivePatchOperation;

import java.io.IOException;
import java.util.Collections;

public class OneDriveMoveFeature implements Move {

    private final OneDriveSession session;
    private Delete delete;

    private final PathContainerService containerService
        = new PathContainerService();

    public OneDriveMoveFeature(OneDriveSession session) {
        this.session = session;
        this.delete = new OneDriveDeleteFeature(session);
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
        final OneDriveItem item = session.toItem(file);
        try {
            item.patch(patchOperation);
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        return renamed;
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
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }
}
