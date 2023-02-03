package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.Map;

public class SDSThresholdDeleteFeature implements Delete {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;
    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSThresholdDeleteFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        if(files.size() == 1) {
            new SDSDeleteFeature(session, nodeid).delete(files, prompt, callback);
        }
        else {
            new SDSBatchDeleteFeature(session, nodeid).delete(files, prompt, callback);
        }
    }

    @Override
    public boolean isRecursive() {
        return true;
    }

    @Override
    public boolean isSupported(final Path file) {
        if(containerService.isContainer(file)) {
            if(new HostPreferences(session.getHost()).getBoolean("sds.delete.dataroom.enable")) {
                // Need the query permission on the parent data room if file itself is subroom
                return new SDSPermissionsFeature(session, nodeid).containsRole(containerService.getContainer(file.getParent()),
                        SDSPermissionsFeature.MANAGE_ROLE);
            }
            return false;
        }
        return new SDSPermissionsFeature(session, nodeid).containsRole(file, SDSPermissionsFeature.DELETE_ROLE);
    }

}
