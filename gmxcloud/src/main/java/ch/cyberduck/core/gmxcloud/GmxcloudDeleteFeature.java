package ch.cyberduck.core.gmxcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.gmxcloud.io.swagger.client.ApiException;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.DeleteResourceApi;
import ch.cyberduck.core.gmxcloud.io.swagger.client.api.MoveToTrashApi;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GmxcloudDeleteFeature implements Delete {

    private final GmxcloudSession session;
    private final GmxcloudResourceIdProvider fileid;

    private final Boolean trashing;

    public GmxcloudDeleteFeature(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid) {
        this(session, fileid, new HostPreferences(session.getHost()).getBoolean("gmxcloud.delete.trash"));
    }

    public GmxcloudDeleteFeature(final GmxcloudSession session, final GmxcloudResourceIdProvider fileid, final Boolean trashing) {
        this.session = session;
        this.fileid = fileid;
        this.trashing = trashing;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        try {
            final List<String> resources = new ArrayList<>();
            for(Path f : files.keySet()) {
                final String resourceId = fileid.getFileId(f, new DisabledListProgressListener());
                resources.add(String.format("%s/resource/%s", session.getBasePath(), resourceId));
                callback.delete(f);
            }
            new MoveToTrashApi(new GmxcloudApiClient(session)).resourceAliasTRASHChildrenMovePost(resources,
                    null, null, null, "overwrite", null);
            if(!trashing) {
                for(Path f : files.keySet()) {
                    new DeleteResourceApi(new GmxcloudApiClient(session)).resourceResourceIdDelete(
                            fileid.getFileId(f, new DisabledListProgressListener()), null, null);
                }
            }
            for(Path f : files.keySet()) {
                fileid.cache(f, null);
            }
        }
        catch(ApiException e) {
            for(Path f : files.keySet()) {
                throw new GmxcloudExceptionMappingService().map("Cannot delete {0}", e, f);
            }
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }
}
