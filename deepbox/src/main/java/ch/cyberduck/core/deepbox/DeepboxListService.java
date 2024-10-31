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
import ch.cyberduck.core.deepbox.io.swagger.client.api.OverviewRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.BoxEntry;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Overview;
import ch.cyberduck.core.deepcloud.DeepcloudExceptionMappingService;
import ch.cyberduck.core.deepcloud.io.swagger.client.api.UsersApi;
import ch.cyberduck.core.deepcloud.io.swagger.client.model.CompanyRoles;
import ch.cyberduck.core.deepcloud.io.swagger.client.model.StructureEnum;
import ch.cyberduck.core.deepcloud.io.swagger.client.model.UserFull;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static ch.cyberduck.core.deepbox.DeepboxAttributesFinderFeature.CANLISTCHILDREN;

/**
 * List files in DeepBox
 * <p>
 * Structure:
 * <p>
 * /
 * ├── company 1
 * │   ├── deepbox 1
 * │   │   ├── mybox 1
 * │   │   │   ├── inbox
 * │   │   │   │   ├── file1.txt
 * │   │   │   │   ├── folder1
 * │   │   │   │   └── ...
 * │   │   │   ├── documents
 * │   │   │   │   ├── template-folder1
 * │   │   │   │   ├── template-folder2
 * │   │   │   │   ├── ...
 * │   │   │   │   └── template-foldern
 * │   │   │   └── trash
 * │   │   │       └── ...
 * │   │   └── mybox 2
 * │   ├── deepbox 2
 * │   │   └── mybox 3
 * │   └── boxes shared with me
 * │       ├── deepbox 77 (box 65)
 * │       ├── deepbox 77 (box 67)
 * │       └── deepbox 89 (box 78)
 * └── company 29
 * └── ....
 */
public class DeepboxListService implements ListService {
    private static final Logger log = LogManager.getLogger(DeepboxListService.class);

    public static final String INBOX = "Inbox";
    public static final String DOCUMENTS = "Documents";
    public static final String TRASH = "Trash";
    public static final String SHARED = "Boxes shared with me";
    public static final List<String> VIRTUALFOLDERS = Arrays.asList(INBOX, DOCUMENTS, TRASH, SHARED);

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final DeepboxAttributesFinderFeature attributes;
    private final DeepboxPathContainerService containerService;
    private final int chunksize;

