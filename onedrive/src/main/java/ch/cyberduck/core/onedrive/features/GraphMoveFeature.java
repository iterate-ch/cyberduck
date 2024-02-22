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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.onedrive.GraphExceptionMappingService;
import ch.cyberduck.core.onedrive.GraphSession;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.onedrive.client.Files;
import org.nuxeo.onedrive.client.OneDriveAPIException;
import org.nuxeo.onedrive.client.PatchOperation;
import org.nuxeo.onedrive.client.types.DriveItem;
import org.nuxeo.onedrive.client.types.FileSystemInfo;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.EnumSet;

public class GraphMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(GraphMoveFeature.class);

    private final GraphSession session;
    private final Delete delete;
    private final GraphFileIdProvider fileid;

    public GraphMoveFeature(final GraphSession session, final GraphFileIdProvider fileid) {
        this.session = session;
        this.delete = new GraphDeleteFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback callback, final ConnectionCallback connectionCallback) throws BackgroundException {
        if(status.isExists()) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Delete file %s to be replaced with %s", renamed, file));
            }
            delete.delete(Collections.singletonMap(renamed, status), connectionCallback, callback);
        }
        final PatchOperation patchOperation = new PatchOperation();
        if(!StringUtils.equals(file.getName(), renamed.getName())) {
            patchOperation.rename(renamed.getName());
        }
        if(!new SimplePathPredicate(file.getParent()).test(renamed.getParent())) {
            final DriveItem moveTarget = session.getItem(renamed.getParent());
            patchOperation.move(moveTarget);
        }
        // Keep current timestamp set
        final FileSystemInfo info = new FileSystemInfo();
        info.setLastModifiedDateTime(Instant.ofEpochMilli(file.attributes().getModificationDate()).atOffset(ZoneOffset.UTC));
        patchOperation.facet("fileSystemInfo", info);
        final DriveItem item = session.getItem(file);
        try {
            final DriveItem.Metadata metadata = Files.patch(item, patchOperation);
            final PathAttributes attributes = new GraphAttributesFinderFeature(session, fileid).toAttributes(metadata);
            fileid.cache(file, null);
            fileid.cache(renamed, attributes.getFileId());
            return renamed.withAttributes(attributes);
        }
        catch(OneDriveAPIException e) {
            throw new GraphExceptionMappingService(fileid).map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(!session.isAccessible(target, true)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(!session.isAccessible(source, false)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(!session.getContainer(source).equals(session.getContainer(target))) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(source.getType().contains(Path.Type.shared)) {
            throw new UnsupportedException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
    }
}
