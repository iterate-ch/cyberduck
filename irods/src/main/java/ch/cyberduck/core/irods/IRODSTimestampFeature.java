package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Timestamp;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.IRODSReplicas;
import org.irods.irods4j.high_level.vfs.LogicalPath;
import org.irods.irods4j.high_level.vfs.ObjectStatus;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class IRODSTimestampFeature implements Timestamp {

    private static final Logger log = LogManager.getLogger(IRODSTimestampFeature.class);

    private IRODSSession session;

    public IRODSTimestampFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public void setTimestamp(final Path file, final TransferStatus status) throws BackgroundException {
        if(status.getModified() == null) {
            return;
        }

        final String logicalPath = file.getAbsolute();
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(status.getModified());
        log.debug("setting timestamp for [{}] to [{}] seconds (since epoch).", logicalPath, seconds);

        try {
            ObjectStatus objectStatus = IRODSFilesystem.status(session.getClient().getRcComm(), logicalPath);
            boolean updated = true;

            if(IRODSFilesystem.isDataObject(objectStatus)) {
                long replicaNumber = getReplicaNumberOfLatestGoodReplica(logicalPath);
                IRODSReplicas.lastWriteTime(session.getClient().getRcComm(), logicalPath, replicaNumber, seconds);
            }
            else if(IRODSFilesystem.isCollection(objectStatus)) {
                IRODSFilesystem.lastWriteTime(session.getClient().getRcComm(), logicalPath, seconds);
            }
            else {
                updated = false;
                log.debug("path does not point to a data object or collection. cannot update timestamp.");
            }

            if(updated) {
                log.debug("timestamp set to [{}] seconds (since epoch) on [{}] successfully.", seconds, logicalPath);
            }
        }
        catch(IRODSException e) {
            throw new IRODSExceptionMappingService().map(e);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e);
        }
    }

    private long getReplicaNumberOfLatestGoodReplica(String logicalPath) throws IRODSException, IOException {
        final IRODSConnection conn = session.getClient();

        log.debug("getting replica number of latest (good) replica.");
        String query = String.format(
                "select DATA_REPL_NUM, DATA_REPL_STATUS, DATA_MODIFY_TIME where COLL_NAME = '%s' and DATA_NAME = '%s' order by DATA_REPL_STATUS desc, DATA_MODIFY_TIME desc",
                LogicalPath.parentPath(logicalPath),
                LogicalPath.objectName(logicalPath));
        log.debug("query = [{}]", query);
        List<List<String>> rows = IRODSQuery.executeGenQuery2(conn.getRcComm(), query);

        return Long.parseLong(rows.get(0).get(0));
    }

}
