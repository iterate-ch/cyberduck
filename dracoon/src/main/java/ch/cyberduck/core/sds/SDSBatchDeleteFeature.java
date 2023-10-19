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
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteDeletedNodesRequest;
import ch.cyberduck.core.sds.io.swagger.client.model.DeleteNodesRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SDSBatchDeleteFeature implements Delete {
    private static final Logger log = LogManager.getLogger(SDSBatchDeleteFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSBatchDeleteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final Map<Path, List<Long>> regular = new HashMap<>();
        final Map<Path, List<Long>> trashed = new HashMap<>();
        for(Path file : files.keySet()) {
            final Map<Path, List<Long>> set = file.attributes().isDuplicate() ? trashed : regular;
            if(set.containsKey(file.getParent())) {
                set.get(file.getParent()).add(Long.parseLong(nodeid.getVersionId(file)));
            }
            else {
                final List<Long> nodes = new ArrayList<>();
                nodes.add(Long.parseLong(nodeid.getVersionId(file)));
                set.put(file.getParent(), nodes);
            }
            callback.delete(file);
            nodeid.cache(file, null);
        }
        for(List<Long> nodes : regular.values()) {
            try {
                new NodesApi(session.getClient()).removeNodes(new DeleteNodesRequest().nodeIds(nodes), StringUtils.EMPTY);
            }
            catch(ApiException e) {
                switch(e.getCode()) {
                    case 400:
                        log.warn(String.format("Ignore failure %s", e));
                        new SDSDeleteFeature(session, nodeid).delete(files, prompt, callback);
                        break;
                    default:
                        throw new SDSExceptionMappingService(nodeid).map("Cannot delete {0}", e, files.keySet().iterator().next());
                }
            }
        }
        for(List<Long> nodes : trashed.values()) {
            try {
                new NodesApi(session.getClient()).removeDeletedNodes(new DeleteDeletedNodesRequest().deletedNodeIds(nodes), StringUtils.EMPTY);
            }
            catch(ApiException e) {
                throw new SDSExceptionMappingService(nodeid).map("Cannot delete {0}", e, files.keySet().iterator().next());
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
