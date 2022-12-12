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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.collections.Partition;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.MoveChildrenForAliasApiApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntryEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntry;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Trash;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EueTrashFeature implements Trash {
    private static final Logger log = LogManager.getLogger(EueTrashFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueTrashFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        this.trash(files, prompt, callback);
        for(Path f : files.keySet()) {
            fileid.cache(f, null);
        }
    }

    protected List<String> trash(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<String> resources = new ArrayList<>();
        try {
            for(Path f : files.keySet()) {
                switch(fileid.getFileId(f.getParent())) {
                    case EueResourceIdProvider.TRASH:
                        log.warn(String.format("Delete file %s already in trash", f));
                        new EueDeleteFeature(session, fileid).delete(Collections.singletonList(fileid.getFileId(f)));
                        break;
                    default:
                        resources.add(fileid.getFileId(f));
                        callback.delete(f);
                }
            }
            if(!resources.isEmpty()) {
                final EueApiClient client = new EueApiClient(session);
                for(List<String> partition : new Partition<>(resources.stream().map(resourceId -> String.format("%s/resource/%s", session.getBasePath(), resourceId)).collect(Collectors.toList()),
                        new HostPreferences(session.getHost()).getInteger("eue.delete.multiple.partition"))) {
                    final ResourceMoveResponseEntries resourceMoveResponseEntries = new MoveChildrenForAliasApiApi(client).resourceAliasAliasChildrenMovePost(
                            EueResourceIdProvider.TRASH, partition,
                            null, null, null, "rename", null);
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
            }
        }
        catch(ApiException e) {
            for(Path f : files.keySet()) {
                throw new EueExceptionMappingService().map("Cannot delete {0}", e, f);
            }
        }
        return resources;
    }

    @Override
    public boolean isSupported(final Path file) {
        if(StringUtils.equals(EueResourceIdProvider.TRASH, file.attributes().getFileId())
                || StringUtils.equals(session.getHost().getProperty("cryptomator.vault.name.default"), file.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
