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
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.PostChildrenForAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntries;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.EnumSet;

public class GmxcloudDirectoryFeature implements Directory<Void> {

    private final GmxcloudSession session;
    private final GmxcloudResourceIdProvider fileid;

    public GmxcloudDirectoryFeature(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path mkdir(final Path folder, final TransferStatus status) throws BackgroundException {
        try {
            final ResourceCreationRepresentationArrayInner resourceCreationRepresentation = new ResourceCreationRepresentationArrayInner();
            final String path = StringUtils.removeStart(folder.getAbsolute(), String.valueOf(Path.DELIMITER));
            resourceCreationRepresentation.setPath(path);
            resourceCreationRepresentation.setResourceType(ResourceCreationRepresentationArrayInner.ResourceTypeEnum.CONTAINER);
            final ResourceCreationResponseEntries resourceCreationResponseEntries = new PostChildrenForAliasApi(new GmxcloudApiClient(session)).resourceAliasAliasChildrenPost(
                    GmxcloudResourceIdProvider.ROOT, Collections.singletonList(resourceCreationRepresentation), null, null, null, null, null);
            if(!resourceCreationResponseEntries.containsKey(path)) {
                throw new NotfoundException(folder.getAbsolute());
            }
            final ResourceCreationResponseEntry resourceCreationResponseEntry = resourceCreationResponseEntries.get(path);
            fileid.cache(folder, GmxcloudResourceIdProvider.getResourceIdFromResourceUri(resourceCreationResponseEntry.getHeaders().getLocation()));
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
