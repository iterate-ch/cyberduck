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

import ch.cyberduck.core.CaseInsensitivePathPredicate;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.MoveChildrenApi;
import ch.cyberduck.core.eue.io.swagger.client.api.MoveChildrenForAliasApiApi;
import ch.cyberduck.core.eue.io.swagger.client.api.UpdateResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCreationResponseEntryEntity;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModel;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModelUpdate;
import ch.cyberduck.core.eue.io.swagger.client.model.Uifs;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Move;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;

public class EueMoveFeature implements Move {
    private static final Logger log = LogManager.getLogger(EueMoveFeature.class);

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
            if(status.isExists()) {
                if(!new CaseInsensitivePathPredicate(file).test(target)) {
                    if(log.isWarnEnabled()) {
                        log.warn(String.format("Trash file %s to be replaced with %s", target, file));
                    }
                    new EueTrashFeature(session, fileid).delete(Collections.singletonMap(target, status), callback, delete);
                }
            }
            final String resourceId = fileid.getFileId(file);
            if(!new SimplePathPredicate(file.getParent()).test(target.getParent())) {
                final ResourceMoveResponseEntries resourceMoveResponseEntries;
                final String parentResourceId = fileid.getFileId(target.getParent());
                switch(parentResourceId) {
                    case EueResourceIdProvider.ROOT:
                    case EueResourceIdProvider.TRASH:
                        resourceMoveResponseEntries = new MoveChildrenForAliasApiApi(client)
                                .resourceAliasAliasChildrenMovePost(parentResourceId,
                                        Collections.singletonList(String.format("%s/resource/%s",
                                                session.getBasePath(), resourceId)), null, null, null,
                                        "rename", null);
                        break;
                    default:
                        resourceMoveResponseEntries = new MoveChildrenApi(client)
                                .resourceResourceIdChildrenMovePost(parentResourceId,
                                        Collections.singletonList(String.format("%s/resource/%s",
                                                session.getBasePath(), resourceId)), null, null, null,
                                        "rename", null);
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
                final ResourceMoveResponseEntries resourceMoveResponseEntries = new UpdateResourceApi(client).resourceResourceIdPatch(resourceId,
                        resourceUpdateModel, null, null, null);
                if(null == resourceMoveResponseEntries) {
                    // Move of single file will return 200 status code with empty response body
                }
                else {
                    for(ResourceMoveResponseEntry resourceMoveResponseEntry : resourceMoveResponseEntries.values()) {
                        switch(resourceMoveResponseEntry.getStatusCode()) {
                            case HttpStatus.SC_CREATED:
                                break;
                            default:
                                log.warn(String.format("Failure %s renaming file %s", resourceMoveResponseEntry, file));
                                throw new EueExceptionMappingService().map(new ApiException(resourceMoveResponseEntry.getReason(),
                                        null, resourceMoveResponseEntry.getStatusCode(), client.getResponseHeaders()));
                        }
                    }
                }
            }
            fileid.cache(file, null);
            return target;
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Cannot rename {0}", e, file);
        }
    }

    @Override
    public void preflight(final Path source, final Path directory, final String filename) throws BackgroundException {
        if(StringUtils.equals(EueResourceIdProvider.TRASH, source.attributes().getFileId())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(StringUtils.equals(session.getHost().getProperty("cryptomator.vault.name.default"), source.getName())) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot rename {0}", "Error"), source.getName())).withFile(source);
        }
        if(!EueTouchFeature.validate(filename)) {
            throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), filename));
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
