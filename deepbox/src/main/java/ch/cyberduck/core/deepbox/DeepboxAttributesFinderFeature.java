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
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.BoxAccessPolicy;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeInfo;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;

import java.util.UUID;

/**
 * See {@link ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy} for full of attributes. list
 * <p>
 * The following attributes are currently unused:
 * <ul>
 *     <li>{@link ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canDirectDownload(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canAnalyze(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canSign(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canReadNodeInfo(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canAdminAccess(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canComment(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canTag(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canI18n(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canRevision(Boolean)}</li>
 *     <li>{@link  ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canWatch(Boolean)}</li>
 *  </ul>
 */
public class DeepboxAttributesFinderFeature implements AttributesFinder, AttributesAdapter<Void> {

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;
    private final DeepboxPathContainerService containerService = new DeepboxPathContainerService();

    /**
     * Used for preflight checks in {@link DeepboxListService}.
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canListChildren(Boolean) (Boolean)
     */
    public static final Acl.Role CANLISTCHILDREN = new Acl.Role("canListChildren");

    /**
     * Used for preflight checks in {@link DeepboxTouchFeature} and {@link DeepboxDirectoryFeature}.
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canAddChildren(Boolean)
     */
    public static final Acl.Role CANADDCHILDREN = new Acl.Role("canAddChildren");

    /**
     * Used for preflight checks in {@link DeepboxMoveFeature}.
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canMoveWithinBox(Boolean)
     */
    public static final Acl.Role CANMOVEWITHINBOX = new Acl.Role("canMoveWithinBox");

    /**
     * Used for preflight checks in {@link DeepboxMoveFeature}.
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canMoveOutOfBox(Boolean)
     */
    public static final Acl.Role CANMOVEOUTOFBOX = new Acl.Role("canMoveOutOfBox");

    /**
     * Used for preflight checks in {@link DeepboxTrashFeature} (non-trash).
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canDelete(Boolean)
     */
    public static final Acl.Role CANDELETE = new Acl.Role("canDelete");

    /**
     * Used for preflight checks in {@link DeepboxDeleteFeature} (trash).
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canPurge(Boolean)
     */
    public static final Acl.Role CANPURGE = new Acl.Role("canPurge");

    /**
     * Used for preflight checks in {@link DeepboxRestoreFeature} (restore from trash).
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canRevert(Boolean)
     */
    public static final Acl.Role CANREVERT = new Acl.Role("canRevert");

    /**
     * Used for preflight checks in {@link DeepboxReadFeature}.
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canDownload(Boolean)
     */
    public static final Acl.Role CANDOWNLOAD = new Acl.Role("canDownload");

    /**
     * Used for preflight checks in {@link DeepboxMoveFeature}.
     *
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canRename(Boolean)
     */
    public static final Acl.Role CANRENAME = new Acl.Role("canRename");

    public DeepboxAttributesFinderFeature(final DeepboxSession session, final DeepboxIdProvider fileid) {
        this.session = session;
        this.fileid = fileid;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            if(file.isRoot()) {
                return new PathAttributes();
            }
            else if(containerService.isDeepbox(file)) {
                final String deepBoxNodeId = fileid.getDeepBoxNodeId(file);
                final DeepBox deepBox = new BoxRestControllerApi(session.getClient()).getDeepBox(UUID.fromString(deepBoxNodeId));
                return this.toAttributes(deepBox);
            }
            else if(containerService.isBox(file)) {
                final String deepBoxNodeId = fileid.getDeepBoxNodeId(file);
                final String boxNodeId = fileid.getBoxNodeId(file);
                final Box box = new BoxRestControllerApi(session.getClient()).getBox(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId));
                return this.toAttributes(box);
            }
            else if(containerService.isThirdLevel(file)) {
                return this.toAttributesThirdLevel(file);
            }
            else {
                final String fileId = fileid.getFileId(file);
                final UUID nodeId = UUID.fromString(fileId);
                final NodeInfo nodeInfo = new CoreRestControllerApi(session.getClient()).getNodeInfo(nodeId, null, null, null);
                return this.toAttributes(nodeInfo.getNode());
            }
        }
        catch(ApiException e) {
            throw new DeepboxExceptionMappingService(fileid).map("Failure to read attributes of {0}", e, file);
        }
    }

    public PathAttributes toAttributesThirdLevel(final Path file) throws BackgroundException, ApiException {
        final String fileId = fileid.getFileId(file);
        final String deepBoxNodeId = fileid.getDeepBoxNodeId(file);
        final String boxNodeId = fileid.getBoxNodeId(file);
        // map BoxAccessPolicy to CANLISTCHILDREN and CANADDCHILDREN for third level
        final Box box = new BoxRestControllerApi(session.getClient()).getBox(UUID.fromString(deepBoxNodeId), UUID.fromString(boxNodeId));
        final Acl acl = new Acl(new Acl.CanonicalUser());
        final BoxAccessPolicy boxPolicy = box.getBoxPolicy();
        if(containerService.isInbox(file)) {
            if(boxPolicy.isCanListQueue()) {
                acl.addAll(new Acl.CanonicalUser(), CANLISTCHILDREN);
            }
            if(boxPolicy.isCanAddQueue()) {
                acl.addAll(new Acl.CanonicalUser(), CANADDCHILDREN);
            }
        }
        else if(containerService.isDocuments(file)) {
            if(boxPolicy.isCanListFilesRoot()) {
                acl.addAll(new Acl.CanonicalUser(), CANLISTCHILDREN);
            }
            if(boxPolicy.isCanAddFilesRoot()) {
                acl.addAll(new Acl.CanonicalUser(), CANADDCHILDREN);
            }
        }
        else {
            if(boxPolicy.isCanAccessTrash()) {
                acl.addAll(new Acl.CanonicalUser(), CANLISTCHILDREN);
            }
        }
        return new PathAttributes().withFileId(fileId).withAcl(acl);
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
        attrs.setSize(node.getSize());
        final Acl acl = new Acl(new Acl.CanonicalUser());
        if(node.getPolicy().isCanListChildren()) {
            acl.addAll(new Acl.CanonicalUser(), CANLISTCHILDREN);
        }
        if(node.getPolicy().isCanAddChildren()) {
            acl.addAll(new Acl.CanonicalUser(), CANADDCHILDREN);
        }
        if(node.getPolicy().isCanMoveWithinBox()) {
            acl.addAll(new Acl.CanonicalUser(), CANMOVEWITHINBOX);
        }
        if(node.getPolicy().isCanMoveOutOfBox()) {
            acl.addAll(new Acl.CanonicalUser(), CANMOVEOUTOFBOX);
        }
        if(node.getPolicy().isCanDelete()) {
            acl.addAll(new Acl.CanonicalUser(), CANDELETE);
        }
        if(node.getPolicy().isCanPurge()) {
            acl.addAll(new Acl.CanonicalUser(), CANPURGE);
        }
        if(node.getPolicy().isCanDownload()) {
            acl.addAll(new Acl.CanonicalUser(), CANDOWNLOAD);
        }
        if(node.getPolicy().isCanRename()) {
            acl.addAll(new Acl.CanonicalUser(), CANRENAME);
        }
        if(node.getPolicy().isCanRevert()) {
            acl.addAll(new Acl.CanonicalUser(), CANREVERT);
        }
        attrs.setAcl(acl);
        return attrs;
    }

    @Override
    public PathAttributes toAttributes(final Void model) {
        return null;
    }
}