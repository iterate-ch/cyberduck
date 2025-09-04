package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.PathRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Folder;
import ch.cyberduck.core.deepbox.io.swagger.client.model.FolderAdded;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class DeepboxDirectoryFeature implements Directory<Node> {
    private static final Logger log = LogManager.getLogger(DeepboxDirectoryFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final DeepboxPathContainerService containerService;

    public DeepboxDirectoryFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.containerService = new DeepboxPathContainerService(session, fileid);
    }

    @Override
    public Path mkdir(final Write<Node> writer, final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            if(new DeepboxFindFeature(session, fileid).find(folder)) {
                throw new ConflictException(folder.getAbsolute());
            }
            final Folder upload = new Folder();
            upload.setName(folder.getName());
            upload.setI18n(Collections.emptyMap());
            final List<Folder> body = Collections.singletonList(upload);
            final String deepBoxNodeId = fileid.getDeepBoxNodeId(folder.getParent());
            final String boxNodeId = fileid.getBoxNodeId(folder.getParent());
            final List<FolderAdded> created;
            if(containerService.isDocuments(folder.getParent())) {
                created = new PathRestControllerApi(session.getClient()).addFolders1(
                        body,
                        deepBoxNodeId,
                        boxNodeId
                );
            }
            else {
                final String parentNodeId = fileid.getFileId(folder.getParent());
                created = new PathRestControllerApi(session.getClient()).addFolders(
                        body,
                        deepBoxNodeId,
                        boxNodeId,
                        parentNodeId
                );
            }
            final FolderAdded f = created.stream().findFirst().orElse(null);
            if(f != null) {
                fileid.cache(folder, f.getNode().getNodeId());
            }
            return new Path(folder).withAttributes(new DeepboxAttributesFinderFeature(session, fileid).toAttributes(f.getNode()));
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(containerService.isInbox(workdir)) {
            throw new AccessDeniedException(LocaleFactory.localizedString("Adding folders is not permitted in the inbox", "Deepbox")).withFile(workdir);
        }
        // Same checks as for new file
        new DeepboxTouchFeature(session, fileid).preflight(workdir, filename);
    }
}
