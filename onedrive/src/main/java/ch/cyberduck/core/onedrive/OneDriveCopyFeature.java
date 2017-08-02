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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.log4j.Logger;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.OneDriveCopyOperation;

import java.io.IOException;

public class OneDriveCopyFeature implements Copy {
    private static final Logger logger = Logger.getLogger(OneDriveCopyFeature.class);
    private final OneDriveSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    public OneDriveCopyFeature(OneDriveSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        final OneDriveCopyOperation copyOperation = new OneDriveCopyOperation();
        if(!StringUtils.equals(source.getName(), target.getName())) {
            copyOperation.rename(target.getName());
        }
        copyOperation.copy(session.toFolder(target.getParent()));
        try {
            session.toFile(source).copy(copyOperation).await(statusObject -> logger.info(
                    String.format("Copy Progress Operation %s progress %f status %s",
                            statusObject.getOperation(),
                            statusObject.getPercentage(),
                            statusObject.getStatus())));
        }
        catch(OneDriveAPIException e) {
            throw new OneDriveExceptionMappingService().map("Cannot copy {0}", e, source);
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
        return true;
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        return this;
    }
}
