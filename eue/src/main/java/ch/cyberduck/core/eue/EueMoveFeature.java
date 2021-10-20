package ch.cyberduck.core.eue;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.api.MoveChildrenForAliasApiApi;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.MoveChildrenApi;
import ch.cyberduck.core.eue.io.swagger.client.api.UpdateResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntryEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModel;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModelUpdate;
import ch.cyberduck.core.eue.io.swagger.client.model.Uifs;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.Collections;

public class EueMoveFeature implements Move {
    private static final Logger log = Logger.getLogger(EueMoveFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueMoveFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path move(final Path file, final Path target, final TransferStatus status, final Delete.Callback delete, final ConnectionCallback callback) throws BackgroundException {
        try {
            final EueApiClient client = new EueApiClient(session);
            final String resourceId = fileid.getFileId(file, new DisabledListProgressListener());
            if(!file.getParent().equals(target.getParent())) {
                final ResourceMoveResponseEntries resourceMoveResponseEntries;
                if((target.getParent().isRoot() || target.getParent().isPlaceholder())) {
                    resourceMoveResponseEntries = new MoveChildrenForAliasApiApi(client)
                            .resourceAliasAliasChildrenMovePost(fileid.getFileId(target.getParent(), new DisabledListProgressListener()),
                                    Collections.singletonList(String.format("%s/resource/%s",
                                            session.getBasePath(), resourceId)), null, null, null,
                                    status.isExists() ? "overwrite" : null, null);
                }
                else {
                    resourceMoveResponseEntries = new MoveChildrenApi(client)
                            .resourceResourceIdChildrenMovePost(fileid.getFileId(target.getParent(), new DisabledListProgressListener()),
                                    Collections.singletonList(String.format("%s/resource/%s",
                                            session.getBasePath(), resourceId)), null, null, null,
                                    status.isExists() ? "overwrite" : null, null);
                }
                if(null == resourceMoveResponseEntries) {
                    // Move of single file will return 200 status code with empty response body
                }
                else {
                    for(ResourceMoveResponseEntry resourceMoveResponseEntry : resourceMoveResponseEntries.values()) {
                        switch(resourceMoveResponseEntry.getStatusCode()) {
                            case HttpStatus.SC_OK:
                                break;
                            default:
                                log.warn(String.format("Failure %s moving file %s", resourceMoveResponseEntries, file));
                                final ResourceCreationResponseEntryEntity entity = resourceMoveResponseEntry.getEntity();
                                if(null == entity) {
                                    throw new EueExceptionMappingService().map(new ApiException(resourceMoveResponseEntry.getReason(),
                                            null, resourceMoveResponseEntry.getStatusCode(), client.getResponseHeaders()));
                                }
                                throw new EueExceptionMappingService().map(new ApiException(resourceMoveResponseEntry.getEntity().getError(),
                                        null, resourceMoveResponseEntry.getStatusCode(), client.getResponseHeaders()));
                        }
                    }
                }
            }
            if(!StringUtils.equals(file.getName(), target.getName())) {
                final ResourceUpdateModel resourceUpdateModel = new ResourceUpdateModel();
                final ResourceUpdateModelUpdate resourceUpdateModelUpdate = new ResourceUpdateModelUpdate();
                final Uifs uifs = new Uifs();
                uifs.setName(target.getName());
                resourceUpdateModelUpdate.setUifs(uifs);
                resourceUpdateModel.setUpdate(resourceUpdateModelUpdate);
                new UpdateResourceApi(client).resourceResourceIdPatch(resourceId,
                        resourceUpdateModel, null, null, null);
            }
            fileid.cache(file, null);
            return target.withAttributes(new EueAttributesFinderFeature(session, fileid).find(target, new DisabledListProgressListener()));
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }
}
