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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.DeleteResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.api.MoveChildrenForAliasApiApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntryEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntry;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EueDeleteFeature implements Delete {
    private static final Logger log = Logger.getLogger(EueDeleteFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    private final Boolean trashing;

    public EueDeleteFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getBoolean("eue.delete.trash"));
    }

    public EueDeleteFeature(final EueSession session, final EueResourceIdProvider fileid, final Boolean trashing) {
        this.session = session;
        this.fileid = fileid;
        this.trashing = trashing;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        try {
            final EueApiClient client = new EueApiClient(session);
            final List<String> resources = new ArrayList<>();
            for(Path f : files.keySet()) {
                switch(fileid.getFileId(f.getParent(), new DisabledListProgressListener())) {
                    case EueResourceIdProvider.TRASH:
                        new DeleteResourceApi(client).resourceResourceIdDelete(
                                fileid.getFileId(f, new DisabledListProgressListener()), null, null);
                        break;
                    default:
                        final String resourceId = fileid.getFileId(f, new DisabledListProgressListener());
                        resources.add(String.format("%s/resource/%s", session.getBasePath(), resourceId));
                }
                callback.delete(f);
            }
            if(!resources.isEmpty()) {
                final ResourceMoveResponseEntries resourceMoveResponseEntries = new MoveChildrenForAliasApiApi(client).resourceAliasAliasChildrenMovePost(
                        EueResourceIdProvider.TRASH, resources, null, null, null, "rename", null);
                if(null == resourceMoveResponseEntries) {
                    // Move of single file will return 200 status code with empty response body
                }
                else {
                    for(ResourceMoveResponseEntry resourceMoveResponseEntry : resourceMoveResponseEntries.values()) {
                        switch(resourceMoveResponseEntry.getStatusCode()) {
                            case HttpStatus.SC_OK:
                                break;
                            default:
                                log.warn(String.format("Failure %s trashing resource %s", resourceMoveResponseEntries, resourceMoveResponseEntry));
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
            if(!trashing) {
                for(Path f : files.keySet()) {
                    switch(fileid.getFileId(f.getParent(), new DisabledListProgressListener())) {
                        case EueResourceIdProvider.TRASH:
                            break;
                        default:
                            new DeleteResourceApi(new EueApiClient(session)).resourceResourceIdDelete(
                                    fileid.getFileId(f, new DisabledListProgressListener()), null, null);
                            break;
                    }
                }
            }
            for(Path f : files.keySet()) {
                fileid.cache(f, null);
            }
        }
        catch(ApiException e) {
            for(Path f : files.keySet()) {
                throw new EueExceptionMappingService().map("Cannot delete {0}", e, f);
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
