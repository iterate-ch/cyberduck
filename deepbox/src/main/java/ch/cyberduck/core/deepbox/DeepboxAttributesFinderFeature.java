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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeInfo;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;

import java.util.UUID;

public class DeepboxAttributesFinderFeature implements AttributesFinder, AttributesAdapter<Node> {
    public static final String INBOX = "Inbox";
    public static final String DOCUMENTS = "Documents";
    public static final String TRASH = "Trash";

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    public DeepboxAttributesFinderFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        // TODO add test
        try {
            if(file.isRoot()) {
                return new PathAttributes();
            }
            else if(new DeepboxPathContainerService().isDeepbox(file)) {
                final BoxRestControllerApi boxApi = new BoxRestControllerApi(session.getClient());
                final String deepBoxNodeId = fileid.getDeepBoxNodeId(file);
                if(deepBoxNodeId == null) {
                    throw new NotfoundException(file.getAbsolute());
                }
                final DeepBox deepBox = boxApi.getDeepBox(UUID.fromString(deepBoxNodeId));
                return this.toAttributes(deepBox);
            }
            else if(new DeepboxPathContainerService().isBox(file)) {
                final BoxRestControllerApi boxApi = new BoxRestControllerApi(session.getClient());
                final String deepBoxNodeId = fileid.getDeepBoxNodeId(file);
                if(deepBoxNodeId == null) {
                    throw new NotfoundException(file.getAbsolute());
                }
                final String boxNodeId = fileid.getBoxNodeId(file);
                if(boxNodeId == null) {
                    throw new NotfoundException(file.getAbsolute());
                }
                final Box box = boxApi.getBox(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId));
                return this.toAttributes(box);
            }
            else if(new DeepboxPathContainerService().isThirdLevel(file)) {
                final String fileId = fileid.getFileId(file);
                if(fileId == null) {
                    throw new NotfoundException(file.getAbsolute());
                }
                return new PathAttributes().withFileId(fileId);
            }
            else {
                final String fileId = fileid.getFileId(file);
                if(fileId == null) {
                    throw new NotfoundException(file.getAbsolute());
                }
                final UUID nodeId = UUID.fromString(fileId);
                final CoreRestControllerApi core = new CoreRestControllerApi(session.getClient());
                final NodeInfo nodeInfo = core.getNodeInfo(nodeId, null, null, null);
                return this.toAttributes(nodeInfo.getNode());
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }

//        try {
//            final PathContainerService service = new DefaultPathContainerService();
//            if(service.isContainer(file)) {
////                for(RootFolder r : session.roots()) {
////                    if(StringUtils.equalsIgnoreCase(file.getName(), PathNormalizer.name(r.getPath()))
////                            || StringUtils.equalsIgnoreCase(file.getName(), PathNormalizer.name(r.getName()))) {
////                        return this.toAttributes(r);
////                    }
////                }
////                throw new NotfoundException(file.getAbsolute());
////            }
////            final FilesApi files = new FilesApi(session.getClient());
//                //final AttributesRestControllerApi api = new AttributesRestControllerApi(this.session.getClient());
//                return this.toAttributes(null);
//            }
//        }
//        catch(ApiException e) {
//            throw new DeepboxExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
//        }
    }

    public PathAttributes toAttributes(final Box box) {
        final PathAttributes attrs = new PathAttributes();
        attrs.setFileId(box.getBoxNodeId().toString());
        return attrs;
    }

    public PathAttributes toAttributes(final DeepBox deepBox) {
        final PathAttributes attrs = new PathAttributes();
        attrs.setFileId(deepBox.getDeepBoxNodeId().toString());
        return attrs;
    }

    public PathAttributes toAttributes(final Node node) {
        final PathAttributes attrs = new PathAttributes();
        attrs.setFileId(node.getNodeId().toString());
        attrs.setCreationDate(node.getCreated().getTime().getMillis());
        attrs.setModificationDate(node.getModified().getTime().getMillis());
        return attrs;
    }


//    @Override
//    public PathAttributes toAttributes(final File f) {
//        final PathAttributes attrs = new PathAttributes();
//        if(0 != f.getModified().getMillis()) {
//            attrs.setModificationDate(f.getModified().getMillis());
//        }
//        else {
//            attrs.setModificationDate(f.getUploaded().getMillis());
//        }
//        if(0 != f.getCreated().getMillis()) {
//            attrs.setCreationDate(f.getCreated().getMillis());
//        }
//        else {
//            attrs.setCreationDate(f.getUploaded().getMillis());
//        }
//        if(f.getSize() != null) {
//            attrs.setSize(f.getSize());
//        }
//        if(f.getFlags() != null) {
//            if((f.getFlags() & 4) == 4) {
//                // This item is locked by some user
//                attrs.setLockId(Boolean.TRUE.toString());
//            }
//            if((f.getFlags() & 512) == 512) {
//                // This item is hidden
//                attrs.setHidden(true);
//            }
//        }
//        if(f.getPermission() != null) {
//            // NoAccess	0
//            // ReadOnly	 1
//            // ReadWrite 2
//            // Synchronize	4	Read, write access and permission to syncronize using desktop client.
//            // FullControl 99
//            final Permission permission;
//            if((f.getPermission() & 2) == 2 || (f.getPermission() & 4) == 4) {
//                permission = new Permission(Permission.Action.read_write, Permission.Action.none, Permission.Action.none);
//            }
//            else {
//                permission = new Permission(Permission.Action.read, Permission.Action.none, Permission.Action.none);
//            }
//            if((f.getFlags() & 1) == 1) {
//                // This item is a folder
//                permission.setUser(permission.getUser().or(Permission.Action.execute));
//            }
//            attrs.setPermission(permission);
//        }
//        attrs.setFileId(f.getId());
//        return attrs;
//    }
}