    public DeepboxListService(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
        this.attributes = new DeepboxAttributesFinderFeature(session, fileid);
        this.containerService = new DeepboxPathContainerService(session, fileid);
        this.chunksize = new HostPreferences(session.getHost()).getInteger("deepbox.listing.chunksize");
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        if(directory.isRoot()) {
            return new CompanyListService().list(directory, listener);
        }
        if(containerService.isSharedWithMe(directory)) { // in Boxes shared with me
            return new SharedWithMeListService(fileid.getCompanyNodeId(directory)).list(directory, listener);
        }
        if(containerService.isCompany(directory)) { // in Company
            return new DeepBoxesListService().list(directory, listener);
        }
        if(containerService.isDeepbox(directory)) { // in DeepBox
            return new BoxesListService().list(directory, listener);
        }
        if(containerService.isBox(directory)) { // in Box
            return new BoxListService().list(directory, listener);
        }
        final String deepBoxNodeId = fileid.getDeepBoxNodeId(directory);
        final String boxNodeId = fileid.getBoxNodeId(directory);
        if(containerService.isFourthLevel(directory)) { // in Inbox/Documents/Trash
            // N.B. although Documents and Trash have a nodeId, calling the listFiles1/listTrash1 API with
            // parentNode may fail!
            if(containerService.isInInbox(directory)) {
                return new NodeListService(new Contents() {
                    @Override
                    public NodeContent getNodes(final int offset) throws ApiException {
                        return new BoxRestControllerApi(session.getClient()).listQueue(deepBoxNodeId,
                                boxNodeId,
                                null,
                                offset, chunksize, "displayName asc");
                    }
                }).list(directory, listener);
            }
            if(containerService.isInDocuments(directory)) {
                return new NodeListService(new Contents() {
                    @Override
                    public NodeContent getNodes(final int offset) throws ApiException {
                        return new BoxRestControllerApi(session.getClient()).listFiles(
                                deepBoxNodeId,
                                boxNodeId,
                                offset, chunksize, "displayName asc");
                    }
                }).list(directory, listener);
            }
            if(containerService.isInTrash(directory)) {
                return new NodeListService(new Contents() {
                    @Override
                    public NodeContent getNodes(final int offset) throws ApiException {
                        return new BoxRestControllerApi(session.getClient()).listTrash(
                                deepBoxNodeId,
                                boxNodeId,
                                offset, chunksize, "displayName asc");
                    }
                }).list(directory, listener);
            }
        }
        // in subfolder of  Documents/Trash (Inbox has no subfolders)
        final String nodeId = fileid.getFileId(directory);
        if(containerService.isInTrash(directory)) {
            return new NodeListService(new Contents() {
                @Override
                public NodeContent getNodes(final int offset) throws ApiException {
                    return new BoxRestControllerApi(session.getClient()).listTrash1(
                            deepBoxNodeId,
                            boxNodeId,
                            nodeId,
                            offset, chunksize, "displayName asc");
                }
            }).list(directory, listener);
        }
        return new NodeListService(new Contents() {
            @Override
            public NodeContent getNodes(final int offset) throws ApiException {
                return new BoxRestControllerApi(session.getClient()).listFiles1(
                        deepBoxNodeId,
                        boxNodeId,
                        nodeId,
                        offset, chunksize, "displayName asc");
            }
        }).list(directory, listener);
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

    protected interface Contents {
        NodeContent getNodes(int offset) throws ApiException;
    }

    private final class NodeListService implements ListService {
        private final Contents supplier;

        public NodeListService(final Contents supplier) {
            this.supplier = supplier;
        }

        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                int offset = 0;
                int size;
                do {
                    final NodeContent files = supplier.getNodes(offset);
                    for(final Node node : files.getNodes()) {
                        list.add(new Path(directory, DeepboxPathNormalizer.name(node.getDisplayName()),
                                EnumSet.of(node.getType() == Node.TypeEnum.FILE ? Path.Type.file : Path.Type.directory)).withAttributes(attributes.toAttributes(node)));
                    }
                    size = files.getSize();
                    offset += chunksize;
                }
                while(offset < size);
                // Mark duplicates
                list.toStream().forEach(f -> f.attributes().setDuplicate(list.findAll(new SimplePathPredicate(f)).size() != 1));
                listener.chunk(directory, list);
                return list;
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
            }
        }
    }

    private final class BoxListService implements ListService {
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
                final String deepBoxNodeId = fileid.getDeepBoxNodeId(directory);
                final String boxNodeId = fileid.getBoxNodeId(directory);
                final Box box = rest.getBox(deepBoxNodeId, boxNodeId);
                if(box.getBoxPolicy().isCanListQueue()) {
                    final Path inbox = new Path(directory, containerService.getPinnedLocalization(INBOX), EnumSet.of(Path.Type.directory, Path.Type.volume));
                    list.add(inbox.withAttributes(attributes.find(inbox)));
                }
                if(box.getBoxPolicy().isCanListFilesRoot()) {
                    final Path documents = new Path(directory, containerService.getPinnedLocalization(DOCUMENTS), EnumSet.of(Path.Type.directory, Path.Type.volume));
                    list.add(documents.withAttributes(attributes.find(documents)));
                }
                if(box.getBoxPolicy().isCanAccessTrash()) {
                    final Path trash = new Path(directory, containerService.getPinnedLocalization(TRASH), EnumSet.of(Path.Type.directory, Path.Type.volume));
                    list.add(trash.withAttributes(attributes.find(trash)));
                }
                listener.chunk(directory, list);
                return list;
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
            }
        }
    }

    private final class BoxesListService implements ListService {
        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
                int offset = 0;
                int size;
                do {
                    final Boxes boxes = rest.listBoxes(fileid.getFileId(directory), offset, chunksize, "name asc", null);
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
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
            }
        }
    }

    private final class DeepBoxesListService implements ListService {
        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final BoxRestControllerApi rest = new BoxRestControllerApi(session.getClient());
                final String companyId = fileid.getFileId(directory);
                int offset = 0;
                int size;
                do {
                    final DeepBoxes deepBoxes = rest.listDeepBoxes(offset, chunksize, "name asc", null);
                    for(final DeepBox deepBox : deepBoxes.getDeepBoxes()) {
                        if(StringUtils.equals(companyId, deepBox.getCompanyId())) {
                            list.add(new Path(directory, DeepboxPathNormalizer.name(deepBox.getName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                                    attributes.toAttributes(deepBox))
                            );
                        }
                    }
                    listener.chunk(directory, list);
                    size = deepBoxes.getSize();
                    offset += chunksize;
                }
                while(offset < size);
                final Path shared = new Path(directory, containerService.getPinnedLocalization(SHARED), EnumSet.of(Path.Type.directory, Path.Type.volume));
                if(!new SharedWithMeListService(companyId).list(shared, listener).isEmpty()) {
                    list.add(shared);
                }
                return list;
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
            }
        }
    }

    private final class SharedWithMeListService implements ListService {

        private final String companyId;

        public SharedWithMeListService(final String companyId) {
            this.companyId = companyId;
        }

        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final OverviewRestControllerApi rest = new OverviewRestControllerApi(session.getClient());
                final Overview overview = rest.getOverview(companyId, chunksize, null);
                for(final BoxEntry box : overview.getSharedWithMe().getBoxes()) {
                    list.add(new Path(directory,
                            String.format("%s (%s)", DeepboxPathNormalizer.name(box.getDeepBoxName()), DeepboxPathNormalizer.name(box.getBoxName())),
                            EnumSet.of(Path.Type.directory, Path.Type.volume),
                            new PathAttributes().withFileId(box.getBoxNodeId()))
                    );
                }
                listener.chunk(directory, list);
                return list;
            }
            catch(ApiException e) {
                throw new DeepboxExceptionMappingService(fileid).map("Listing directory failed", e, directory);
            }
        }
    }

    private final class CompanyListService implements ListService {
        @Override
        public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
            try {
                final AttributedList<Path> list = new AttributedList<>();
                final UsersApi rest = new UsersApi(session.getDeepcloudClient());
                final UserFull user = rest.usersMeList();
                for(CompanyRoles company : user.getCompanies()) {
                    if(company.getStructure() == StructureEnum.PERSONAL) {
                        continue;
                    }
                    list.add(new Path(directory, DeepboxPathNormalizer.name(company.getDisplayName()), EnumSet.of(Path.Type.directory, Path.Type.volume),
                            attributes.toAttributes(company))
                    );
                }
                listener.chunk(directory, list);
                return list;
            }
            catch(ch.cyberduck.core.deepcloud.io.swagger.client.ApiException e) {
                throw new DeepcloudExceptionMappingService(fileid).map("Listing directory failed", e, directory);
            }
        }
    }
}
