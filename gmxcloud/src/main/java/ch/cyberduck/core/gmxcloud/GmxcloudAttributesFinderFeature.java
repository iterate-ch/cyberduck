package ch.cyberduck.core.gmxcloud;/*
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
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiWin32;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Uifs;

public class GmxcloudAttributesFinderFeature implements AttributesFinder {

    private final GmxcloudSession session;
    private final GmxcloudIdProvider fileid;

    public GmxcloudAttributesFinderFeature(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        final GmxcloudApiClient client = new GmxcloudApiClient(session);
        try {
            final String fileId = this.fileid.getFileId(file, listener);
            if(fileId == null) {
                throw new NotfoundException(file.getAbsolute());
            }
            UiFsModel response = new ListResourceApi(client).resourceResourceIdGet(fileId,
                null, null, null, null, null, null, "win32props", null);
            final PathAttributes pathAttributes = this.toAttributes(response.getUifs());
            if(response.getUiwin32() != null) {
                this.addUi32Properties(pathAttributes, response.getUiwin32());
            }
            return pathAttributes;
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map(e);
        }
    }

    protected PathAttributes toAttributes(final Uifs entity) {
        final PathAttributes attr = new PathAttributes();
        attr.setDisplayname(entity.getName());
        attr.setETag(entity.getMetaETag());
        attr.setSize(entity.getSize());
        attr.setVersionId("" + entity.getVersion());
        attr.setFileId(Util.getResourceIdFromResourceUri(entity.getResourceURI()));
        return attr;
    }

    protected void addUi32Properties(PathAttributes pathAttributes, final UiWin32 uiWin32) {
        pathAttributes.setCreationDate(uiWin32.getCreationMillis());
        pathAttributes.setModificationDate(uiWin32.getLastModificationMillis());
        pathAttributes.setHidden(uiWin32.isHidden());
        pathAttributes.setAccessedDate(uiWin32.getLastAccessMillis());
    }
}
