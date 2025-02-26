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

import ch.cyberduck.core.CachingVersionIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.VersionIdProvider;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.unicode.UnicodeNormalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

public class SDSNodeIdProvider extends CachingVersionIdProvider implements VersionIdProvider {
    private static final Logger log = LogManager.getLogger(SDSNodeIdProvider.class);

    private static final UnicodeNormalizer normalizer = new NFCNormalizer();
    private static final String ROOT_NODE_ID = "0";

    private final SDSSession session;

    public SDSNodeIdProvider(final SDSSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
    }

    @Override
    public String getVersionId(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            log.debug("Return version {} from attributes for file {}", file.attributes().getVersionId(), file);
            return file.attributes().getVersionId();
        }
        final String cached = super.getVersionId(file);
        if(cached != null) {
            log.debug("Return cached versionid {} for file {}", cached, file);
            return cached;
        }
        return this.getNodeId(file, HostPreferencesFactory.get(session.getHost()).getInteger("sds.listing.chunksize"));
    }

    protected String getNodeId(final Path file, final int chunksize) throws BackgroundException {
        if(file.isRoot()) {
            return ROOT_NODE_ID;
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

            int offset = 0;
            NodeList nodes;
            do {
                if(StringUtils.isNoneBlank(file.getParent().attributes().getVersionId())) {
                    nodes = new NodesApi(session.getClient()).searchNodes(
                            URIEncoder.encode(normalizer.normalize(file.getName()).toString()),
                            StringUtils.EMPTY, 0, Long.valueOf(file.getParent().attributes().getVersionId()),
                            String.format("type:eq:%s", type),
                            null, offset, chunksize, null);
                }
                else {
                    nodes = new NodesApi(session.getClient()).searchNodes(
                            URIEncoder.encode(normalizer.normalize(file.getName()).toString()),
                            StringUtils.EMPTY, -1, null,
                            String.format("type:eq:%s|parentPath:eq:%s/", type, file.getParent().isRoot() ? StringUtils.EMPTY : file.getParent().getAbsolute()),
                            null, offset, chunksize, null);
                }
                for(Node node : nodes.getItems()) {
                    // Case-insensitive
                    if(node.getName().equalsIgnoreCase(normalizer.normalize(file.getName()).toString())) {
                        log.info("Return node {} for file {}", node.getId(), file);
                        return this.cache(file, node.getId().toString());
                    }
                }
                offset += chunksize;
            }
            while(nodes.getItems().size() == chunksize);
            throw new NotfoundException(file.getAbsolute());
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(this).map("Failure to read attributes of {0}", e, file);
        }
    }

    public <V> V retry(final Path file, final ApiExceptionCallable<V> callable) throws ApiException, BackgroundException {
        try {
            return callable.call();
        }
        catch(ApiException e) {
            switch(e.getCode()) {
                case HttpStatus.SC_NOT_FOUND:
                    // Parent directory not found. Try again with resetting node id cache
                    this.cache(file, null);
                    log.warn("Retry {}", callable);
                    return callable.call();
            }
            throw e;
        }
    }

    public interface ApiExceptionCallable<T> extends Callable<T> {
        @Override
        T call() throws ApiException, BackgroundException;
    }
}
