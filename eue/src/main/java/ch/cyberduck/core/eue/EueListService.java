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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.eue.io.swagger.client.ApiException;
import ch.cyberduck.core.eue.io.swagger.client.api.ListResourceAliasApi;
import ch.cyberduck.core.eue.io.swagger.client.api.ListResourceApi;
import ch.cyberduck.core.eue.io.swagger.client.model.Children;
import ch.cyberduck.core.eue.io.swagger.client.model.UiFsModel;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import java.util.Collections;
import java.util.EnumSet;

public class EueListService implements ListService {

    private final EueSession session;
    private final EueAttributesFinderFeature attributes;
    private final EueResourceIdProvider fileid;

    public EueListService(final EueSession session, final EueResourceIdProvider fileid) {
        this.session = session;
        this.attributes = new EueAttributesFinderFeature(session, fileid);
        this.fileid = fileid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, new HostPreferences(session.getHost()).getInteger("eue.listing.chunksize"));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<>();
        final EueApiClient client = new EueApiClient(session);
        try {
            int offset = 0;
            UiFsModel fsModel;
            do {
                final String resourceId = fileid.getFileId(directory);
                switch(resourceId) {
                    case EueResourceIdProvider.ROOT:
                    case EueResourceIdProvider.TRASH:
                        fsModel = new ListResourceAliasApi(client).resourceAliasAliasGet(resourceId,
                                null, null, null, null, chunksize, offset,
                                Collections.singletonList(EueAttributesFinderFeature.OPTION_WIN_32_PROPS), null);
                        break;
                    default:
                        fsModel = new ListResourceApi(client).resourceResourceIdGet(resourceId,
                                null, null, null, null, chunksize, offset,
                                Collections.singletonList(EueAttributesFinderFeature.OPTION_WIN_32_PROPS), null);
                        break;
                }
                for(Children child : fsModel.getUifs().getChildren()) {
                    final EnumSet<Path.Type> type;
                    switch(child.getUifs().getResourceType()) {
                        case "aliascontainer":
                            type = EnumSet.of(Path.Type.directory, Path.Type.placeholder);
                            break;
                        case "container":
                            type = EnumSet.of(Path.Type.directory);
                            break;
                        default:
                            type = EnumSet.of(Path.Type.file);
                    }
                    children.add(new Path(directory, child.getUifs().getName(), type,
                            attributes.toAttributes(child.getUifs(), child.getUiwin32(),
                                    EueShareFeature.findShareForResource(session.userShares(),
                                            EueResourceIdProvider.getResourceIdFromResourceUri(child.getUifs().getResourceURI()))))
                    );
                    listener.chunk(directory, children);
                }
                offset += chunksize;
            }
            while(fsModel.getUifs().getChildren().size() == chunksize);
            return children;
        }
        catch(ApiException e) {
            throw new EueExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
