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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANDELETE;

public class DeepboxDeleteFeature implements Delete {

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxDeleteFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Map.Entry<Path, TransferStatus> file : files.entrySet()) {
            try {
                final String fileId = fileid.getFileId(file.getKey());
                if(fileId == null) {
                    throw new NotfoundException(String.format("Cannot delete %s", file));
                }
                final UUID nodeId = UUID.fromString(fileId);
                callback.delete(file.getKey());
                final CoreRestControllerApi coreApi = new CoreRestControllerApi(session.getClient());
                coreApi.deletePurgeNode(nodeId, false);
                fileid.cache(file.getKey(), null);
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map(e);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }

    @Override
    public void preflight(Path file) throws BackgroundException {
        final Acl acl = file.attributes().getAcl();
        if(!acl.get(new Acl.CanonicalUser()).contains(CANDELETE)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("ACL %s for %s does not include %s", acl, file, CANDELETE));
            }
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), file.getName())).withFile(file);
        }
    }
}
