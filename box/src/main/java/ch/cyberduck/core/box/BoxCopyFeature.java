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
import ch.cyberduck.core.box.io.swagger.client.model.FileIdCopyBody;
import ch.cyberduck.core.box.io.swagger.client.model.FilesfileIdcopyParent;
import ch.cyberduck.core.box.io.swagger.client.model.FolderIdCopyBody;
import ch.cyberduck.core.box.io.swagger.client.model.FoldersfolderIdcopyParent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

import static ch.cyberduck.core.features.Copy.validate;

public class BoxCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(BoxCopyFeature.class);

    private final BoxSession session;
    private final BoxFileidProvider fileid;

    public BoxCopyFeature(final BoxSession session, final BoxFileidProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path file, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            if(status.isExists()) {
                log.warn("Delete file {} to be replaced with {}", target, file);
                new BoxDeleteFeature(session, fileid).delete(Collections.singletonList(target), callback, new Delete.DisabledCallback());
            }
            if(file.isDirectory()) {
                return target.withAttributes(new BoxAttributesFinderFeature(session, fileid).toAttributes(
                        new FoldersApi(new BoxApiClient(session.getClient())).postFoldersIdCopy(
                                fileid.getFileId(file),
                                new FolderIdCopyBody().name(target.getName()).parent(new FoldersfolderIdcopyParent().id(fileid.getFileId(target.getParent()))),
                                BoxAttributesFinderFeature.DEFAULT_FIELDS)
                ));
            }
            return target.withAttributes(new BoxAttributesFinderFeature(session, fileid).toAttributes(
                    new FilesApi(new BoxApiClient(session.getClient())).postFilesIdCopy(
                            fileid.getFileId(file),
                            new FileIdCopyBody()
                                    .name(target.getName())
                                    .parent(new FilesfileIdcopyParent().id(fileid.getFileId(target.getParent()))),
                            null, BoxAttributesFinderFeature.DEFAULT_FIELDS)
            ));
        }
        catch(ApiException e) {
            throw new BoxExceptionMappingService(fileid).map("Cannot copy {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(final Path source, final Path target) throws BackgroundException {
        if(!BoxTouchFeature.validate(target.getName())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), target.getName()));
        }
        validate(session.getCaseSensitivity(), source, target);
    }
}
