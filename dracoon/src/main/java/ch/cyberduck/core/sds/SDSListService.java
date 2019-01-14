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


import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.PreferencesFactory;
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
        return this.list(directory, listener, PreferencesFactory.get().getInteger("sds.listing.chunksize"));
    }

    public AttributedList<Path> list(final Path directory, final ListProgressListener listener, final int chunksize) throws BackgroundException {
        final AttributedList<Path> children = new AttributedList<Path>();
        try {
            Integer offset = 0;
            final SDSAttributesFinderFeature feature = new SDSAttributesFinderFeature(session, nodeid);
            NodeList nodes;
            do {
                nodes = new NodesApi(session.getClient()).getFsNodes(0,
                    Long.parseLong(nodeid.getFileid(directory, new DisabledListProgressListener())),
                    null, null, "name:asc", offset, chunksize, StringUtils.EMPTY, null);
                for(Node node : nodes.getItems()) {
                    final PathAttributes attributes = feature.toAttributes(node);
                    final EnumSet<AbstractPath.Type> type;
                    switch(node.getType()) {
                        case ROOM:
                            type = EnumSet.of(Path.Type.directory, Path.Type.volume);
                            break;
                        case FOLDER:
                            type = EnumSet.of(Path.Type.directory);
                            if(node.getIsEncrypted()) {
                                type.add(Path.Type.decrypted);
                            }
                            break;
                        default:
                            type = EnumSet.of(Path.Type.file);
                            if(node.getIsEncrypted()) {
                                type.add(Path.Type.decrypted);
                            }
                    }
                    final Path file = new Path(directory, node.getName(), type, attributes);
                    children.add(file);
                    listener.chunk(directory, children);
                }
                offset += chunksize;
            }
            while(nodes.getItems().size() == chunksize);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
        return children;
    }

    @Override
    public ListService withCache(final Cache<Path> cache) {
        nodeid.withCache(cache);
        return this;
    }
}
