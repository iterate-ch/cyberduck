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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.deepbox.io.swagger.client.model.PathSegment;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

public class DeepboxIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(DeepboxIdProvider.class);

    private final DeepboxSession session;
    private final int chunksize;
    private final DeepboxPathContainerService containerService;

    public DeepboxIdProvider(final DeepboxSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        this.containerService = new DeepboxPathContainerService(session);
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
            final String nodeid = this.lookupFileId(seg);
            // fail if one of the segments cannot be found
            if(null == nodeid) {
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
                return this.cache(file, new DeepboxNodeIdProvider().getFileId(file));
            }
            else if(containerService.isBox(file)) { // Box
                return this.cache(file, new BoxNodeIdProvider().getFileId(file));
            }
            else if(containerService.isThirdLevel(file)) { // 3rd level: Inbox,Documents,Trash
                final String boxNodeId = this.getFileId(file.getParent());
                final String deepBoxNodeId = this.getFileId(file.getParent().getParent());
                if(containerService.isDocuments(file)) {
                    // N.B. we can get node id of documents - however, in some cases, we might not get its nodeinfo or do listfiles from
                    // the documents root node, even if boxPolicy.isCanListFilesRoot()==true! In such cases, it may be possible to delete
                    // a file (aka. move to trash) but be unable to list/find the file in the trash afterward.
                    final Optional<PathSegment> documentsId = new BoxRestControllerApi(session.getClient())
                            .listFiles(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null)
                            .getPath().getSegments().stream().findFirst();
                    return documentsId.map(pathSegment -> this.cache(file, pathSegment.getNodeId().toString())).orElse(null);
                }
                if(containerService.isInbox(file)) {
                    final Optional<PathSegment> inboxId = new BoxRestControllerApi(session.getClient())
                            .listQueue(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null, null)
                            .getPath().getSegments().stream().findFirst();
                    return inboxId.map(pathSegment -> this.cache(file, pathSegment.getNodeId().toString())).orElse(null);
                }
                if(containerService.isTrash(file)) {
                    final Optional<PathSegment> trashId = new BoxRestControllerApi(session.getClient())
                            .listTrash(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId), null, null, null)
                            .getPath().getSegments().stream().findFirst();
                    return trashId.map(pathSegment -> this.cache(file, pathSegment.getNodeId().toString())).orElse(null);
                }
                return null;
            }
            else if(containerService.isThirdLevel(file.getParent())) { // Inbox,Documents,Trash
                // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with
                // parentNode may fail!
                final UUID boxNodeId = UUID.fromString(this.getFileId(file.getParent().getParent()));
                final UUID deepBoxNodeId = UUID.fromString(this.getFileId(file.getParent().getParent().getParent()));
                if(containerService.isInDocuments(file)) {
                    return this.cache(file, new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listFiles(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file));
                }
                else if(containerService.isInInbox(file)) {
                    return this.cache(file, new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listQueue(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    null, offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file));
                }
                else if(containerService.isInTrash(file)) {
                    return this.cache(file, new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listTrash(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file));
                }
                return null;
            }
            else { // second+ level under Documents,Trash (Inbox has no hierarchy)
                final UUID deepBoxNodeId = UUID.fromString(this.getDeepBoxNodeId(file.getParent()));
                final UUID boxNodeId = UUID.fromString(this.getBoxNodeId(file.getParent()));
                final UUID parentNodeId = UUID.fromString(this.getFileId(file.getParent()));
                if(containerService.isInDocuments(file)) {
                    return this.cache(file, new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listFiles1(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    parentNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file));
                }
                else if(containerService.isInTrash(file)) {
                    return this.cache(file, new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listTrash1(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    parentNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file));
                }
                return null;
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(this).map("Failure to read attributes of {0}", e, file);
        }
    }

    private final class NodeIdProvider implements FileIdProvider {
        private final DeepboxListService.Contents supplier;

        public NodeIdProvider(final DeepboxListService.Contents supplier) {
            this.supplier = supplier;
        }

        @Override
        public String getFileId(final Path file) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                int offset = 0;
                int size;
                do {
                    final NodeContent files = supplier.getNodes(offset);
                    final String nodeId = files.getNodes().stream().filter(b -> DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                    if(nodeId != null) {
                        return nodeId;
                    }
                    size = files.getSize();
                    offset += chunksize;
                }
                while(offset < size);
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(DeepboxIdProvider.this).map("Failure to read attributes of {0}", e, file);
            }
            return null;
        }
    }

    private final class BoxNodeIdProvider implements FileIdProvider {
        @Override
        public String getFileId(final Path file) throws BackgroundException {
            try {
                final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
                int size;
                int offset = 0;
                final UUID deepBoxNodeId = UUID.fromString(DeepboxIdProvider.this.getFileId(file.getParent()));
                do {
                    final Boxes boxes = rest.listBoxes(deepBoxNodeId, offset, chunksize, "displayName asc", null);
                    final String boxName = file.getName();
                    final String boxNodeId = boxes.getBoxes().stream().filter(b -> DeepboxPathNormalizer.name(b.getName()).equals(boxName)).findFirst().map(b -> b.getBoxNodeId().toString()).orElse(null);
                    if(boxNodeId != null) {
                        return boxNodeId;
                    }
                    size = boxes.getSize();
                    offset += chunksize;
                }
                while(offset < size);
                return null;
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(DeepboxIdProvider.this).map("Failure to read attributes of {0}", e, file);
            }
        }
    }

    private final class DeepboxNodeIdProvider implements FileIdProvider {
        @Override
        public String getFileId(final Path file) throws BackgroundException {
            try {
                final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
                int size;
                int offset = 0;
                do {
                    final DeepBoxes deepBoxes = rest.listDeepBoxes(offset, chunksize, "displayName asc", null);
                    final String deepBoxName = file.getName();
                    final String deepBoxNodeId = deepBoxes.getDeepBoxes().stream().filter(db -> DeepboxPathNormalizer.name(db.getName()).equals(deepBoxName)).findFirst().map(db -> db.getDeepBoxNodeId().toString()).orElse(null);
                    if(deepBoxNodeId != null) {
                        return deepBoxNodeId;
                    }
                    size = deepBoxes.getSize();
                    offset += chunksize;
                }
                while(offset < size);
                return null;

            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(DeepboxIdProvider.this).map("Failure to read attributes of {0}", e, file);
            }
        }
    }
}
