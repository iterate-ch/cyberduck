package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
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
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class SDSListService implements ListService {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSListService(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        return this.list(directory, listener, new HostPreferences(session.getHost()).getInteger("sds.listing.chunksize"));
    }

    protected AttributedList<Path> list(final Path directory, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        try {
            int offset = 0;
            final SDSAttributesAdapter feature = new SDSAttributesAdapter(session);
            NodeList nodes;
            do {
                nodes = new NodesApi(session.getClient()).requestNodes(null, 0,
                        Long.parseLong(nodeid.getVersionId(directory)),
                        false, null, "name:asc", offset, chunksize, StringUtils.EMPTY);
                for(Node node : nodes.getItems()) {
                    final PathAttributes attributes = feature.toAttributes(node);
                    final EnumSet<Path.Type> type = feature.toType(node);
                    final Path file = new Path(directory, node.getName(), type, attributes);
                    nodeid.cache(file, String.valueOf(node.getId()));
                    children.add(file);
                }
                offset += chunksize;
                listener.chunk(directory, children);
            }
            while(nodes.getItems().size() == chunksize);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Listing directory {0} failed", e, directory);
        }
        return children;
    }

}
