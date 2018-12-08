package ch.cyberduck.core.sds;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import java.util.HashMap;
import java.util.Map;

public class SDSEncryptionBulkFeature implements Bulk<Void> {

    private final SDSSession session;
    private final SDSNodeIdProvider nodeid;

    public SDSEncryptionBulkFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.nodeid = nodeid;
    }

    @Override
    public Void pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default: {
                final Map<Path, Boolean> rooms = this.getRoomEncryptionStatus(files);
                for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                    final Path container = new PathContainerService().getContainer(entry.getKey().remote);
                    if(rooms.get(container)) {
                        final TransferStatus status = entry.getValue();
                        nodeid.setFileKey(status);
                    }
                }
            }
        }
        return null;
    }

    private Map<Path, Boolean> getRoomEncryptionStatus(final Map<TransferItem, TransferStatus> files) throws BackgroundException {
        final Map<Path, Boolean> rooms = new HashMap<>();
        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
            // Get top level room
            final Path container = new PathContainerService().getContainer(entry.getKey().remote);
            if(rooms.containsKey(container)) {
                continue;
            }
            rooms.put(container, nodeid.isEncrypted(entry.getKey().remote));
        }
        return rooms;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default:
                if(PreferencesFactory.get().getBoolean("sds.encryption.missingkeys.upload")) {
                    if(session.userAccount().isEncryptionEnabled()) {
                        final SDSMissingFileKeysSchedulerFeature background = new SDSMissingFileKeysSchedulerFeature(session, nodeid);
                        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                            final TransferItem item = entry.getKey();
                            if(nodeid.isEncrypted(entry.getKey().remote)) {
                                background.operate(callback, item.remote);
                            }
                        }
                    }
                }
        }
    }

    @Override
    public Bulk<Void> withDelete(final Delete delete) {
        return this;
    }

    @Override
    public Bulk<Void> withCache(final Cache<Path> cache) {
        nodeid.withCache(cache);
        return this;
    }
}
