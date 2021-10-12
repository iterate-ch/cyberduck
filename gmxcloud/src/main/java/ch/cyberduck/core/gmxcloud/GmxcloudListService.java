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

import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Children;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.gmxcloud.io.swagger.client.model.Uifs;

import java.util.EnumSet;

public class GmxcloudListService implements ListService {

    private final GmxcloudSession session;
    private final GmxcloudAttributesFinderFeature attributes;
    private final GmxcloudIdProvider fileId;

    public GmxcloudListService(final GmxcloudSession session, final GmxcloudIdProvider fileid) {
        this.session = session;
        this.attributes = new GmxcloudAttributesFinderFeature(session, fileid);
        this.fileId = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener)
        throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        final GmxcloudApiClient client = new GmxcloudApiClient(session);
        UiFsModel response;
        try {
            if(directory.isRoot()) {
                response = new ListResourceAliasApi(client).resourceAliasAliasGet("ROOT",
                    null, null, null, null, null, null, null, null);
            }
            else {
                final String fileId = this.fileId.getFileId(directory, listener);
                if(fileId == null) {
                    throw new NotfoundException(directory.getAbsolute());
                }
                response = new ListResourceApi(client).resourceResourceIdGet(fileId,
                    null, null, null, null, null, null, "win32props", null);
            }
            for(Children child : response.getUifs().getChildren()) {
                Uifs uifs = child.getUifs();
                EnumSet<Type> type = EnumSet.of("container".equals(uifs.getResourceType()) ? Path.Type.directory : Path.Type.file);
                final PathAttributes attributes = this.attributes.toAttributes(uifs);
                if(child.getUiwin32() != null) {
                    this.attributes.addUi32Properties(attributes, child.getUiwin32());
                }
                children.add(new Path(directory, uifs.getName(), type, attributes));
                listener.chunk(directory, children);
            }
            return children;
        }
        catch(ApiException e) {
            throw new GmxcloudExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }

    }

}
