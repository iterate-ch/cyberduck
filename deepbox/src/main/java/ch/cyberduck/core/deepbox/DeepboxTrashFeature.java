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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Map;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANDELETE;
import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANPURGE;

public class DeepboxTrashFeature implements Trash {

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxTrashFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        this.trash(files, callback, false);
    }

    // Move to trash unless already in trash or forcePurge=true
    protected void trash(final Map<Path, TransferStatus> files, final Callback callback, final boolean forcePurge) throws BackgroundException {
        for(Map.Entry<Path, TransferStatus> entry : files.entrySet()) {
            final Path file = entry.getKey();
            try {
                final String fileId = fileid.getFileId(file);
                callback.delete(file);
                final boolean inTrash = new DeepboxPathContainerService(session, fileid).isInTrash(file);
                // Purge if in trash
                new CoreRestControllerApi(session.getClient()).deletePurgeNode(fileId, inTrash || forcePurge);
                fileid.cache(file, null);
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(Path file) throws BackgroundException {
        if(file.isRoot() || new DeepboxPathContainerService(session, fileid).isContainer(file)) {
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file.getName())).withFile(file);
        }
        final Acl acl = file.attributes().getAcl();
        if(Acl.EMPTY == acl) {
            // Missing initialization
            log.warn("Unknown ACLs on {}", file);
            return;
        }
        if(new DeepboxPathContainerService(session, fileid).isInTrash(file)) {
            if(!acl.get(new Acl.CanonicalUser()).contains(CANPURGE)) {
                log.warn("ACL {} for {} does not include {}", acl, file, CANPURGE);
                throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file.getName())).withFile(file);
            }
        }
        else if(!acl.get(new Acl.CanonicalUser()).contains(CANDELETE)) {
            log.warn("ACL {} for {} does not include {}", acl, file, CANDELETE);
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot delete {0}", "Error"), file.getName())).withFile(file);
        }
    }
}
