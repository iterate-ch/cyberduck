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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.swagger.CopyNodesRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

public class SDSCopyFeature implements Copy {

    private final SDSSession session;

    private final PathContainerService containerService
            = new PathContainerService();

    public SDSCopyFeature(final SDSSession session) {
        this.session = session;
    }

    @Override
    public void copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            new NodesApi(session.getClient()).copyNodes(StringUtils.EMPTY,
                    // Target Parent Node ID
                    Long.parseLong(new SDSNodeIdProvider(session).getFileid(target.getParent(), new DisabledListProgressListener())),
                    new CopyNodesRequest()
                            .addNodeIdsItem(Long.parseLong(new SDSNodeIdProvider(session).getFileid(source, new DisabledListProgressListener())))
                            .resolutionStrategy(CopyNodesRequest.ResolutionStrategyEnum.OVERWRITE), null);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Cannot copy {0}", e, source);
        }
    }

    @Override
    public boolean isRecursive(final Path source, final Path target) {
        return true;
    }

    @Override
    public boolean isSupported(final Path source, final Path target) {
        if(containerService.isContainer(source)) {
            // Rooms cannot be copied
            return false;
        }
        if(containerService.getContainer(source).equals(containerService.getContainer(target))) {
            // Nodes must be in same source parent
            return true;
        }
        return false;
    }

    @Override
    public Copy withTarget(final Session<?> session) {
        return this;
    }
}
