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
import ch.cyberduck.core.Acl;
import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANLISTCHILDREN;

public class DeepboxListService implements ListService {
    private static final Logger log = LogManager.getLogger(DeepboxListService.class);

    public static final String INBOX = "Inbox";
    public static final String DOCUMENTS = "Documents";
    public static final String TRASH = "Trash";

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final DeepboxAttributesFinderFeature attributes;
    private final int chunksize;
    private final DeepboxPathContainerService containerService;

    public DeepboxListService(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.attributes = new DeepboxAttributesFinderFeature(session, fileid);
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
        this.containerService = new DeepboxPathContainerService(session);
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            if(directory.isRoot()) {
                return this.listDeepBoxes(directory, listener);
            }
            if(containerService.isDeepbox(directory)) { // in DeepBox
                return this.listBoxes(directory, listener);
            }
            if(containerService.isBox(directory)) { // in Box
                return this.listBox(directory, listener);
            }
            final UUID deepBoxNodeId = UUID.fromString(fileid.getDeepBoxNodeId(directory));
            final UUID boxNodeId = UUID.fromString(fileid.getBoxNodeId(directory));
            if(containerService.isThirdLevel(directory)) { // in Inbox/Documents/Trash
                // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with
                // parentNode may fail!
                if(containerService.isInInbox(directory)) {
                    return duplicates(this.list(directory, listener, new Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listQueue(deepBoxNodeId,
                                    boxNodeId,
                                    null,
                                    offset, chunksize, "displayName asc");
                        }
                    }));
                }
                if(containerService.isInDocuments(directory)) {
                    return duplicates(this.list(directory, listener, new Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listFiles(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }));
                }
                if(containerService.isInTrash(directory)) {
                    return duplicates(this.list(directory, listener, new Contents() {
                        @Override
                        public NodeContent getNodes(final int offset) throws ApiException {
                            return new BoxRestControllerApi(session.getClient()).listTrash(
                                    deepBoxNodeId,
                                    boxNodeId,
                                    offset, chunksize, "displayName asc");
                        }
                    }));
                }
            }
            // in subfolder of  Documents/Trash (Inbox has no subfolders)
            final String nodeId = fileid.getFileId(directory);
            if(containerService.isInTrash(directory)) {
                return duplicates(this.list(directory, listener, new Contents() {
                    @Override
                    public NodeContent getNodes(final int offset) throws ApiException {
                        return new BoxRestControllerApi(session.getClient()).listTrash1(
                                deepBoxNodeId,
                                boxNodeId,
                                UUID.fromString(nodeId),
                                offset, chunksize, "displayName asc");
                    }
                }));
            }
            return duplicates(this.list(directory, listener, new Contents() {
                @Override
                public NodeContent getNodes(final int offset) throws ApiException {
                    return new BoxRestControllerApi(session.getClient()).listFiles1(
                            deepBoxNodeId,
                            boxNodeId,
                            UUID.fromString(nodeId),
                            offset, chunksize, "displayName asc");
                }
            }));
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
        }
    }

    private interface Contents {
        NodeContent getNodes(int offset) throws ApiException;
    }

    private static AttributedList<Path> duplicates(final AttributedList<Path> list) {
        list.toStream().forEach(f -> f.attributes().setDuplicate(list.findAll(new SimplePathPredicate(f)).size() != 1));
        return list;
    }

    private AttributedList<Path> list(final Path directory, final ListProgressListener listener, final Contents supplier) throws ApiException, ConnectionCanceledException {
        final AttributedList<Path> list = new AttributedList<>();
        int offset = 0;
        int size;
        do {
            final NodeContent files = supplier.getNodes(offset);
            for(final Node node : files.getNodes()) {
                list.add(new Path(directory, DeepboxPathNormalizer.name(node.getDisplayName()),
                        EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory)).withAttributes(attributes.toAttributes(node)));
            }
            listener.chunk(directory, list);
            size = files.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return list;
    }

    private AttributedList<Path> listBox(final Path directory, final ListProgressListener listener) throws ApiException, BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        final String deepBoxNodeId = fileid.getDeepBoxNodeId(directory);
        final String boxNodeId = fileid.getBoxNodeId(directory);
        final Box box = rest.getBox(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId));
        if(box.getBoxPolicy().isCanListQueue()) {
            final String inboxName = session.getPinnedLocalization(INBOX);
            final Path inbox = new Path(directory, inboxName, EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new PathAttributes().withFileId(fileid.getFileId(new Path(directory, inboxName, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume))))
            );
            list.add(inbox.withAttributes(attributes.find(inbox)));
        }
        if(box.getBoxPolicy().isCanListFilesRoot()) {
            final String documentsName = session.getPinnedLocalization(DOCUMENTS);
            final Path documents = new Path(directory, documentsName, EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new PathAttributes().withFileId(fileid.getFileId(new Path(directory, documentsName, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume))))
            );
            list.add(documents.withAttributes(attributes.find(documents)));
        }
        if(box.getBoxPolicy().isCanAccessTrash()) {
            final String trashName = session.getPinnedLocalization(TRASH);
            final Path trash = new Path(directory, trashName, EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new PathAttributes().withFileId(fileid.getFileId(new Path(directory, trashName, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume))))
            );
            list.add(trash.withAttributes(attributes.find(trash)));
        }
        listener.chunk(directory, list);
        return list;
    }

    private AttributedList<Path> listBoxes(final Path directory, final ListProgressListener listener) throws ApiException, BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final Boxes boxes = rest.listBoxes(UUID.fromString(fileid.getFileId(directory)), offset, chunksize, "name asc", null);
            for(final Box box : boxes.getBoxes()) {
                list.add(new Path(directory, DeepboxPathNormalizer.name(box.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                        attributes.toAttributes(box))
                );
            }
            listener.chunk(directory, list);
            size = boxes.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return list;
    }

    private AttributedList<Path> listDeepBoxes(final Path directory, final ListProgressListener listener) throws ApiException, ConnectionCanceledException {
        final AttributedList<Path> list = new AttributedList<>();
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final DeepBoxes deepBoxes = rest.listDeepBoxes(offset, chunksize, "name asc", null);
            for(final DeepBox deepBox : deepBoxes.getDeepBoxes()) {
                list.add(new Path(directory, DeepboxPathNormalizer.name(deepBox.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                        attributes.toAttributes(deepBox))
                );
            }
            listener.chunk(directory, list);
            size = deepBoxes.getSize();
            offset += chunksize;
        }
        while(offset < size);
        return list;
    }

    @Override
    public void preflight(final Path directory) throws BackgroundException {
        final Acl acl = directory.attributes().getAcl();
        if(Acl.EMPTY == acl) {
            // Missing initialization
            log.warn(String.format("Unknown ACLs on %s", directory));
            return;
        }
        if(!acl.get(new Acl.CanonicalUser()).contains(CANLISTCHILDREN)) {
            if(log.isWarnEnabled()) {
                log.warn(String.format("ACL %s for %s does not include %s", acl, directory, CANLISTCHILDREN));
            }
            throw new AccessDeniedException(MessageFormat.format(LocaleFactory.localizedString("Cannot download {0}", "Error"), directory.getName())).withFile(directory);
        }
    }
}
