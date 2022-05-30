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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.CopyOperation;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.types.DriveItem;

import java.io.IOException;
import java.util.Collections;

public class GraphCopyFeature implements Copy {
    private static final Logger logger = LogManager.getLogger(GraphCopyFeature.class);

    private final GraphSession session;
    private final GraphAttributesFinderFeature attributes;
    private final GraphFileIdProvider fileid;

    public GraphCopyFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.attributes = new GraphAttributesFinderFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path file, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        final CopyOperation copyOperation = new CopyOperation();
        if(!StringUtils.equals(file.getName(), target.getName())) {
            copyOperation.rename(target.getName());
        }
        if(status.isExists()) {
            if(logger.isWarnEnabled()) {
                logger.warn(String.format("Delete file %s to be replaced with %s", target, file));
            }
            new GraphDeleteFeature(session, fileid).delete(Collections.singletonMap(target, status), callback, new Delete.DisabledCallback());
        }
        final DriveItem targetItem = session.getItem(target.getParent());
        copyOperation.copy(targetItem);
        final DriveItem item = session.getItem(file);
        try {
            Files.copy(item, copyOperation).await(statusObject -> logger.info(String.format("Copy Progress Operation %s progress %f status %s",
                statusObject.getOperation(),
                statusObject.getPercentage(),
                statusObject.getStatus())));
            listener.sent(status.getLength());
            target.attributes().setFileId(null);
            final PathAttributes attr = attributes.find(target);
            fileid.cache(target, attr.getFileId());
            return target.withAttributes(attr);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Cannot copy {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot copy {0}", e, file);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(!session.isAccessible(target, true)) {
            return false;
        }
        if(!session.isAccessible(source, false)) {
            return false;
        }
        if(!session.getContainer(source).equals(session.getContainer(target))) {
            return false;
        }
        return !source.getType().contains(Path.Type.shared);
    }
}
