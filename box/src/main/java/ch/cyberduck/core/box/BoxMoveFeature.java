package ch.cyberduck.core.box;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.box.io.swagger.client.ApiException;
import ch.cyberduck.core.box.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.box.io.swagger.client.api.FoldersApi;
import ch.cyberduck.core.box.io.swagger.client.model.File;
import ch.cyberduck.core.box.io.swagger.client.model.FilesFileIdBody;
import ch.cyberduck.core.box.io.swagger.client.model.FilesfileIdParent;
import ch.cyberduck.core.box.io.swagger.client.model.Folder;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersFolderIdBody;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersfolderIdParent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

public class BoxMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(BoxMoveFeature.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxMoveFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path renamed, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            if(status.isExists()) {
                if(!fileid.getFileId(file).equals(fileid.getFileId(renamed))) {
                    log.warn("Delete file {} to be replaced with {}", renamed, file);
                    new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(renamed), callback, delete);
                }
            }
            final String id = fileid.getFileId(file);
            if(file.isDirectory()) {
                final Folder result = new FoldersApi(new BoxApiClient(session.getClient())).putFoldersId(
                        id, new FoldersFolderIdBody()
                                .name(renamed.getName())
                                .parent(new FoldersfolderIdParent()
                                        .id(fileid.getFileId(renamed.getParent()))),
                        null, BoxAttributesFinderFeature.DEFAULT_FIELDS);
                fileid.cache(file, null);
                fileid.cache(renamed, id);
                return renamed.withAttributes(new BoxAttributesFinderFeature(session, fileid).toAttributes(result));
            }
            final File result = new FilesApi(new BoxApiClient(session.getClient())).putFilesId(
                    id, new FilesFileIdBody()
                            .name(renamed.getName())
                            .parent(new FilesfileIdParent()
                                    .id(fileid.getFileId(renamed.getParent()))),
                    null, BoxAttributesFinderFeature.DEFAULT_FIELDS);
            fileid.cache(file, null);
            fileid.cache(renamed, id);
            return renamed.withAttributes(new BoxAttributesFinderFeature(session, fileid).toAttributes(result));
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(!BoxTouchFeature.validate(target.getName())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), target.getName())).withFile(source);
        }
    }
}
