package ch.cyberduck.core.deepbox;/*
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
import ch.cyberduck.core.LoginCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Restore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANREVERT;

public class DeepboxRestoreFeature implements Restore {
    private static final Logger log = LogManager.getLogger(DeepboxRestoreFeature.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxRestoreFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void restore(final Path file, final LoginCallback prompt) throws BackgroundException {
        final CoreRestControllerApi core = new CoreRestControllerApi(session.getClient());
        final String nodeId = fileid.getFileId(file);
        if(nodeId == null) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("Could not restore %s", file));
            }
            return;
        }
        try {
            core.revertNode(UUID.fromString(nodeId));
            this.fileid.cache(file, null);
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map(e);
        }
    }

    @Override
    public boolean isRestorable(final Path file) {
        try {
            if(new DeepboxPathContainerService().isInTrash(file)) {
                final Acl acl = file.attributes().getAcl();
                if(Acl.EMPTY == acl) {
                    // Missing initialization
                    log.warn(String.format("Unknown ACLs on %s", file));
                    return true;
                }
                return acl.get(new Acl.CanonicalUser()).contains(CANREVERT);
            }
        }
        catch(Exception e) {
            log.warn(e);
        }
        return false;
    }
}
