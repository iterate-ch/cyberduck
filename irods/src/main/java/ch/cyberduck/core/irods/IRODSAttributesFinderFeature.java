package ch.cyberduck.core.irods;

/*
 * Copyright (c) 2002-2025 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.LogicalPath;
import org.irods.irods4j.high_level.vfs.ObjectStatus;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.List;

public class IRODSAttributesFinderFeature implements AttributesFinder, AttributesAdapter<List<String>> {

    private static final Logger log = LogManager.getLogger(IRODSAttributesFinderFeature.class);

    private static final String REPLICA_STATUS_GOOD = "1";
    private static final String REPLICA_STATUS_STALE = "0";

    private final IRODSSession session;

    public IRODSAttributesFinderFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            log.debug("looking up path attributes.");

            final String logicalPath = file.getAbsolute();
            final IRODSConnection conn = session.getClient();

            ObjectStatus status = IRODSFilesystem.status(conn.getRcComm(), logicalPath);

            if(IRODSFilesystem.isDataObject(status)) {
                log.debug("data object exists in iRODS. fetching data using GenQuery2.");
                String query = String.format(
                        "select DATA_CREATE_TIME, DATA_MODIFY_TIME, DATA_SIZE, DATA_CHECKSUM, DATA_REPL_STATUS where COLL_NAME = '%s' and DATA_NAME = '%s' order by DATA_REPL_STATUS desc, DATA_MODIFY_TIME desc",
                        LogicalPath.parentPath(logicalPath),
                        LogicalPath.objectName(logicalPath));
                log.debug("query = [{}]", query);
                List<List<String>> rows = IRODSQuery.executeGenQuery2(conn.getRcComm(), query);

                PathAttributes attrs = new DefaultPathAttributes();

                if(!rows.isEmpty()) {
                    List<String> row = rows.get(0);
                    if(REPLICA_STATUS_STALE.equals(row.get(4)) || REPLICA_STATUS_GOOD.equals(row.get(4))) {
                        setAttributes(attrs, row);
                    }
                }

                return attrs;
            }

            if(IRODSFilesystem.isCollection(status)) {
                log.debug("collection exists in iRODS. fetching data using GenQuery2.");
                String query = String.format("select COLL_CREATE_TIME, COLL_MODIFY_TIME where COLL_NAME = '%s'", logicalPath);
                log.debug("query = [{}]", query);
                List<List<String>> rows = IRODSQuery.executeGenQuery2(conn.getRcComm(), query);

                PathAttributes attrs = new DefaultPathAttributes();

                if(!rows.isEmpty()) {
                    // Collections do not have the same properties as data objects
                    // so fill in the gaps to satisfy requirements of setAttributes.
                    List<String> row = rows.get(0);
                    row.add("0"); // Data size
                    row.add("");  // Checksum
                    row.add("");  // Replica status
                    setAttributes(attrs, row);
                }

                return attrs;
            }

            throw new NotfoundException(logicalPath);
        }
        catch(IRODSException e) {
            throw new IRODSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final List<String> row) {
        PathAttributes attrs = new DefaultPathAttributes();
        setAttributes(attrs, row);
        return attrs;
    }

    private static void setAttributes(final PathAttributes attrs, final List<String> row) {
        log.debug("path attribute info: created at [{}], modified at [{}], data size = [{}], checksum = [{}]",
                row.get(0), row.get(1), row.get(2), row.get(3));
        attrs.setCreationDate(Long.parseLong(row.get(0)) * 1000); // seconds to ms
        attrs.setModificationDate(Long.parseLong(row.get(1)) * 1000);
        attrs.setSize(Long.parseLong(row.get(2)));
        attrs.setChecksum(IRODSChecksumUtils.toChecksum(row.get(3)));
    }

}
