package ch.cyberduck.core.storegate;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.storegate.io.swagger.client.ApiException;
import ch.cyberduck.core.storegate.io.swagger.client.api.FilesApi;
import ch.cyberduck.core.storegate.io.swagger.client.model.CreateFolderRequest;
import ch.cyberduck.core.storegate.io.swagger.client.model.File;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;

public class StoregateDirectoryFeature implements Directory<VersionId> {

    private final StoregateSession session;
    private final StoregateIdProvider fileid;

    public StoregateDirectoryFeature(final StoregateSession session, final StoregateIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final FilesApi files = new FilesApi(session.getClient());
            final CreateFolderRequest request = new CreateFolderRequest();
            request.setName(folder.getName());
            request.setParentID(fileid.getFileId(folder.getParent()));
            final File f = files.filesCreateFolder(request);
            fileid.cache(folder, f.getId());
            return folder.withAttributes(new StoregateAttributesFinderFeature(session, fileid).toAttributes(f));
        }
        catch(ApiException e) {
            throw new StoregateExceptionMappingService(fileid).map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot()) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename)).withFile(workdir);
        }
    }
}
