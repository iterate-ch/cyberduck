package ch.cyberduck.core.onedrive;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;

import org.apache.commons.codec.binary.StringUtils;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDrivePatchOperation;

import java.io.IOException;

public class OneDriveMoveFeature implements Move {

    private final OneDriveSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    public OneDriveMoveFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void move(final Path file, final Path renamed, final boolean exists, final Delete.Callback callback) throws BackgroundException {
        final OneDrivePatchOperation patchOperation = new OneDrivePatchOperation();
        if(!StringUtils.equals(file.getName(), renamed.getName())) {
            patchOperation.rename(renamed.getName());
        }
        if(file.getParent() != renamed.getParent()) {
            patchOperation.move(session.toFolder(renamed.getParent()));
        }
        try {
            session.toFile(file).patch(patchOperation);
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(source.isRoot() || source.getParent().isRoot()) {
            return false;
        }
        if(target.isRoot() || target.getParent().isRoot()) {
            return false;
        }
        final String sourceContainer = containerService.getContainer(source).getName();
        final String targetContainer = containerService.getContainer(source).getName();
        if(!StringUtils.equals(sourceContainer, targetContainer)) {
            return false;
        }
        return true;
    }

    @Override
    public Move withDelete(final Delete delete) {
        return this;
    }

    @Override
    public Move withList(final ListService list) {
        return this;
    }
}
