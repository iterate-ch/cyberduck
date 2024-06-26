package ch.cyberduck.core.deepbox.nodepolicy;

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

import ch.cyberduck.core.deepbox.AbstractDeepboxTest;
import ch.cyberduck.core.deepbox.io.swagger.client.ApiException;
import ch.cyberduck.core.deepbox.io.swagger.client.api.BoxRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.api.CoreRestControllerApi;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Box;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Boxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBox;
import ch.cyberduck.core.deepbox.io.swagger.client.model.DeepBoxes;
import ch.cyberduck.core.deepbox.io.swagger.client.model.Node;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodeContent;
import ch.cyberduck.core.deepbox.io.swagger.client.model.NodePolicy;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DeepboxNodePolicyTest extends AbstractDeepboxTest {

    @Before
    public void setup() throws Exception {
        setup("deepbox.deepboxapp3.user");
    }

    private static final Logger log = LogManager.getLogger(DeepboxNodePolicyTest.class);

    private void depthFirst(final NodeContent nodeContent, final String where, final Set<Pair<String, NodePolicy>> policies) throws ApiException {
        final CoreRestControllerApi coreApi = new CoreRestControllerApi(session.getClient());

        for(final Node node : nodeContent.getNodes()) {
            log.info(String.format("%s (%s, %s, %s)", node.getDisplayName(), node.getNodeId(), node.getPolicy(), coreApi.getNodeInfo(node.getNodeId(), null, null, null).getPath()));
            policies.add(new ImmutablePair<>(where, node.getPolicy()));


            if(node.getType().equals(Node.TypeEnum.FOLDER)) {
                depthFirst(coreApi.listNodeContent(node.getNodeId(), 0, 50, null), where, policies);
            }
        }
    }

    @Test
    @Ignore
    public void recurse() throws ApiException {
        final BoxRestControllerApi boxApi = new BoxRestControllerApi(this.session.getClient());

        final DeepBoxes deepBoxes = boxApi.listDeepBoxes(0, 50, null, null);
        final HashSet<Pair<String, NodePolicy>> policies = new HashSet<>();
        log.info("=====================================================");
        log.info(session.getHost().getCredentials().getUsername());
        for(final DeepBox deepBox : deepBoxes.getDeepBoxes()) {
            final UUID deepBoxNodeId = deepBox.getDeepBoxNodeId();

            log.info(String.format("%s (%s, %s)", deepBox.getName(), deepBoxNodeId, deepBox.getBoxType()));
            final Boxes boxes = boxApi.listBoxes(deepBoxNodeId, 0, 50, null, null);
            for(final Box box : boxes.getBoxes()) {
                final UUID boxNodeId = box.getBoxNodeId();
                final String where = box.getName();
                log.info(String.format("%s (%s, %s)", box.getName(), boxNodeId, box.getBoxPolicy()));

                depthFirst(boxApi.listFiles(deepBoxNodeId, boxNodeId, 0, 50, null), where, policies);
                try {
                    depthFirst(boxApi.listTrash(deepBoxNodeId, boxNodeId, 0, 50, null), where, policies);
                }
                catch(ApiException e) {
                    if(403 != e.getCode()) {
                        throw e;
                    }
                }
                try {
                    depthFirst(boxApi.listQueue(deepBoxNodeId, boxNodeId, null, 0, 50, null), where, policies);
                }
                catch(ApiException e) {
                    if(403 != e.getCode()) {
                        throw e;
                    }
                }
            }
        }
        log.info("=====================================================");
        log.info(policies);
        System.out.println("canListChildren;canAddChildren;canMoveWithinBox;canMoveOutOfBox;canDelete;canPurge;canRevert;canDownload;canDirectDownload;canAnalyze;canSign;canReadNodeInfo;canRename;canAdminAccess;canComment;canTag;canI18n;canRevision;canWatch;username;deepboxNodeId");
        for(Pair<String, NodePolicy> pair : policies) {
            final NodePolicy policy = pair.getRight();
            System.out.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n", policy.isCanListChildren(), policy.isCanAddChildren(), policy.isCanMoveWithinBox(), policy.isCanMoveOutOfBox(), policy.isCanDelete(), policy.isCanPurge(), policy.isCanRevert(), policy.isCanDownload(), policy.isCanDirectDownload(), policy.isCanAnalyze(), policy.isCanSign(), policy.isCanReadNodeInfo(), policy.isCanRename(), policy.isCanAdminAccess(), policy.isCanComment(), policy.isCanTag(), policy.isCanI18n(), policy.isCanRevision(), policy.isCanWatch(),
                    session.getHost().getCredentials().getUsername(), pair.getLeft()
            );
        }
        log.info("=====================================================");
    }
}
