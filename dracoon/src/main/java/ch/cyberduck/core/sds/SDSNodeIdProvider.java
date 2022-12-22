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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.VersionIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.unicode.UnicodeNormalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.fasterxml.jackson.databind.ObjectWriter;

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
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return version %s from attributes for file %s", file.attributes().getVersionId(), file));
            }
            return file.attributes().getVersionId();
        }
        final String cached = super.getVersionId(file);
        if(cached != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached versionid %s for file %s", cached, file));
            }
            return cached;
        }
        return this.getNodeId(file, new HostPreferences(session.getHost()).getInteger("sds.listing.chunksize"));
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
                nodes = new NodesApi(session.getClient()).searchNodes(
                        URIEncoder.encode(normalizer.normalize(file.getName()).toString()),
                        StringUtils.EMPTY, -1, null,
                        String.format("type:eq:%s|parentPath:eq:%s/", type, file.getParent().isRoot() ? StringUtils.EMPTY : file.getParent().getAbsolute()),
                        null, offset, chunksize, null);
                for(Node node : nodes.getItems()) {
                    // Case insensitive
                    if(node.getName().equalsIgnoreCase(normalizer.normalize(file.getName()).toString())) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Return node %s for file %s", node.getId(), file));
                        }
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

    public static boolean isEncrypted(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        if(file.attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return Boolean.parseBoolean(file.attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED));
        }
        final Path parent = file.getParent();
        if(parent.attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return Boolean.parseBoolean(parent.attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED));
        }
        final Path container = new DefaultPathContainerService().getContainer(file);
        if(container.attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return Boolean.parseBoolean(container.attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED));
        }
        return false;
    }

    public ByteBuffer getFileKey() throws BackgroundException {
        return this.toBuffer(TripleCryptConverter.toSwaggerFileKey(Crypto.generateFileKey(PlainFileKey.Version.AES256GCM)));
    }

    public ByteBuffer toBuffer(final FileKey fileKey) throws BackgroundException {
        final ObjectWriter writer = session.getClient().getJSON().getContext(null).writerFor(FileKey.class);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            writer.writeValue(out, fileKey);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
        return ByteBuffer.wrap(out.toByteArray());
    }
}
