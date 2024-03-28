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

import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.CachingVersionIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.features.VersionIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.unicode.UnicodeNormalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SDSNodeIdProvider implements FileIdProvider, VersionIdProvider {
    private static final Logger log = LogManager.getLogger(SDSNodeIdProvider.class);

    private static final UnicodeNormalizer normalizer = new NFCNormalizer();
    private static final String ROOT_NODE_ID = "0";

    private final SDSSession session;
    /**
     * Node id as file version
     */
    private final CachingVersionIdProvider versions;
    /**
     * Reference id that is static across different file versions
     */
    private final CachingFileIdProvider references;

    public SDSNodeIdProvider(final SDSSession session) {
        this.session = session;
        this.versions = new CachingVersionIdProvider(session.getCaseSensitivity());
        this.references = new CachingFileIdProvider(session.getCaseSensitivity());
    }

    @Override
    public String getVersionId(final Path file) throws BackgroundException {
        if(StringUtils.isNotBlank(file.attributes().getVersionId())) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return version %s from attributes for file %s", file.attributes().getVersionId(), file));
            }
            return file.attributes().getVersionId();
        }
        final String cached = versions.getVersionId(file);
        if(cached != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached versionid %s for file %s", cached, file));
            }
            return cached;
        }
        final Node node = this.getNode(file, new HostPreferences(session.getHost()).getInteger("sds.listing.chunksize"));
        if(null == node) {
            return ROOT_NODE_ID;
        }
        if(null != node.getId()) {
            return String.valueOf(node.getId());
        }
        return null;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        if(file.isDirectory()) {
            return null;
        }
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return version %s from attributes for file %s", file.attributes().getFileId(), file));
            }
            return file.attributes().getFileId();
        }
        final String cached = references.getFileId(file);
        if(cached != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached versionid %s for file %s", cached, file));
            }
            return cached;
        }
        final Node node = this.getNode(file, new HostPreferences(session.getHost()).getInteger("sds.listing.chunksize"));
        if(null == node) {
            return ROOT_NODE_ID;
        }
        if(null != node.getReferenceId()) {
            return String.valueOf(node.getReferenceId());
        }
        return null;
    }

    @Override
    public void clear() {
        versions.clear();
        references.clear();
    }

    protected Node getNode(final Path file, final int chunksize) throws BackgroundException {
        if(file.isRoot()) {
            return null;
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
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Return node %s for file %s", node.getId(), file));
                        }
                        versions.cache(file, node.getId().toString());
                        references.cache(file, node.getReferenceId().toString());
                        return node;
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
}
