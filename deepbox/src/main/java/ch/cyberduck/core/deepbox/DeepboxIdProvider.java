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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.CachingFileIdProvider;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.OverviewRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.BoxEntry;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Overview;
import ch.cyberduck.core.deepbox.io.swagger.client.model.PathSegment;
import ch.cyberduck.core.deepcloud.DeepcloudExceptionMappingService;
import ch.cyberduck.core.deepcloud.io.swagger.client.api.UsersApi;
import ch.cyberduck.core.deepcloud.io.swagger.client.model.CompanyRoles;
import ch.cyberduck.core.deepcloud.io.swagger.client.model.UserFull;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.FileIdProvider;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeepboxIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(DeepboxIdProvider.class);

    private final DeepboxSession session;
    private final int chunksize;
    private final DeepboxPathContainerService containerService;

    private static final Pattern SHARED = Pattern.compile("(.*)\\s\\((.*)\\)");

    public DeepboxIdProvider(final DeepboxSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        this.containerService = new DeepboxPathContainerService(session, this);
    }

    public String getCompanyNodeId(final Path file) throws BackgroundException {
        final Path company = containerService.getCompanyPath(file);
        if(null == company) {
            throw new NotfoundException(file.getName());
        }
        return this.getFileId(company);
    }

    public String getDeepBoxNodeId(final Path file) throws BackgroundException {
        final Path normalized = this.normalize(file);
        final Path deepBox = containerService.getDeepboxPath(normalized);
        if(null == deepBox) {
            throw new NotfoundException(normalized.getName());
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

    public String getFourthLevelId(final Path file) throws BackgroundException {
        final Path path = containerService.getFourthLevelPath(file);
        if(null == path) {
            throw new NotfoundException(file.getName());
        }
        return this.getFileId(path);
    }

    protected Path normalize(final Path file) {
        if(!containerService.isInSharedWithMe(file)) {
            return file;
        }
        if(containerService.isSharedWithMe(file)) {
            return file;
        }
        final Deque<Path> segments = this.decompose(file);
        Path result = new Path(String.valueOf(Path.DELIMITER), EnumSet.of(Path.Type.volume, Path.Type.directory));

        while(!segments.isEmpty()) {
            Path segment = segments.pop();
            if(containerService.isSharedWithMe(segment)) {
                final String combined = segments.pop().getName();
                final Matcher matcher = SHARED.matcher(combined);
                if(matcher.matches()) {
                    final String deepboxName = matcher.group(1);
                    final String boxName = matcher.group(2);
                    final EnumSet<AbstractPath.Type> type = EnumSet.copyOf(segment.getType());
                    type.add(AbstractPath.Type.shared);
                    final Path deepbox = new Path(result, deepboxName, type, new PathAttributes(segment.attributes()).withFileId(null));
                    result = new Path(deepbox, boxName, type, segment.attributes());
                }
                else {
                    log.warn("Folder {} does not match pattern {}", combined, SHARED.pattern());
                    return file;
                }
            }
            else {
                final EnumSet<AbstractPath.Type> type = EnumSet.copyOf(segment.getType());
                if(containerService.isInSharedWithMe(segment)) {
                    type.add(AbstractPath.Type.shared);
                }
                result = new Path(result, segment.getName(), type, segment.attributes());
            }
        }
        return result;
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
        final Path normalized = this.normalize(file);
        if(normalized.isRoot()) {
            return null;
        }
        if(StringUtils.isNotBlank(normalized.attributes().getFileId())) {
            return normalized.attributes().getFileId();
        }
        final String cached = super.getFileId(normalized);
        if(cached != null) {
            log.debug("Return cached fileid {} for file {}", cached, normalized);
            return cached;
        }
        // The DeepBox API is ID-based and not path-based.
        // Therefore, we have to iteratively get from/add to cache
        // There is currently no API to reverse-lookup the fileId (DeepBox nodeId) of a file in a folder by its name,
        // let alone to directly look up the fileId (DeepBox nodeId) by the full path (which is even language-dependent).
        final Deque<Path> segments = this.decompose(normalized);
        while(!segments.isEmpty()) {
            final Path segment = segments.pop();
            if(StringUtils.isNotBlank(segment.attributes().getFileId())) {
                continue;
            }
            final String cachedSeg = super.getFileId(segment);
            if(cachedSeg != null) {
                continue;
            }
            final String nodeid = this.cache(segment, this.lookupFileId(segment));
            // fail if one of the segments cannot be found
            if(null == nodeid) {
                throw new NotfoundException(String.format("Cannot find file id for %s", segment.getName()));
            }
        }
        // get from cache now
        return super.getFileId(normalized);
    }

    private String lookupFileId(final Path file) throws BackgroundException {
        // pre-condition: all parents can be looked up from cache
        try {
            if(containerService.isCompany(file)) { // Company
                return new CompanyNodeIdProvider().getFileId(file);
            }
            if(containerService.isDeepbox(file)) { // DeepBox
                return new DelegatingDeepboxNodeIdProvider().getFileId(file);
            }
            else if(containerService.isBox(file)) { // Box
                return new BoxNodeIdProvider().getFileId(file);
            }
            else if(containerService.isFourthLevel(file)) { // 4th level: Inbox,Documents,Trash
                final String boxNodeId = this.getFileId(file.getParent());
                final String deepBoxNodeId = this.getFileId(file.getParent().getParent());
                if(containerService.isDocuments(file)) {
                    // N.B. we can get node id of documents - however, in some cases, we might not get its nodeinfo or do listfiles from
                    // the documents root node, even if boxPolicy.isCanListFilesRoot()==true! In such cases, it may be possible to delete
                    // a file (aka. move to trash) but be unable to list/find the file in the trash afterward.
                    final Optional<PathSegment> documentsId = new BoxRestControllerApi(session.getClient())
                            .listFiles(deepBoxNodeId, boxNodeId, null, null, null)
                            .getPath().getSegments().stream().findFirst();
                    return documentsId.map(PathSegment::getNodeId).orElse(null);
                }
                if(containerService.isInbox(file)) {
                    final Optional<PathSegment> inboxId = new BoxRestControllerApi(session.getClient())
                            .listQueue(deepBoxNodeId, boxNodeId, null, null, null, null)
                            .getPath().getSegments().stream().findFirst();
                    return inboxId.map(PathSegment::getNodeId).orElse(null);
                }
                if(containerService.isTrash(file)) {
                    final Optional<PathSegment> trashId = new BoxRestControllerApi(session.getClient())
                            .listTrash(deepBoxNodeId, boxNodeId, null, null, null)
                            .getPath().getSegments().stream().findFirst();
                    return trashId.map(PathSegment::getNodeId).orElse(null);
                }
                return null;
            }
            else if(containerService.isFourthLevel(file.getParent())) { // Inbox,Documents,Trash
                // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with
                // parentNode may fail!
                final String boxNodeId = this.getFileId(file.getParent().getParent());
                final String deepBoxNodeId = this.getFileId(file.getParent().getParent().getParent());
                if(containerService.isInDocuments(file)) {
                    return new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listFiles(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file);
                }
                else if(containerService.isInInbox(file)) {
                    return new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listQueue(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    null, offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file);
                }
                else if(containerService.isInTrash(file)) {
                    return new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listTrash(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file);
                }
                return null;
            }
            else { // second+ level under Documents,Trash (Inbox has no hierarchy)
                final String deepBoxNodeId = this.getDeepBoxNodeId(file.getParent());
                final String boxNodeId = this.getBoxNodeId(file.getParent());
                final String parentNodeId = this.getFileId(file.getParent());
                if(containerService.isInDocuments(file)) {
                    return new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listFiles1(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    parentNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file);
                }
                else if(containerService.isInTrash(file)) {
                    return new NodeIdProvider(new DeepboxListService.Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listTrash1(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    parentNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }).getFileId(file);
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
                int offset = 0;
                int size;
                do {
                    final NodeContent files = supplier.getNodes(offset);
                    final String nodeId = files.getNodes().stream().filter(b ->
                            DeepboxPathNormalizer.name(b.getDisplayName()).equals(file.getName())).findFirst().map(Node::getNodeId).orElse(null);
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
                final String deepBoxNodeId = DeepboxIdProvider.this.getFileId(file.getParent());
                do {
                    final Boxes boxes = rest.listBoxes(deepBoxNodeId, offset, chunksize, "displayName asc", null);
                    final String boxName = file.getName();
                    final String boxNodeId = boxes.getBoxes().stream().filter(b ->
                            DeepboxPathNormalizer.name(b.getName()).equals(boxName)).findFirst().map(Box::getBoxNodeId).orElse(null);
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

    private final class DelegatingDeepboxNodeIdProvider implements FileIdProvider {
        @Override
        public String getFileId(final Path file) throws BackgroundException {
            if(file.getType().contains(Path.Type.shared)) {
                return new SharedDeepboxNodeIdProvider().getFileId(file);
            }
            return new DeepboxNodeIdProvider().getFileId(file);
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
                    final String deepBoxNodeId = deepBoxes.getDeepBoxes().stream().filter(db ->
                            DeepboxPathNormalizer.name(db.getName()).equals(deepBoxName)).findFirst().map(DeepBox::getDeepBoxNodeId).orElse(null);
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

    private final class SharedDeepboxNodeIdProvider implements FileIdProvider {
        @Override
        public String getFileId(final Path file) throws BackgroundException {
            try {
                final OverviewRestControllerApi rest = new OverviewRestControllerApi(session.getClient());
                final String companyId = DeepboxIdProvider.this.getFileId(file.getParent());
                final Overview overview = rest.getOverview(companyId, 1, null);
                return overview.getSharedWithMe().getBoxes().stream().filter(box ->
                        DeepboxPathNormalizer.name(box.getDeepBoxName()).equals(file.getName())).findFirst().map(BoxEntry::getDeepBoxNodeId).orElse(null);
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(DeepboxIdProvider.this).map("Failure to read attributes of {0}", e, file);
            }
        }
    }

    private final class CompanyNodeIdProvider implements FileIdProvider {
        @Override
        public String getFileId(final Path file) throws BackgroundException {
            try {
                final UsersApi rest = new UsersApi(session.getDeepcloudClient());
                final UserFull user = rest.usersMeList();
                return user.getCompanies().stream().filter(db ->
                        DeepboxPathNormalizer.name(db.getDisplayName()).equals(file.getName())).findFirst().map(CompanyRoles::getGroupId).orElse(null);
            }
            catch(ch.cyberduck.core.deepcloud.io.swagger.client.ApiException e) {
                throw new DeepcloudExceptionMappingService(DeepboxIdProvider.this).map("Failure to read attributes of {0}", e, file);
            }
        }
    }
}
