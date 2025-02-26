package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2020 iterate GmbH. All rights reserved.
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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.UnsupportedException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNode;
import ch.cyberduck.core.sds.io.swagger.client.model.DeletedNodeVersionsList;
import ch.cyberduck.core.sds.io.swagger.client.model.RestoreDeletedNodesRequest;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class SDSVersioningFeature implements Versioning {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSVersioningFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) {
        return new VersioningConfiguration(true);
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new UnsupportedException();
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        try {
            new NodesApi(session.getClient()).restoreNodes(
                    new RestoreDeletedNodesRequest()
                            .resolutionStrategy(RestoreDeletedNodesRequest.ResolutionStrategyEnum.OVERWRITE)
                            .keepShareLinks(HostPreferencesFactory.get(session.getHost()).getBoolean("sds.upload.sharelinks.keep"))
                            .addDeletedNodeIdsItem(Long.parseLong(nodeid.getVersionId(file)))
                            .parentId(Long.parseLong(nodeid.getVersionId(file.getParent()))), StringUtils.EMPTY);//todo
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to write attributes of {0}", e, file);
        }

    }

    @Override
    public AttributedList<Path> list(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isDirectory()) {
            return AttributedList.emptyList();
        }
        final int chunksize = HostPreferencesFactory.get(session.getHost()).getInteger("sds.listing.chunksize");
        try {
            int offset = 0;
            DeletedNodeVersionsList nodes;
            final AttributedList<Path> versions = new AttributedList<>();
            do {
                final int range = offset;
                nodes = nodeid.retry(file.getParent(), () -> new NodesApi(session.getClient()).requestDeletedNodeVersions(
                        Long.parseLong(nodeid.getVersionId(file.getParent())),
                        file.isFile() ? "file" : "folder", file.getName(), StringUtils.EMPTY, "updatedAt:desc",
                        range, chunksize, null));
                for(DeletedNode item : nodes.getItems()) {
                    versions.add(new Path(file.getParent(), file.getName(), file.getType(),
                            new SDSAttributesAdapter(session).toAttributes(item)));
                }
                offset += chunksize;
                listener.chunk(file.getParent(), versions);
            }
            while(nodes.getItems().size() == chunksize);
            return versions;
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.revert, Flags.list);
    }
}
