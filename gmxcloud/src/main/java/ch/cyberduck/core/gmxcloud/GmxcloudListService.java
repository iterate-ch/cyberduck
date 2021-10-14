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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Children;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.preferences.HostPreferences;

import java.util.EnumSet;

public class GmxcloudListService implements ListService {

    private final GmxcloudSession session;
    private final GmxcloudAttributesFinderFeature attributes;
    private final GmxcloudResourceIdProvider fileId;

    public GmxcloudListService(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid) {
        this.session = session;
        this.attributes = new GmxcloudAttributesFinderFeature(session, fileid);
        this.fileId = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, new HostPreferences(session.getHost()).getInteger("gmxcloud.listing.chunksize"));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        final GmxcloudApiClient client = new GmxcloudApiClient(session);
        try {
            final String resourceId = fileId.getFileId(directory, listener);
            int offset = 0;
            UiFsModel fsModel;
            do {
                if(directory.isRoot()) {
                    fsModel = new ListResourceAliasApi(client).resourceAliasAliasGet(resourceId,
                            null, null, null, null, chunksize, offset, null, null);
                }
                else {
                    fsModel = new ListResourceApi(client).resourceResourceIdGet(resourceId,
                            null, null, null, null, chunksize, offset, "win32props", null);
                }
                for(Children child : fsModel.getUifs().getChildren()) {
                    children.add(new Path(directory, child.getUifs().getName(),
                            EnumSet.of("container".equalsIgnoreCase(child.getUifs().getResourceType()) ? Path.Type.directory : Path.Type.file),
                            attributes.toAttributes(child.getUifs(), child.getUiwin32())));
                    listener.chunk(directory, children);
                }
                offset += chunksize;
            }
            while(fsModel.getUifs().getChildren().size() == chunksize);
            return children;
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }

    }
}
