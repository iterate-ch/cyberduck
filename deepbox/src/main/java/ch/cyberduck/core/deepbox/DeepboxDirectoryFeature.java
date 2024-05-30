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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersionId;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.PathRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Folder;
import ch.cyberduck.core.deepbox.io.swagger.client.model.FolderAdded;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANADDCHILDREN;

public class DeepboxDirectoryFeature implements Directory<VersionId> {
    private static final Logger log = LogManager.getLogger(DeepboxDirectoryFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxDirectoryFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final Folder upload = new Folder();
            upload.setName(folder.getName());
            upload.setI18n(Collections.emptyMap());
            final List<Folder> body = Collections.singletonList(upload);
            final String deepBoxNodeId = fileid.getDeepBoxNodeId(folder.getParent());
            if(deepBoxNodeId == null) {
                throw new NotfoundException(String.format("Cannot find deep box node id for parent %s", folder.getName()));
            }
            final String boxNodeId = fileid.getBoxNodeId(folder.getParent());
            if(boxNodeId == null) {
                throw new NotfoundException(String.format("Cannot find box node id for parent %s", folder.getName()));
            }
            final String nodeId = fileid.getFileId(folder);
            if(nodeId != null) {
                return folder;
            }
            final PathRestControllerApi pathApi = new PathRestControllerApi(session.getClient());
            final List<FolderAdded> created;
            if(new DeepboxPathContainerService().isDocuments(folder.getParent())) {
                created = pathApi.addFolders1(
                        body,
                        UUID.fromString(deepBoxNodeId),
                        UUID.fromString(boxNodeId)
                );
            }
            else {
                final String parentNodeId = fileid.getFileId(folder.getParent());
                if(parentNodeId == null) {
                    throw new NotfoundException(String.format("Cannot find box node id for parent %s", folder.getName()));
                }
                created = pathApi.addFolders(
                        body,
                        UUID.fromString(deepBoxNodeId),
                        UUID.fromString(boxNodeId),
                        UUID.fromString(parentNodeId)
                );
            }
            final FolderAdded f = created.stream().findFirst().orElse(null);
            if(f != null) {
                fileid.cache(folder, f.getNode().getNodeId().toString());
            }
            return folder.withAttributes(new DeepboxAttributesFinderFeature(session, fileid).toAttributes(f.getNode()));
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public Directory<VersionId> withWriter(final Write<VersionId> writer) {
        return this;
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(workdir.isRoot() || (new DeepboxPathContainerService().isContainer(workdir) && !new DeepboxPathContainerService().isDocuments(workdir))) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename)).withFile(workdir);
        }
        final Acl acl = workdir.attributes().getAcl();
        if(Acl.EMPTY == acl) {
            // Missing initialization
            log.warn(String.format("Unknown ACLs on %s", workdir));
            return;
        }
        if(!acl.get(new Acl.CanonicalUser()).contains(CANADDCHILDREN)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("ACL %s for %s does not include %s", acl, workdir, CANADDCHILDREN));
            }
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename)).withFile(workdir);
        }
    }
}


