package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteDeletedNodesRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;

public class SDSDeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(SDSDeleteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSDeleteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        for(Path file : files.keySet()) {
            callback.delete(file);
            try {
                if(file.attributes().isDuplicate()) {
                    // Already trashed
                    log.warn("Delete file {} already in trash", file);
                    new NodesApi(session.getClient()).removeDeletedNodes(new DeleteDeletedNodesRequest().deletedNodeIds(Collections.singletonList(
                            Long.parseLong(nodeid.getVersionId(file)))), StringUtils.EMPTY);
                }
                else if(file.attributes().getVerdict() == PathAttributes.Verdict.malicious) {
                    // Delete malicious file
                    log.warn("Delete file {} marked as malicious", file);
                    new NodesApi(session.getClient()).removeMaliciousFile(
                            Long.parseLong(nodeid.getVersionId(file)), StringUtils.EMPTY);
                }
                else {
                    new NodesApi(session.getClient()).removeNode(
                            Long.parseLong(nodeid.getVersionId(file)), StringUtils.EMPTY);
                }
                nodeid.cache(file, null);
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService(nodeid).map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features() {
        return EnumSet.of(Flags.recursive);
    }
}
