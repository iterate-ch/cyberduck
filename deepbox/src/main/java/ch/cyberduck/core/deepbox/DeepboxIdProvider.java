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
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;

public class DeepboxIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(DeepboxIdProvider.class);

    private final DeepboxSession session;
    private final int chunksize;
    private final DeepboxPathContainerService containerService = new DeepboxPathContainerService();

    public DeepboxIdProvider(final DeepboxSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
    }

    public String getDeepBoxNodeId(final Path file) throws BackgroundException {
        final Path deepBox = containerService.getDeepboxPath(file);
        if(null == deepBox) {
            throw new NotfoundException(file.getName());
        }
        return this.getFileId(deepBox);
    }

    public String getBoxNodeId(final Path file) throws BackgroundException {
        final Path box = containerService.getBoxPath(file);
        if(null == box) {
            throw new NotfoundException(file.getName());
        }
        return this.getFileId(box);
    }

    public String getThirdLevelId(final Path file) throws BackgroundException {
        final Path path = containerService.getThirdLevelPath(file);
        if(null == path) {
            throw new NotfoundException(file.getName());
        }
        return this.getFileId(path);
    }

    private Deque<Path> decompose(final Path path) {
        final Deque<Path> walk = new ArrayDeque<>();
        Path next = path;
        while(!next.isRoot()) {
            walk.addFirst(next);
            next = next.getParent();
        }
        return walk;
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
        // The DeepBox API is ID-based and not path-based.
        // Therefore, we have to iteratively get from/add to cache
        // There is currently no API to reverse-lookup the fileId (DeepBox nodeId) of a file in a folder by its name,
        // let alone to directly look up the fileId (DeepBox nodeId) by the full path (which is even language-dependent).
        final Deque<Path> segs = this.decompose(file);
        while(!segs.isEmpty()) {
            final Path seg = segs.pop();
            if(StringUtils.isNotBlank(seg.attributes().getFileId())) {
                continue;
            }
            final String cachedSeg = super.getFileId(file);
            if(cachedSeg != null) {
                continue;
            }
            final String ret = this.lookupFileId(seg);
            // fail if one of the segments cannot be found
            if(StringUtils.isEmpty(ret)) {
                throw new NotfoundException(String.format("Cannot find file id for %s", seg.getName()));
            }
        }
        // get from cache now
        return super.getFileId(file);
    }

    private String lookupFileId(final Path file) throws BackgroundException {
        // pre-condition: all parents can be looked up from cache
        try {
            if(containerService.isDeepbox(file)) { // DeepBox
                return this.lookupDeepboxNodeId(file);
            }
            else if(containerService.isBox(file)) { // Box
                final String deepBoxNodeId = this.getFileId(file.getParent());
                return this.lookupBoxNodeId(file, deepBoxNodeId);
            }
            else if(containerService.isThirdLevel(file)) { // 3rd level: Inbox,Documents,Trash
                final String boxNodeId = this.getFileId(file.getParent());
                final String deepBoxNodeId = this.getFileId(file.getParent().getParent());
                if(containerService.isDocuments(file)) {
                    return this.lookupDocumentsNodeId(file, deepBoxNodeId, boxNodeId);
                }
                if(containerService.isInbox(file)) {
                    return this.lookupInboxNodeId(file, deepBoxNodeId, boxNodeId);
                }
                if(containerService.isTrash(file)) {
                    return this.lookupTrashNodeId(file, deepBoxNodeId, boxNodeId);
                }
                return null;
            }
            else if(containerService.isThirdLevel(file.getParent())) { // Inbox,Documents,Trash
                // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with parentNode fails!
                final String boxNodeId = this.getFileId(file.getParent().getParent());
                final String deepBoxNodeId = this.getFileId(file.getParent().getParent().getParent());
                if(containerService.isInDocuments(file)) {
                    return this.lookupFileInDocumentsNodeId(file, deepBoxNodeId, boxNodeId);
                }
                else if(containerService.isInInbox(file)) {
                    return this.lookupFileInInboxNodeId(file, deepBoxNodeId, boxNodeId);
                }
                else if(containerService.isInTrash(file)) {
                    return this.lookupFileInTrashNodeId(file, deepBoxNodeId, boxNodeId);
                }
                return null;
            }
            else { // second+ level under Documents,Trash (Inbox has no hierarchy)
                final String deepBoxNodeId = this.getDeepBoxNodeId(file.getParent());
                final String boxNodeId = this.getBoxNodeId(file.getParent());
                final String parentNodeId = this.getFileId(file.getParent());
                if(containerService.isInDocuments(file)) {
                    return this.lookupFileInDocumentsNodeId(file, deepBoxNodeId, boxNodeId, parentNodeId);
                }
                else if(containerService.isInTrash(file)) {
                    return this.lookupFileInTrashNodeId(file, deepBoxNodeId, boxNodeId, parentNodeId);
                }
                return null;
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(this).map("Failure to read attributes of {0}", e, file);
        }
    }

    private String lookupDeepboxNodeId(final Path file) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final DeepBoxes deepBoxes = rest.listDeepBoxes(offset, chunksize, "displayName asc", null);
            final String deepBoxName = file.getName();
            final String deepBoxNodeId = deepBoxes.getDeepBoxes().stream().filter(db -> DeepboxPathNormalizer.name(db.getName()).equals(deepBoxName)).findFirst().map(db -> db.getDeepBoxNodeId().toString()).orElse(null);
            if(deepBoxNodeId != null) {
                this.cache(file, deepBoxNodeId);
                return deepBoxNodeId;
            }
            size = deepBoxes.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }

    private String lookupBoxNodeId(final Path file, final String deepBoxNodeId) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final Boxes boxes = rest.listBoxes(UUID.fromString(deepBoxNodeId), offset, chunksize, "displayName asc", null);
            final String boxName = file.getName();
            final String boxNodeId = boxes.getBoxes().stream().filter(b -> DeepboxPathNormalizer.name(b.getName()).equals(boxName)).findFirst().map(b -> b.getBoxNodeId().toString()).orElse(null);
            if(boxNodeId != null) {
                this.cache(file, boxNodeId);
                return boxNodeId;
            }
            size = boxes.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }

    // N.B. we can get node id of documents - however, we might not get its nodeinfo or do listfiles from the documents root node, even if boxPolicy.isCanListFilesRoot()==true!
    private String lookupDocumentsNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId) throws ApiException {
        final String documentsId = new BoxRestControllerApi(session.getClient())
                .listFiles(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null)
                .getPath().getSegments().get(0).getNodeId().toString();
        return this.cache(file, documentsId);
    }

    private String lookupInboxNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId) throws ApiException {
        final String inboxId = new BoxRestControllerApi(session.getClient())
                .listQueue(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null, null)
                .getPath().getSegments().get(0).getNodeId().toString();
        return this.cache(file, inboxId);
    }

    private String lookupTrashNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId) throws ApiException {
        final String trashId = new BoxRestControllerApi(session.getClient())
                .listTrash(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null)
                .getPath().getSegments().get(0).getNodeId().toString();
        return this.cache(file, trashId);
    }

    private String lookupFileInDocumentsNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final NodeContent files = rest.listFiles(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    offset, chunksize, "displayName asc");
            final String nodeId = files.getNodes().stream().filter(b -> DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
            if(nodeId != null) {
                this.cache(file, nodeId);
                return nodeId;
            }
            size = files.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }

    private String lookupFileInInboxNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final NodeContent files = rest.listQueue(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    null, offset, chunksize, "displayName asc");
            final String nodeId = files.getNodes().stream().filter(b -> DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
            if(nodeId != null) {
                this.cache(file, nodeId);
                return nodeId;
            }
            size = files.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }

    private String lookupFileInTrashNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final NodeContent files = rest.listTrash(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    offset, chunksize, "displayName asc");
            final String nodeId = files.getNodes().stream().filter(b -> DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
            if(nodeId != null) {
                this.cache(file, nodeId);
                return nodeId;
            }
            size = files.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }

    private String lookupFileInDocumentsNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId, final String parentNodeId) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final NodeContent files = rest.listFiles1(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    UUID.fromString(parentNodeId),
                    offset, chunksize, "displayName asc");
            final String nodeId = files.getNodes().stream().filter(b -> DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
            if(nodeId != null) {
                this.cache(file, nodeId);
                return nodeId;
            }
            size = files.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }

    private String lookupFileInTrashNodeId(final Path file, final String deepBoxNodeId, final String boxNodeId, final String parentNodeId) throws ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int size;
        int offset = 0;
        do {
            final NodeContent files = rest.listTrash1(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    UUID.fromString(parentNodeId),
                    offset, chunksize, "displayName asc");
            final String nodeId = files.getNodes().stream().filter(b -> DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
            if(nodeId != null) {
                this.cache(file, nodeId);
                return nodeId;
            }
            size = files.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return this.cache(file, null);
    }
}
