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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathContainerService;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.features.Scheduler;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SDSEncryptionBulkFeature implements Bulk<Void> {
    private static final Logger log = LogManager.getLogger(SDSEncryptionBulkFeature.class);

    private final SDSSession session;
    private final SDSTripleCryptEncryptorFeature triplecrypt;

    private final PathContainerService containerService
            = new SDSPathContainerService();

    public SDSEncryptionBulkFeature(final SDSSession session, final SDSNodeIdProvider nodeid) {
        this.session = session;
        this.triplecrypt = new SDSTripleCryptEncryptorFeature(session, nodeid);
    }

    @Override
    public Void pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default: {
                final Map<Path, Boolean> rooms = this.getRoomEncryptionStatus(files);
                for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                    final Path container = containerService.getContainer(entry.getKey().remote);
                    if(rooms.get(container)) {
                        final TransferStatus status = entry.getValue();
                        log.debug("Set file key for {}", entry.getKey());
                        status.setFilekey(SDSTripleCryptEncryptorFeature.generateFileKey());
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param files Files to upload
     * @return Map of rooms with Triple Crypt enabled
     */
    private Map<Path, Boolean> getRoomEncryptionStatus(final Map<TransferItem, TransferStatus> files) throws BackgroundException {
        final Map<Path, Boolean> rooms = new HashMap<>();
        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
            final Path container = containerService.getContainer(entry.getKey().remote);
            if(rooms.containsKey(container)) {
                continue;
            }
            log.debug("Determine encryption status for {}", container);
            rooms.put(container, triplecrypt.isEncrypted(container));
        }
        return rooms;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case download:
                break;
            default:
                if(new HostPreferences(session.getHost()).getBoolean("sds.encryption.missingkeys.upload")) {
                    if(session.userAccount().isEncryptionEnabled()) {
                        final Scheduler scheduler = session.getFeature(Scheduler.class);
                        final Map<Path, Boolean> rooms = this.getRoomEncryptionStatus(files);
                        for(Map.Entry<TransferItem, TransferStatus> entry : files.entrySet()) {
                            final Path file = entry.getKey().remote;
                            if(file.isFile()) {
                                final Path container = containerService.getContainer(file);
                                if(rooms.get(container)) {
                                    log.debug("Run missing file keys for {}", file);
                                    scheduler.execute(callback);
                                }
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
}
