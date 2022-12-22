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
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.CopyNode;
import ch.cyberduck.core.sds.io.swagger.client.model.CopyNodesRequest;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class SDSCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(SDSCopyFeature.class);

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSCopyFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Path copy(final Path source, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            new NodesApi(session.getClient()).copyNodes(
                new CopyNodesRequest()
                    .resolutionStrategy(CopyNodesRequest.ResolutionStrategyEnum.OVERWRITE)
                    .addItemsItem(new CopyNode().id(Long.parseLong(nodeid.getVersionId(source))))
                    .keepShareLinks(new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep")),
                // Target Parent Node ID
                Long.parseLong(nodeid.getVersionId(target.getParent())),
                StringUtils.EMPTY, null);
            listener.sent(status.getLength());
            nodeid.cache(target, null);
            final PathAttributes attributes = new SDSAttributesFinderFeature(session, nodeid).find(target);
            nodeid.cache(target, attributes.getVersionId());
            return target.withAttributes(attributes);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Cannot copy {0}", e, source);
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
        if(SDSNodeIdProvider.isEncrypted(source) ^ SDSNodeIdProvider.isEncrypted(target)) {
            // If source xor target is encrypted data room we cannot use server side copy
            log.warn(String.format("Cannot use server side copy with source container %s and target container %s",
                containerService.getContainer(source), containerService.getContainer(target)));
            return false;
        }
        if(!StringUtils.equals(source.getName(), target.getName())) {
            // Cannot rename node to be copied at the same time
            log.warn(String.format("Deny copy of %s for changed name %s", source, target.getName()));
            return false;
        }
        if(Objects.equals(source.getParent(), target.getParent())) {
            // Nodes must not have the same parent
            log.warn(String.format("Deny copy of %s to %s", source, target));
            return false;
        }
        return true;
    }
}
