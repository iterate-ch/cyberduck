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
import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathCache;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.URIEncoder;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.IdProvider;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.FileKey;
import ch.cyberduck.core.sds.io.swagger.client.model.Node;
import ch.cyberduck.core.sds.io.swagger.client.model.NodeList;
import ch.cyberduck.core.sds.triplecrypt.TripleCryptConverter;
import ch.cyberduck.core.unicode.NFCNormalizer;
import ch.cyberduck.core.unicode.UnicodeNormalizer;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.dracoon.sdk.crypto.Crypto;
import com.dracoon.sdk.crypto.model.PlainFileKey;
import com.fasterxml.jackson.databind.ObjectWriter;

public class SDSNodeIdProvider implements IdProvider {
    private static final Logger log = Logger.getLogger(SDSNodeIdProvider.class);

    private static final UnicodeNormalizer normalizer = new NFCNormalizer();
    private static final String ROOT_NODE_ID = "0";

    private final SDSSession session;

    private Cache<Path> cache = PathCache.empty();

    public SDSNodeIdProvider(final SDSSession session) {
        this.session = session;
    }

    @Override
    public String getFileid(final Path file, final ListProgressListener listener) throws BackgroundException {
        return this.getFileid(file, listener, PreferencesFactory.get().getInteger("sds.listing.chunksize"));
    }

    protected String getFileid(final Path file, final ListProgressListener listener, final int chunksize) throws BackgroundException {
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
            final Path found = list.find(new SimplePathPredicate(file));
            if(null != found) {
                if(StringUtils.isNotBlank(found.attributes().getVersionId())) {
                    return this.set(file, found.attributes().getVersionId());
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

            int offset = 0;
            NodeList nodes;
            do {
                nodes = new NodesApi(session.getClient()).searchNodes(
                    URIEncoder.encode(normalizer.normalize(file.getName()).toString()),
                    StringUtils.EMPTY, -1, null,
                    String.format("type:eq:%s|parentPath:eq:%s/", type, file.getParent().isRoot() ? StringUtils.EMPTY : file.getParent().getAbsolute()),
                    null, offset, chunksize, null);
                for(Node node : nodes.getItems()) {
                    if(node.getName().equals(normalizer.normalize(file.getName()).toString())) {
                        if(log.isInfoEnabled()) {
                            log.info(String.format("Return node %s for file %s", node.getId(), file));
                        }
                        return this.set(file, node.getId().toString());
                    }
                }
                offset += chunksize;
            }
            while(nodes.getItems().size() == chunksize);
            throw new NotfoundException(file.getAbsolute());
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    protected String set(final Path file, final String id) {
        file.attributes().setVersionId(id);
        return id;
    }

    public boolean isEncrypted(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        if(file.getType().contains(Path.Type.triplecrypt)) {
            return true;
        }
        if(file.attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return Boolean.parseBoolean(file.attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED));
        }
        final Path parent = file.getParent();
        if(parent.getType().contains(Path.Type.triplecrypt)) {
            // Backward compatibility where flag is missing in room
            return true;
        }
        if(parent.attributes().getCustom().containsKey(SDSAttributesFinderFeature.KEY_ENCRYPTED)) {
            return Boolean.parseBoolean(parent.attributes().getCustom().get(SDSAttributesFinderFeature.KEY_ENCRYPTED));
        }
        final Path container = new PathContainerService().getContainer(file);
        if(container.getType().contains(Path.Type.triplecrypt)) {
            return true;
        }
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

    @Override
    public SDSNodeIdProvider withCache(final Cache<Path> cache) {
        this.cache = cache;
        return this;
    }
}
