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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiWin32;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Uifs;

import org.apache.log4j.Logger;

public class GmxcloudAttributesFinderFeature implements AttributesFinder {
    private static final Logger log = Logger.getLogger(GmxcloudAttributesFinderFeature.class);

    private final GmxcloudSession session;
    private final GmxcloudResourceIdProvider fileid;

    public GmxcloudAttributesFinderFeature(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            final String resourceId = fileid.getFileId(file, listener);
            if(resourceId == null) {
                throw new NotfoundException(file.getAbsolute());
            }
            final GmxcloudApiClient client = new GmxcloudApiClient(session);
            final UiFsModel response;
            if(file.isPlaceholder()) {
                response = new ListResourceAliasApi(client).resourceAliasAliasGet(resourceId,
                        null, null, null, null, null, null, "win32props", null);
            }
            else {
                response = new ListResourceApi(client).resourceResourceIdGet(resourceId,
                        null, null, null, null, null, null, "win32props", null);
            }
            return this.toAttributes(response.getUifs(), response.getUiwin32());
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    protected PathAttributes toAttributes(final Uifs entity, final UiWin32 uiwin32) {
        final PathAttributes attr = new PathAttributes();
        attr.setDisplayname(entity.getName());
        attr.setETag(entity.getContentETag());
        attr.setSize(entity.getSize());
        attr.setFileId(GmxcloudResourceIdProvider.getResourceIdFromResourceUri(entity.getResourceURI()));
        if(null == uiwin32) {
            log.warn("Missing extended properties");
            return attr;
        }
        else {
            attr.setCreationDate(uiwin32.getCreationMillis());
            attr.setModificationDate(uiwin32.getLastModificationMillis());
            attr.setAccessedDate(uiwin32.getLastAccessMillis());
            attr.setHidden(uiwin32.isHidden());
        }
        return attr;
    }
}
