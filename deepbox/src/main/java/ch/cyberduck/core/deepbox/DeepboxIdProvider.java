package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.features.FileIdProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxListService.DOCUMENTS;
import static ch.cyberduck.core.deepbox.DeepboxListService.INBOX;

public class DeepboxIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(DeepboxIdProvider.class);

    private final DeepboxSession session;
    private final BoxRestControllerApi api;

    public DeepboxIdProvider(final DeepboxSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
        this.api = new BoxRestControllerApi(this.session.getClient());
    }

    public String getDeepBoxNodeId(final Path file) throws BackgroundException {
        Path deepBox = file;
        if(deepBox.isRoot()) {
            return null;
        }
        while(!deepBox.getParent().isRoot()) {
            deepBox = deepBox.getParent();
        }
        final String cachedFileId = super.getFileId(deepBox);
        if(cachedFileId != null) {
            return cachedFileId;
        }
        try {
            final DeepBoxes deepBoxes = api.listDeepBoxes(0, 50, "asc", null);
            final String deepBoxName = deepBox.getName();
            final String deepBoxNodeId = deepBoxes.getDeepBoxes().stream().filter(db -> db.getName().equals(deepBoxName)).findFirst().map(db -> db.getDeepBoxNodeId().toString()).orElse(null);
            this.cache(deepBox, deepBoxNodeId);
            return deepBoxNodeId;
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(this).map(e);
        }
    }

    public String getBoxId(final Path file) throws BackgroundException {
        Path box = file;
        if(box.isRoot()) {
            return null;
        }
        if(box.getParent().isRoot()) {
            return null;
        }
        while(!box.getParent().getParent().isRoot()) {
            box = box.getParent();
        }
        final String cachedFileId = super.getFileId(box);
        if(cachedFileId != null) {
            return cachedFileId;
        }
        try {
            final String deepBoxNodeId = getDeepBoxNodeId(file);
            final Boxes boxes = api.listBoxes(UUID.fromString(deepBoxNodeId), 0, 50, "asc", null);
            final String boxName = box.getName();
            final String boxNodeId = boxes.getBoxes().stream().filter(b -> b.getName().equals(boxName)).findFirst().map(b -> b.getBoxNodeId().toString()).orElse(null);
            this.cache(box, boxNodeId);
            return boxNodeId;
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(this).map(e);
        }
    }

    public String getThirdLevelId(final Path file) throws BackgroundException {
        Path thirdLevel = file;
        if(thirdLevel.isRoot()) {
            return null;
        }
        if(thirdLevel.getParent().isRoot()) {
            return null;
        }
        if(thirdLevel.getParent().getParent().isRoot()) {
            return null;
        }
        while(!thirdLevel.getParent().getParent().getParent().isRoot()) {
            thirdLevel = thirdLevel.getParent();
        }
        final String cachedFileId = super.getFileId(thirdLevel);
        if(cachedFileId != null) {
            return cachedFileId;
        }
        final String thirdLevelFileId = String.format("%s_%s", getBoxId(file), thirdLevel.getName());
        this.cache(thirdLevel, thirdLevelFileId);
        return thirdLevelFileId;
    }

    @Override
    public String getFileId(final Path file) throws BackgroundException {
        try {
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

            final String id;
            if(file.isRoot()) {
                return null;
            }
            else {
                // recursively cache file attributes
                final String parentNodeId = getFileId(file.getParent());

                // retrieve the relevant IDs
                final String deepBoxNodeId = getDeepBoxNodeId(file);
                final String boxNodeId = getBoxId(file);
                final String thirdLevelId = getThirdLevelId(file);

                // lookup as now everything recursively cached
                if(file.getParent().isRoot()) { // DeepBox
                    return deepBoxNodeId;
                }
                else if(file.getParent().getParent().isRoot()) { // Box
                    return boxNodeId;
                }
                else if(file.getParent().getParent().getParent().isRoot()) { // 3rd level: Inbox,Documents,Trash
                    return thirdLevelId;
                }
                else if(file.getParent().getParent().getParent().getParent().isRoot()) { // first level under Inbox,Documents,Trash
                    if(thirdLevelId.endsWith(DOCUMENTS)) {
                        final NodeContent files = api.listFiles(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                0, 50, "asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                    else if(thirdLevelId.endsWith(INBOX)) {
                        final NodeContent files = api.listQueue(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                null, 0, 50, "asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                    else {
                        final NodeContent files = api.listTrash(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                0, 50, "asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                }
                else { // second+ level under Documents,Trash (Inbox has no hierarchy)
                    if(thirdLevelId.endsWith(DOCUMENTS)) {
                        final NodeContent files = api.listFiles1(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                UUID.fromString(parentNodeId),
                                0, 50, "asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                    else {
                        final NodeContent files = api.listTrash1(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                UUID.fromString(parentNodeId),
                                0, 50, "asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                }
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(this).map("Failure to read attributes of {0}", e, file);
        }
    }

    /**
     * Mapping of path "/Home/mduck" to "My files"
     * Mapping of path "/Common" to "Common files"
     */
    protected String getPrefixedPath(final Path file) {
        /*
        final PathContainerService service = new DefaultPathContainerService();
        final String name = new DefaultPathContainerService().getContainer(file).getName();
        for(RootFolder r : session.roots()) {
            if(StringUtils.equalsIgnoreCase(name, PathNormalizer.name(r.getPath()))
                    || StringUtils.equalsIgnoreCase(name, PathNormalizer.name(r.getName()))) {
                if(service.isContainer(file)) {
                    return r.getPath();
                }
                return String.format("%s/%s", r.getPath(), PathRelativizer.relativize(name, file.getAbsolute()));
            }
        }
        return file.getAbsolute();
         */
        return null;
    }
}
