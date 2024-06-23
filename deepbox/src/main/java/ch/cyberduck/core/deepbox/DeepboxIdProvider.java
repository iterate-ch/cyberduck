package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class DeepboxIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(DeepboxIdProvider.class);

    private final DeepboxSession session;
    private final int chunksize;


    public DeepboxIdProvider(final DeepboxSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
    }

    public String getDeepBoxNodeId(final Path file) throws BackgroundException {
        final List<Path> segs = pathToList(file);
        if(segs.isEmpty()) {
            return null;
        }
        return getFileId(segs.get(0));
    }

    public String getBoxNodeId(final Path file) throws BackgroundException {
        final List<Path> segs = pathToList(file);
        if(segs.size() < 2) {
            return null;
        }
        return getFileId(segs.get(1));
    }

    public String getThirdLevelId(final Path file) throws BackgroundException {
        final List<Path> segs = pathToList(file);
        if(segs.size() < 3) {
            return null;
        }
        return getFileId(segs.get(2));
    }

    private List<Path> pathToList(final Path path) {
        final LinkedList<Path> l = new LinkedList<>();
        Path p = path;
        while(!p.isRoot()) {
            l.addFirst(p);
            p = p.getParent();
        }
        return l;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        if(file.isRoot()) {
            return null;
        }
        if(StringUtils.isNotBlank(file.attributes().getFileId())) {
            return file.attributes().getFileId();
        }
        final String cached = super.getFileId(file);
        if(cached != null) {
            if(log.isDebugEnabled()) {
                log.debug(String.format("Return cached fileid %s for file %s", cached, file));
            }
            return cached;
        }

        // iteratively add to cache
        // TODO (16) assume path separator is not in segments
        final List<Path> segs = pathToList(file);
        for(final Path seg : segs) {
            if(StringUtils.isNotBlank(seg.attributes().getFileId())) {
                continue;
            }
            final String cachedSeg = super.getFileId(file);
            if(cachedSeg != null) {
                continue;
            }
            final String ret = lookupFileId(seg);
            // fail if one of the segments cannot be found
            if(StringUtils.isEmpty(ret)) {
                return null;
            }
        }

        // get from cache now
        return super.getFileId(file);
    }

    private String lookupFileId(final Path file) throws BackgroundException {
        // pre-condition: all parents can be looked up from cache
        try {
            int size;
            int offset = 0;

            if(new DeepboxPathContainerService().isDeepbox(file)) { // DeepBox
                final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                do {
                    final DeepBoxes deepBoxes = api.listDeepBoxes(offset, this.chunksize, "displayName asc", null);
                    final String deepBoxName = file.getName();
                    final String deepBoxNodeId = deepBoxes.getDeepBoxes().stream().filter(db -> PathNormalizer.name(db.getName()).equals(deepBoxName)).findFirst().map(db -> db.getDeepBoxNodeId().toString()).orElse(null);
                    if(deepBoxNodeId != null) {
                        this.cache(file, deepBoxNodeId);
                        return deepBoxNodeId;
                    }
                    size = deepBoxes.getSize();
                    offset += this.chunksize;
                }
                while(offset < size);
                return this.cache(file, null);

            }
            else if(new DeepboxPathContainerService().isBox(file)) { // Box
                final String deepBoxNodeId = getFileId(file.getParent());
                final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                do {
                    final Boxes boxes = api.listBoxes(UUID.fromString(deepBoxNodeId), offset, this.chunksize, "displayName asc", null);
                    final String boxName = file.getName();
                    final String boxNodeId = boxes.getBoxes().stream().filter(b -> PathNormalizer.name(b.getName()).equals(boxName)).findFirst().map(b -> b.getBoxNodeId().toString()).orElse(null);
                    if(boxNodeId != null) {
                        this.cache(file, boxNodeId);
                        return boxNodeId;
                    }
                    size = boxes.getSize();
                    offset += this.chunksize;
                }
                while(offset < size);
                return this.cache(file, null);
            }
            else if(new DeepboxPathContainerService().isThirdLevel(file)) { // 3rd level: Inbox,Documents,Trash
                final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                final String boxNodeId = getFileId(file.getParent());
                final String deepBoxNodeId = getFileId(file.getParent().getParent());
                if(new DeepboxPathContainerService().isDocuments(file)) {
                    final String documentsId = api.listFiles(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null).getPath().getSegments().get(0).getNodeId().toString();
                    return this.cache(file, documentsId);
                }
                if(new DeepboxPathContainerService().isInbox(file)) {
                    final String inboxId = api.listQueue(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null, null).getPath().getSegments().get(0).getNodeId().toString();
                    return this.cache(file, inboxId);
                }
                if(new DeepboxPathContainerService().isTrash(file)) {
                    final String trashId = api.listTrash(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null).getPath().getSegments().get(0).getNodeId().toString();
                    return this.cache(file, trashId);
                }
                return null;
            }
            else if(new DeepboxPathContainerService().isThirdLevel(file.getParent())) { // Inbox,Documents,Trash
                // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with parentNode fails!

                final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                final String boxNodeId = getFileId(file.getParent().getParent());
                final String deepBoxNodeId = getFileId(file.getParent().getParent().getParent());
                if(new DeepboxPathContainerService().isInDocuments(file)) {
                    do {
                        final NodeContent files = api.listFiles(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                offset, this.chunksize, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> PathNormalizer.name(b.getName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        if(nodeId != null) {
                            this.cache(file, nodeId);
                            return nodeId;
                        }
                        size = files.getSize();
                        offset += this.chunksize;
                    }
                    while(offset < size);
                    return this.cache(file, null);
                }
                else if(new DeepboxPathContainerService().isInInbox(file)) {
                    do {
                        final NodeContent files = api.listQueue(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                null, offset, this.chunksize, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> PathNormalizer.name(b.getName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        if(nodeId != null) {
                            this.cache(file, nodeId);
                            return nodeId;
                        }
                        size = files.getSize();
                        offset += this.chunksize;
                    }
                    while(offset < size);
                    return this.cache(file, null);
                }
                else if(new DeepboxPathContainerService().isInTrash(file)) {
                    do {
                        final NodeContent files = api.listTrash(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                offset, this.chunksize, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> PathNormalizer.name(b.getName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        if(nodeId != null) {
                            this.cache(file, nodeId);
                            return nodeId;
                        }
                        size = files.getSize();
                        offset += this.chunksize;
                    }
                    while(offset < size);
                    return this.cache(file, null);
                }
                return null;
            }
            else { // second+ level under Documents,Trash (Inbox has no hierarchy)
                final String deepBoxNodeId = getDeepBoxNodeId(file.getParent());
                final String boxNodeId = getBoxNodeId(file.getParent());
                final String parentNodeId = getFileId(file.getParent());

                final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                if(new DeepboxPathContainerService().isInDocuments(file)) {
                    do {
                        final NodeContent files = api.listFiles1(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                UUID.fromString(parentNodeId),
                                offset, this.chunksize, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> PathNormalizer.name(b.getName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        if(nodeId != null) {
                            this.cache(file, nodeId);
                            return nodeId;
                        }
                        size = files.getSize();
                        offset += this.chunksize;
                    }
                    while(offset < size);
                    return this.cache(file, null);
                }
                else if(new DeepboxPathContainerService().isInTrash(file)) {
                    do {
                        final NodeContent files = api.listTrash1(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                UUID.fromString(parentNodeId),
                                offset, this.chunksize, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> PathNormalizer.name(b.getName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        if(nodeId != null) {
                            this.cache(file, nodeId);
                            return nodeId;
                        }
                        size = files.getSize();
                        offset += this.chunksize;
                    }
                    while(offset < size);
                    return this.cache(file, null);
                }
                return null;
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(this).map("Failure to read attributes of {0}", e, file);
        }
    }
}
