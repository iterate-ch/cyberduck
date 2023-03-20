package ch.cyberduck.core.shared;

/*
 * Copyright (c) 2002-2023 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.DisabledListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.comparator.FilenameComparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultVersioningBulkFeature extends DisabledBulkFeature {
    private static final Logger log = LogManager.getLogger(DefaultVersioningBulkFeature.class);

    private final Session<?> session;
    private Delete delete;

    public DefaultVersioningBulkFeature(final Session<?> session) {
        this.session = session;
        this.delete = session.getFeature(Delete.class);
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files, final ConnectionCallback callback) throws BackgroundException {
        switch(type) {
            case upload:
                if(new HostPreferences(session.getHost()).getBoolean("queue.upload.file.versioning")) {
                    for(TransferItem item : files.keySet()) {
                        final List<Path> versions = new DefaultVersioningFeature(session).list(item.remote, new DisabledListProgressListener()).toStream()
                                .sorted(new FilenameComparator(false)).skip(new HostPreferences(session.getHost()).getInteger("queue.upload.file.versioning.limit")).collect(Collectors.toList());
                        if(log.isWarnEnabled()) {
                            log.warn(String.format("Delete %d previous versions of %s", versions.size(), item.remote));
                        }
                        delete.delete(versions, callback, new Delete.DisabledCallback());
                    }
                }
        }
    }

    @Override
    public Bulk withDelete(final Delete delete) {
        this.delete = delete;
        return this;
    }
}
