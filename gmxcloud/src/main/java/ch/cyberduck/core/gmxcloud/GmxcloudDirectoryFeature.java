package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.PostChildrenForAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntries;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class GmxcloudDirectoryFeature implements Directory<Void> {
    private static final String ROOT = "ROOT";
    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;

    public GmxcloudDirectoryFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final GmxcloudApiClient gmxcloudApiClient = new GmxcloudApiClient(session);
            List<ResourceCreationRepresentationArrayInner> resourceCreationRepresentationArrayInners = new ArrayList<>();
            ResourceCreationRepresentationArrayInner resourceCreationRepresentationArrayInner = new ResourceCreationRepresentationArrayInner();
            final String folderPath = StringUtils.removeStart(folder.getAbsolute(), String.valueOf(Path.DELIMITER));
            resourceCreationRepresentationArrayInner.setPath(folderPath);
            resourceCreationRepresentationArrayInner.setResourceType(ResourceCreationRepresentationArrayInner.ResourceTypeEnum.CONTAINER);
            resourceCreationRepresentationArrayInners.add(resourceCreationRepresentationArrayInner);
            final PostChildrenForAliasApi postResourceAliasApi = new PostChildrenForAliasApi(gmxcloudApiClient);
            ResourceCreationResponseEntries resourceCreationResponseEntries = postResourceAliasApi.resourceAliasAliasChildrenPost(ROOT, resourceCreationRepresentationArrayInners, null, null, null, null, null);
            ResourceCreationResponseEntry resourceCreationResponseEntry = resourceCreationResponseEntries.get(folderPath);
            fileid.cache(folder, Util.getResourceIdFromResourceUri(resourceCreationResponseEntry.getHeaders().getLocation()));
            return new Path(folder.getAbsolute(), EnumSet.of(Path.Type.directory),
                new GmxcloudAttributesFinderFeature(session, fileid).find(folder, new DisabledListProgressListener()));
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public Directory<Void> withWriter(final Write<Void> writer) {
        return this;
    }
}
