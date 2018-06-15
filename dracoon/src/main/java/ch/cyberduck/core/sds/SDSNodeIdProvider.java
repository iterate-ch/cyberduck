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
import ch.cyberduck.core.Cache;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.NullFilter;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class SDSNodeIdProvider implements IdProvider {
    private static final Logger log = Logger.getLogger(SDSNodeIdProvider.class);

    private static final String ROOT_NODE_ID = "0";

    private final SDSSession session;

    private Cache<Path> cache = PathCache.empty();

    private final PathContainerService containerService
        = new SDSPathContainerService();

    public SDSNodeIdProvider(final SDSSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Return cached node %s for file %s", file.attributes().getVersionId(), file));
            }
            return file.attributes().getVersionId();
        }
        if(file.isRoot()) {
            return ROOT_NODE_ID;
        }
        if(cache.isCached(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.filter(new NullFilter<>()).find(new SimplePathPredicate(file));
            if(null != found) {
                if(StringUtils.isNotBlank(found.attributes().getVersionId())) {
                    return found.attributes().getVersionId();
                }
            }
        }
        try {
            final String type;
            if(file.isDirectory()) {
                type = "room:folder";
            }
            else {
                type = "file";
            }
            // Top-level nodes only
            final NodeList nodes = new NodesApi(session.getClient()).getFsNodes(0,
                Long.parseLong(this.getFileid(file.getParent(), new DisabledListProgressListener())),
                null, String.format("type:eq:%s|name:cn:%s", type, file.getName()),
                null, null, null, StringUtils.EMPTY, null);
            for(Node node : nodes.getItems()) {
                if(node.getName().equals(file.getName())) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Return node %s for file %s", node.getId(), file));
                    }
                    return node.getId().toString();
                }
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    public boolean isEncrypted(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return false;
        }
        final Path container = containerService.getContainer(file);
        if(cache.isCached(file.getParent())) {
            final AttributedList<Path> list = cache.get(file.getParent());
            final Path found = list.filter(new NullFilter<>()).find(new SimplePathPredicate(file));
            if(null != found) {
                if(file.attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
                    return true;
                }
            }
        }
        try {
            if(container.getType().contains(Path.Type.vault)) {
                return true;
            }
            // Top-level nodes only
            final NodeList nodes = new NodesApi(session.getClient()).getFsNodes(0,
                Long.parseLong(ROOT_NODE_ID), null, String.format("type:eq:%s|name:cn:%s", "room", container.getName()),
                null, null, null, StringUtils.EMPTY, null);
            for(Node node : nodes.getItems()) {
                if(node.getName().equals(container.getName())) {
                    return node.getIsEncrypted();
                }
            }
            throw new NotfoundException(file.getAbsolute());
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public SDSNodeIdProvider withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
