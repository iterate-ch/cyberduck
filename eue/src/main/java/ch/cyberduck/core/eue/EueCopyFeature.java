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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.CopyChildrenApi;
import ch.cyberduck.core.eue.io.swagger.client.api.CopyChildrenForAliasApiApi;
import ch.cyberduck.core.eue.io.swagger.client.api.UpdateResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCopyResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceCopyResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntries;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceMoveResponseEntry;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModel;
import ch.cyberduck.core.eue.io.swagger.client.model.ResourceUpdateModelUpdate;
import ch.cyberduck.core.eue.io.swagger.client.model.Uifs;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.InvalidFilenameException;
import ch.cyberduck.core.features.Copy;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

import static ch.cyberduck.core.features.Copy.validate;

public class EueCopyFeature implements Copy {
    private static final Logger log = LogManager.getLogger(EueCopyFeature.class);

    private final EueSession session;
    private final EueResourceIdProvider fileid;

    public EueCopyFeature(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public Path copy(final Path file, final Path target, final TransferStatus status, final ConnectionCallback callback, final StreamListener listener) throws BackgroundException {
        try {
            final EueApiClient client = new EueApiClient(session);
            if(status.isExists()) {
                log.warn("Trash file {} to be replaced with {}", target, file);
                new EueTrashFeature(session, fileid).delete(Collections.singletonMap(target, status), callback, new Delete.DisabledCallback());
            }
            final String resourceId = fileid.getFileId(file);
            final String parentResourceId = fileid.getFileId(target.getParent());
            final ResourceCopyResponseEntries resourceCopyResponseEntries;
            switch(parentResourceId) {
                case EueResourceIdProvider.ROOT:
                case EueResourceIdProvider.TRASH:
                    resourceCopyResponseEntries = new CopyChildrenForAliasApiApi(client)
                            .resourceAliasAliasChildrenCopyPost(parentResourceId,
                                    Collections.singletonList(String.format("%s/resource/%s",
                                            session.getBasePath(), resourceId)), null, null, null,
                                    "rename", null);
                    break;
                default:
                    resourceCopyResponseEntries = new CopyChildrenApi(client).resourceResourceIdChildrenCopyPost(parentResourceId,
                            Collections.singletonList(String.format("%s/resource/%s", session.getBasePath(), resourceId)), null, null, null,
                            "rename", null);
            }
            if(null == resourceCopyResponseEntries) {
                // Copy of single file will return 200 status code with empty response body
            }
            else {
                for(ResourceCopyResponseEntry resourceCopyResponseEntry : resourceCopyResponseEntries.values()) {
                    switch(resourceCopyResponseEntry.getStatusCode()) {
                        case HttpStatus.SC_CREATED:
                            fileid.cache(target, EueResourceIdProvider.getResourceIdFromResourceUri(resourceCopyResponseEntry.getHeaders().getLocation()));
                            break;
                        default:
                            log.warn("Failure {} copying file {}", resourceCopyResponseEntries, file);
                            throw new EueExceptionMappingService().map(new ApiException(resourceCopyResponseEntry.getReason(),
                                    null, resourceCopyResponseEntry.getStatusCode(), client.getResponseHeaders()));
                    }
                }

            }
            listener.sent(status.getLength());
            if(!StringUtils.equals(file.getName(), target.getName())) {
                final ResourceUpdateModel resourceUpdateModel = new ResourceUpdateModel();
                final ResourceUpdateModelUpdate resourceUpdateModelUpdate = new ResourceUpdateModelUpdate();
                final Uifs uifs = new Uifs();
                uifs.setName(target.getName());
                resourceUpdateModelUpdate.setUifs(uifs);
                resourceUpdateModel.setUpdate(resourceUpdateModelUpdate);
                final ResourceMoveResponseEntries resourceMoveResponseEntries = new UpdateResourceApi(client).resourceResourceIdPatch(fileid.getFileId(target),
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
                                log.warn("Failure {} renaming file {}", resourceMoveResponseEntry, file);
                                throw new EueExceptionMappingService().map(new ApiException(resourceMoveResponseEntry.getReason(),
                                        null, resourceMoveResponseEntry.getStatusCode(), client.getResponseHeaders()));
                        }
                    }
                }
            }
            return target;
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Cannot copy {0}", e, file);
        }
    }

    @Override
    public void preflight(final Path source, final Optional<Path> optional) throws BackgroundException {
        if(optional.isPresent()) {
            final Path target = optional.get();
            if(!EueTouchFeature.validate(target.getName())) {
                throw new InvalidFilenameException(MessageFormat.format(LocaleFactory.localizedString("Cannot create {0}", "Error"), target.getName()));
            }
            validate(session.getCaseSensitivity(), source, target);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path source, final Path target) {
        return EnumSet.of(Flags.recursive);
    }
}
