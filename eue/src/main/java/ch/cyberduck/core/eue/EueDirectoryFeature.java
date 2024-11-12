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

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.PostChildrenForAliasApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationRepresentationArrayInner;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntryEntity;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConflictException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Directory;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;

public class EueDirectoryFeature implements Directory<EueWriteFeature.Chunk> {
    private static final Logger log = LogManager.getLogger(EueDirectoryFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueDirectoryFeature(final EueSession session, final EueResourceIdProvider fileid) {
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
            final EueApiClient client = new EueApiClient(session);
            final ResourceCreationResponseEntries resourceCreationResponseEntries = new PostChildrenForAliasApi(client).resourceAliasAliasChildrenPost(
                    EueResourceIdProvider.ROOT, Collections.singletonList(resourceCreationRepresentation), null, null, null, null, null);
            if(!resourceCreationResponseEntries.containsKey(path)) {
                throw new NotfoundException(folder.getAbsolute());
            }
            final ResourceCreationResponseEntry resourceCreationResponseEntry = resourceCreationResponseEntries.get(path);
            switch(resourceCreationResponseEntry.getStatusCode()) {
                case HttpStatus.SC_OK:
                    // Already exists
                    throw new ConflictException(folder.getAbsolute());
                case HttpStatus.SC_CREATED:
                    final String resourceId = EueResourceIdProvider.getResourceIdFromResourceUri(resourceCreationResponseEntry.getHeaders().getLocation());
                    fileid.cache(folder, resourceId);
                    return folder;
                default:
                    log.warn("Failure {} creating folder {}", resourceCreationResponseEntry, folder);
                    final ResourceCreationResponseEntryEntity entity = resourceCreationResponseEntry.getEntity();
                    if(null == entity) {
                        throw new EueExceptionMappingService().map(new ApiException(resourceCreationResponseEntry.getReason(),
                                null, resourceCreationResponseEntry.getStatusCode(), client.getResponseHeaders()));
                    }
                    throw new EueExceptionMappingService().map(new ApiException(resourceCreationResponseEntry.getEntity().getError(),
                            null, resourceCreationResponseEntry.getStatusCode(), client.getResponseHeaders()));
            }
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Cannot create folder {0}", e, folder);
        }
    }

    @Override
    public void preflight(final Path workdir, final String filename) throws BackgroundException {
        if(!EueTouchFeature.validate(filename)) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create folder {0}", "Error"), filename));
        }
    }
}
