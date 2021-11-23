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

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.VersioningConfiguration;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.features.Versioning;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.sds.io.swagger.client.ApiException;
import ch.cyberduck.core.sds.io.swagger.client.api.NodesApi;
import ch.cyberduck.core.sds.io.swagger.client.model.RestoreDeletedNodesRequest;

import org.apache.commons.lang3.StringUtils;

public class SDSVersioningFeature implements Versioning {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSVersioningFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public VersioningConfiguration getConfiguration(final Path container) {
        return VersioningConfiguration.empty();
    }

    @Override
    public void setConfiguration(final Path container, final PasswordCallback prompt, final VersioningConfiguration configuration) throws BackgroundException {
        throw new InteroperabilityException();
    }

    @Override
    public void revert(final Path file) throws BackgroundException {
        try {
            new NodesApi(session.getClient()).restoreNodes(
                new RestoreDeletedNodesRequest()
                    .resolutionStrategy(RestoreDeletedNodesRequest.ResolutionStrategyEnum.OVERWRITE)
                    .keepShareLinks(new HostPreferences(session.getHost()).getBoolean("sds.upload.sharelinks.keep"))
                    .addDeletedNodeIdsItem(Long.parseLong(nodeid.getVersionId(file, new DisabledListProgressListener())))
                    .parentId(Long.parseLong(nodeid.getVersionId(file.getParent(), new DisabledListProgressListener()))), StringUtils.EMPTY);
        }
        catch(ApiException e) {
            throw new SDSExceptionMappingService(nodeid).map("Failure to write attributes of {0}", e, file);
        }

    }

    @Override
    public boolean isRevertable(final Path file) {
        return true;
    }

    @Override
    public Credentials getToken(final String mfaSerial, final PasswordCallback callback) throws ConnectionCanceledException {
        return null;
    }

}
