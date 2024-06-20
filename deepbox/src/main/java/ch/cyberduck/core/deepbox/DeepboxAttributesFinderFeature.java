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
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.HostUrlProvider;
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

import java.net.URI;
import java.util.UUID;

public class DeepboxAttributesFinderFeature implements AttributesFinder, AttributesAdapter<Void> {
    public static final String INBOX = "Inbox";
    public static final String DOCUMENTS = "Documents";
    public static final String TRASH = "Trash";

    private final DeepboxSession session;
    private final DeepboxIdProvider fileid;

    /**
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canDelete(Boolean)
     */
    public static final Acl.Role CANDELETE = new Acl.Role("canDelete");

    // TODO check direct download api
    /**
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canAddChildren(Boolean) (Boolean) (Boolean)
     */
    public static final Acl.Role CANADDCHILDREN = new Acl.Role("canAddChildren");

    /**
     * @see ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy#canAddChildren(Boolean) (Boolean) (Boolean)
     */
    public static final Acl.Role CANLISTCHILDREN = new Acl.Role("canListChildren");

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


    public PathAttributes toAttributes(final Node node) throws ApiException {
        final PathAttributes attrs = new PathAttributes();
        attrs.setFileId(node.getNodeId().toString());
        attrs.setCreationDate(node.getCreated().getTime().getMillis());
        attrs.setModificationDate(node.getModified().getTime().getMillis());
        attrs.setSize(node.getSize());
        // For now, use pattern https://{env}.deepbox.swiss/node/{nodeId}/preview, API forthcoming
        attrs.setLink(new DescriptiveUrl(URI.create(new HostUrlProvider()
                .withPath(true).withUsername(false)
                .get(session.getHost().getProtocol().getScheme(),
                        session.getHost().getPort(),
                        null,
                        String.format("%sdeepbox.swiss", session.getStage()),
                        String.format("/node/%s/preview", node.getNodeId().toString())
                ))));

        // TODO check with DeepBox: integration test setup? How can we change can*?
        // TODO full list of cancan/preflight?
        final Acl acl = new Acl(new Acl.CanonicalUser());
        if(node.getPolicy().isCanDelete()) {
            acl.addAll(new Acl.CanonicalUser(), CANDELETE);
        }
        if(node.getPolicy().isCanAddChildren()) {
            acl.addAll(new Acl.CanonicalUser(), CANADDCHILDREN);
        }
        if(node.getPolicy().isCanListChildren()) {
            acl.addAll(new Acl.CanonicalUser(), CANLISTCHILDREN);
        }
        attrs.setAcl(acl);
        return attrs;
    }

    @Override
    public PathAttributes toAttributes(final Void model) {
        return null;
    }
}
