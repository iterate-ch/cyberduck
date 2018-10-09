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
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveCopyOperation;
import org.nuxeo.onedrive.client.OneDriveFolder;
import org.nuxeo.onedrive.client.OneDriveItem;

import java.io.IOException;
import java.util.Collections;

public class GraphCopyFeature implements Copy {
    private static final Logger logger = Logger.getLogger(GraphCopyFeature.class);

    private final GraphSession session;

    private final PathContainerService containerService
        = new PathContainerService();

    public GraphCopyFeature(final GraphSession session) {
        this.session = session;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final OneDriveCopyOperation copyOperation = new OneDriveCopyOperation();
        if(!StringUtils.equals(source.getName(), target.getName())) {
            copyOperation.rename(target.getName());
        }
        if(status.isExists()) {
            new GraphDeleteFeature(session).delete(Collections.singletonList(target), callback, new Delete.DisabledCallback());
        }

        final OneDriveFolder targetItem = session.toFolder(target.getParent());
        copyOperation.copy(targetItem);
        final OneDriveItem item = session.toItem(source);
        try {
            item.copy(copyOperation).await(statusObject -> logger.info(String.format("Copy Progress Operation %s progress %f status %s",
                statusObject.getOperation(),
                statusObject.getPercentage(),
                statusObject.getStatus())));
            return target;
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService().map("Cannot copy {0}", e, source);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, source);
        }
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
}
