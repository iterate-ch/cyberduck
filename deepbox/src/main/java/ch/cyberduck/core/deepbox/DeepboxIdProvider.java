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
import ch.cyberduck.core.features.FileIdProvider;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.DOCUMENTS;
import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.INBOX;

public class DeepboxIdProvider extends CachingFileIdProvider implements FileIdProvider {
    private static final Logger log = LogManager.getLogger(DeepboxIdProvider.class);

    private final DeepboxSession session;

    // TODO add test

    public DeepboxIdProvider(final DeepboxSession session) {
        super(session.getCaseSensitivity());
        this.session = session;
    }

    public String getDeepBoxNodeId(final Path file) throws BackgroundException {
        final List<Path> segs = pathToList(file);
        if(segs.size() < 1) {
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
            if(file.isRoot()) {
                return null;
            }
            else {
                // recursively cache file attributes
                final String parentNodeId = getFileId(file.getParent());

                // lookup as now everything recursively cached and we are not in cache
                // TODO alternatively, we could use https://apidocs.deepcloud.swiss/deepbox-api-docs/index.html#info - would that simplify the implementation?
                // The node info endpoint can be used to obtain further details about a specified file or folder node. For example, if only the "{nodeId}" is known, the details of the "deepBoxNodeId" and "boxNodeId" can be obtained to assist with the file or folder navigation, or other queries requiring the "deepBoxNodeId" and "boxNodeId".
                if(new DeepboxPathContainerService().isDeepbox(file)) { // DeepBox
                    final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                    // TODO pagination? Bad code smell - duplication with list service?
                    final DeepBoxes deepBoxes = api.listDeepBoxes(0, 50, "displayName asc", null);
                    final String deepBoxName = file.getName();
                    final String deepBoxNodeId = deepBoxes.getDeepBoxes().stream().filter(db -> db.getName().equals(deepBoxName)).findFirst().map(db -> db.getDeepBoxNodeId().toString()).orElse(null);
                    this.cache(file, deepBoxNodeId);
                    return deepBoxNodeId;
                }
                else if(new DeepboxPathContainerService().isBox(file)) { // Box
                    final String deepBoxNodeId = getFileId(file.getParent());
                    final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());

                    final Boxes boxes = api.listBoxes(UUID.fromString(deepBoxNodeId), 0, 50, "displayName asc", null);
                    final String boxName = file.getName();
                    final String boxNodeId = boxes.getBoxes().stream().filter(b -> b.getName().equals(boxName)).findFirst().map(b -> b.getBoxNodeId().toString()).orElse(null);
                    this.cache(file, boxNodeId);
                    return boxNodeId;
                }
                else if(new DeepboxPathContainerService().isThirdLevel(file)) { // 3rd level: Inbox,Documents,Trash
                    final String boxNodeId = getFileId(file.getParent());
                    final String thirdLevelFileId = String.format("%s_%s", boxNodeId, file.getName());
                    this.cache(file, thirdLevelFileId);
                    return thirdLevelFileId;
                }
                else if(new DeepboxPathContainerService().isThirdLevel(file.getParent())) { // first level under Inbox,Documents,Trash
                    final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
                    final String thirdLevelId = getFileId(file.getParent());
                    final String boxNodeId = getFileId(file.getParent().getParent());
                    final String deepBoxNodeId = getFileId(file.getParent().getParent().getParent());
                    if(thirdLevelId.endsWith(DOCUMENTS)) {
                        final NodeContent files = api.listFiles(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                0, 50, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                    else if(thirdLevelId.endsWith(INBOX)) {
                        final NodeContent files = api.listQueue(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                null, 0, 50, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                    else {
                        final NodeContent files = api.listTrash(
                                UUID.fromString(deepBoxNodeId),
                                UUID.fromString(boxNodeId),
                                0, 50, "displayName asc");
                        final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                        this.cache(file, nodeId);
                        return nodeId;
                    }
                }
                else { // second+ level under Documents,Trash (Inbox has no hierarchy)
                    final String deepBoxNodeId = getDeepBoxNodeId(file.getParent());
                    final String boxNodeId = getBoxNodeId(file.getParent());

                    final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());

                    final NodeContent files = api.listTrash1(
                            UUID.fromString(deepBoxNodeId),
                            UUID.fromString(boxNodeId),
                            UUID.fromString(parentNodeId),
                            0, 50, "displayName asc");
                    final String nodeId = files.getNodes().stream().filter(b -> b.getName().equals(file.getName())).findFirst().map(b -> b.getNodeId().toString()).orElse(null);
                    this.cache(file, nodeId);
                    return nodeId;
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
