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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import java.util.EnumSet;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.*;

public class DeepboxListService implements ListService {
    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final int chunksize;
    private final DeepboxAttributesFinderFeature attributes;

    public DeepboxListService(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        this.attributes = new DeepboxAttributesFinderFeature(session, fileid);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        final String deepBoxNodeId = fileid.getDeepBoxNodeId(directory);
        final String boxNodeId = fileid.getBoxNodeId(directory);
        final String thirdLevelId = fileid.getThirdLevelId(directory);
        try {
            final BoxRestControllerApi api = new BoxRestControllerApi(this.session.getClient());
            if(directory.isRoot()) {
                final DeepBoxes deepBoxes = api.listDeepBoxes(0, 50, "asc", null);
                for(final DeepBox deepBox : deepBoxes.getDeepBoxes()) {
                    list.add(new Path(directory, PathNormalizer.name(deepBox.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                            attributes.toAttributes(deepBox))
                    );
                }
            }
            else if(new DeepboxPathContainerService().isDeepbox(directory)) { // in DeepBox
                final Boxes boxes = api.listBoxes(UUID.fromString(directory.attributes().getFileId()), 0, 50, "asc", null);
                for(final Box box : boxes.getBoxes()) {
                    list.add(new Path(directory, PathNormalizer.name(box.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                            attributes.toAttributes(box))
                    );
                }
            }
            else if(new DeepboxPathContainerService().isBox(directory)) { // in Box
                // TODO i18n
                list.add(new Path(directory, PathNormalizer.name(INBOX), EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                        new PathAttributes().withFileId(String.format("%s_%s", boxNodeId, INBOX))
                ));
                list.add(new Path(directory, PathNormalizer.name(DOCUMENTS), EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                        new PathAttributes().withFileId(String.format("%s_%s", boxNodeId, DOCUMENTS))
                ));
                list.add(new Path(directory, PathNormalizer.name(TRASH), EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                        new PathAttributes().withFileId(String.format("%s_%s", boxNodeId, TRASH))
                ));
            }
            else if(new DeepboxPathContainerService().isThirdLevel(directory)) { // in Inbox/Documents/Trash
                if(thirdLevelId.endsWith(INBOX)) {
                    final NodeContent inbox = api.listQueue(UUID.fromString(deepBoxNodeId),
                            UUID.fromString(boxNodeId),
                            null,
                            0, 50, "asc");
                    for(Node node : inbox.getNodes()) {
                        list.add(new Path(directory, PathNormalizer.name(node.getName()), EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory))
                                .withAttributes(attributes.toAttributes(node)));
                    }
                }
                else if(thirdLevelId.endsWith(DOCUMENTS)) {
                    final NodeContent files = api.listFiles(
                            UUID.fromString(deepBoxNodeId),
                            UUID.fromString(boxNodeId),
                            0, 50, "asc"
                    );
                    for(final Node node : files.getNodes()) {
                        list.add(new Path(directory, PathNormalizer.name(node.getName()), EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory))
                                .withAttributes(attributes.toAttributes(node)));
                    }
                }
                else if(thirdLevelId.endsWith(TRASH)) {
                    final NodeContent trashFiles = api.listTrash(
                            UUID.fromString(deepBoxNodeId),
                            UUID.fromString(boxNodeId),
                            0, 50, "asc"
                    );
                    for(final Node node : trashFiles.getNodes()) {
                        list.add(new Path(directory, PathNormalizer.name(node.getName()), EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory))
                                .withAttributes(attributes.toAttributes(node)));
                    }
                }
            }
            else { // in subfolder in Documents/Trash (Inbox has no subfolders)
                final String nodeId = fileid.getFileId(directory);
                if(thirdLevelId.endsWith(DOCUMENTS)) {
                    final NodeContent files = api.listFiles1(
                            UUID.fromString(deepBoxNodeId),
                            UUID.fromString(boxNodeId),
                            UUID.fromString(nodeId),
                            0, 50, "asc"
                    );
                    for(final Node node : files.getNodes()) {
                        list.add(new Path(directory, PathNormalizer.name(node.getName()), EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory))
                                .withAttributes(attributes.toAttributes(node)));
                    }
                }
                else {
                    final NodeContent files = api.listTrash1(
                            UUID.fromString(deepBoxNodeId),
                            UUID.fromString(boxNodeId),
                            UUID.fromString(nodeId),
                            0, 50, "asc"
                    );
                    for(final Node node : files.getNodes()) {
                        list.add(new Path(directory, PathNormalizer.name(node.getName()), EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory))
                                .withAttributes(attributes.toAttributes(node)));
                    }
                }
            }
        }
        catch(ApiException e) {
            throw new BackgroundException(e);
        }
        return list;
    }
}
