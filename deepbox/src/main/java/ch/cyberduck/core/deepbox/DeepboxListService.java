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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANLISTCHILDREN;

public class DeepboxListService implements ListService {
    private static final Logger log = LogManager.getLogger(DeepboxListService.class);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final DeepboxAttributesFinderFeature attributes;
    private final int chunksize;
    private final DeepboxPathContainerService containerService = new DeepboxPathContainerService();

    public DeepboxListService(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.attributes = new DeepboxAttributesFinderFeature(session, fileid);
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        final AttributedList<Path> list = new AttributedList<>();
        final HashSet<String> closed = new HashSet<>();
        try {
            if(directory.isRoot()) {
                this.listDeepBoxes(directory, listener, list);
            }
            else {
                final String deepBoxNodeId = fileid.getDeepBoxNodeId(directory);
                if(containerService.isDeepbox(directory)) { // in DeepBox
                    this.listBoxes(directory, listener, list);
                }
                else {
                    final String boxNodeId = fileid.getBoxNodeId(directory);
                    if(containerService.isBox(directory)) { // in Box
                        this.listBox(directory, listener, deepBoxNodeId, boxNodeId, list);
                    }
                    else if(containerService.isThirdLevel(directory)) { // in Inbox/Documents/Trash
                        // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with parentNode fails!
                        if(containerService.isInInbox(directory)) {
                            this.listQueue(directory, listener, deepBoxNodeId, boxNodeId, list, closed);
                        }
                        else if(containerService.isInDocuments(directory)) {
                            this.listFiles(directory, listener, deepBoxNodeId, boxNodeId, list, closed);
                        }
                        else if(containerService.isInTrash(directory)) {
                            this.listTrash(directory, listener, deepBoxNodeId, boxNodeId, list, closed);
                        }
                    }
                    else { // in subfolder of  Documents/Trash (Inbox has no subfolders)
                        final String nodeId = fileid.getFileId(directory);
                        if(containerService.isInDocuments(directory)) {
                            this.listFiles(directory, listener, deepBoxNodeId, boxNodeId, nodeId, list, closed);
                        }
                        else if(containerService.isInTrash(directory)) {
                            this.listTrash(directory, listener, deepBoxNodeId, boxNodeId, nodeId, list, closed);
                        }
                    }
                }
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
        }
        return list;
    }

    private void listTrash(final Path directory, final ListProgressListener listener, final String deepBoxNodeId, final String boxNodeId, final String nodeId, final AttributedList<Path> list, final HashSet<String> closed) throws ApiException, ConnectionCanceledException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final NodeContent files = rest.listTrash1(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    UUID.fromString(nodeId),
                    offset, this.chunksize, "displayName asc"
            );
            this.listChunk(directory, files, list, closed);
            listener.chunk(directory, list);
            size = files.getSize();
            offset += this.chunksize;
        }
        while(offset < size);
    }

    private void listFiles(final Path directory, final ListProgressListener listener, final String deepBoxNodeId, final String boxNodeId, final String nodeId, final AttributedList<Path> list, final HashSet<String> closed) throws ApiException, ConnectionCanceledException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final NodeContent files = rest.listFiles1(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    UUID.fromString(nodeId),
                    offset, this.chunksize, "displayName asc"
            );
            this.listChunk(directory, files, list, closed);
            listener.chunk(directory, list);
            size = files.getSize();
            offset += this.chunksize;
        }
        while(offset < size);
    }

    private void listTrash(final Path directory, final ListProgressListener listener, final String deepBoxNodeId, final String boxNodeId, final AttributedList<Path> list, final HashSet<String> closed) throws ApiException, ConnectionCanceledException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final NodeContent trashFiles = rest.listTrash(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    offset, this.chunksize, "displayName asc"
            );
            this.listChunk(directory, trashFiles, list, closed);
            listener.chunk(directory, list);
            size = trashFiles.getSize();
            offset += this.chunksize;
        }
        while(offset < size);
    }

    private void listFiles(final Path directory, final ListProgressListener listener, final String deepBoxNodeId, final String boxNodeId, final AttributedList<Path> list, final HashSet<String> closed) throws ApiException, ConnectionCanceledException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final NodeContent files = rest.listFiles(
                    UUID.fromString(deepBoxNodeId),
                    UUID.fromString(boxNodeId),
                    offset, this.chunksize, "displayName asc"
            );
            this.listChunk(directory, files, list, closed);
            listener.chunk(directory, list);
            size = files.getSize();
            offset += this.chunksize;
        }
        while(offset < size);
    }

    private void listQueue(final Path directory, final ListProgressListener listener, final String deepBoxNodeId, final String boxNodeId, final AttributedList<Path> list, final HashSet<String> closed) throws ConnectionCanceledException, ApiException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size = 0;
        do {
            try {
                final NodeContent inbox = rest.listQueue(UUID.fromString(deepBoxNodeId),
                        UUID.fromString(boxNodeId),
                        null,
                        offset, this.chunksize, "displayName asc");
                this.listChunk(directory, inbox, list, closed);
                listener.chunk(directory, list);
                size = inbox.getSize();
                offset += this.chunksize;
            }
            catch(ApiException e) {
                if(e.getCode() != 403) {
                    throw e;
                }
                // inbox not visible if 403
            }
        }
        while(offset < size);
    }

    private void listBox(final Path directory, final ListProgressListener listener, final String deepBoxNodeId, final String boxNodeId, final AttributedList<Path> list) throws ApiException, BackgroundException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        final Box box = rest.getBox(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId));
        if(box.getBoxPolicy().isCanListQueue()) {
            final String inboxName = DeepboxPathNormalizer.name(LocaleFactory.localizedString("Inbox", "Deepbox"));
            final Path inbox = new Path(directory, inboxName, EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new PathAttributes().withFileId(fileid.getFileId(new Path(directory, inboxName, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume))))
            );
            list.add(inbox.withAttributes(attributes.find(inbox)));
        }
        if(box.getBoxPolicy().isCanListFilesRoot()) {
            final String documentsName = DeepboxPathNormalizer.name(LocaleFactory.localizedString("Documents", "Deepbox"));
            final Path documents = new Path(directory, documentsName, EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new PathAttributes().withFileId(fileid.getFileId(new Path(directory, documentsName, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume))))
            );
            list.add(documents.withAttributes(attributes.find(documents)));
        }
        if(box.getBoxPolicy().isCanAccessTrash()) {
            final String trashName = DeepboxPathNormalizer.name(LocaleFactory.localizedString("Trash", "Deepbox"));
            final Path trash = new Path(directory, trashName, EnumSet.of(Path.Type.directory, Path.Type.volume)).withAttributes(
                    new PathAttributes().withFileId(fileid.getFileId(new Path(directory, trashName, EnumSet.of(AbstractPath.Type.directory, AbstractPath.Type.volume))))
            );
            list.add(trash.withAttributes(attributes.find(trash)));
        }
        listener.chunk(directory, list);
    }

    private void listBoxes(final Path directory, final ListProgressListener listener, final AttributedList<Path> list) throws ApiException, ConnectionCanceledException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final Boxes boxes = rest.listBoxes(UUID.fromString(directory.attributes().getFileId()), offset, this.chunksize, "name asc", null);
            for(final Box box : boxes.getBoxes()) {
                list.add(new Path(directory, DeepboxPathNormalizer.name(box.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                        attributes.toAttributes(box))
                );
            }
            listener.chunk(directory, list);
            size = boxes.getSize();
            offset += this.chunksize;
        }
        while(offset < size);
    }

    private void listDeepBoxes(final Path directory, final ListProgressListener listener, final AttributedList<Path> list) throws ApiException, ConnectionCanceledException {
        final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
        int offset = 0;
        int size;
        do {
            final DeepBoxes deepBoxes = rest.listDeepBoxes(offset, this.chunksize, "name asc", null);
            for(final DeepBox deepBox : deepBoxes.getDeepBoxes()) {
                list.add(new Path(directory, DeepboxPathNormalizer.name(deepBox.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                        attributes.toAttributes(deepBox))
                );
            }
            listener.chunk(directory, list);
            size = deepBoxes.getSize();
            offset += this.chunksize;
        }
        while(offset < size);
    }

    // Hide duplicates in listing.
    // Due to path normalization, paths might not come in order despite remote listing by "name asc"
    private void listChunk(final Path directory, final NodeContent inbox, final AttributedList<Path> list, final Set<String> closed) throws ApiException {
        for(final Node node : inbox.getNodes()) {
            final String name = DeepboxPathNormalizer.name(node.getDisplayName());
            final Path path = new Path(directory, name, EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory))
                    .withAttributes(attributes.toAttributes(node));
            // remove duplicates
            if(!closed.contains(name)) {
                list.add(path);
                fileid.cache(path, node.getNodeId().toString());
            }
            else {
                // remove from list and cache
                final Path last = list.get(list.size() - 1);
                if(last.getName().equals(name)) {
                    // Usually even after path normalization, the last element in the list should be the duplicate due to listing by file name.
                    list.remove(last);
                    fileid.cache(last, null);
                }
                else {
                    // Due to path normalization, the path to remove might not be the last one in the listing.
                    // Should be very rare, so searching the list O(n) should be fine.
                    final Path previous = list.find(p -> p.getName().equals(name));
                    if(previous != null) {
                        list.remove(previous);
                        fileid.cache(previous, null);
                    }
                }
            }
            closed.add(name);
        }
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
