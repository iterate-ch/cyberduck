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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.catalog.IRODSQuery.GenQuery1QueryArgs;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.low_level.api.GenQuery1Columns;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class IRODSWriteFeature implements Write<List<String>> {

    private static final Logger log = LogManager.getLogger(IRODSWriteFeature.class);

    private final IRODSSession session;

    public IRODSWriteFeature(IRODSSession session) {
        this.session = session;
    }

    @Override
    public StatusOutputStream<List<String>> write(final Path file, final TransferStatus status, final ConnectionCallback callback) throws BackgroundException {
        try {
            // Step 1: Get the active iRODS client connection and parameters
            final IRODSConnection conn = session.getClient();
            boolean append = status.isAppend();
            boolean truncate = !append;
            final OutputStream out = new IRODSDataObjectOutputStream(conn.getRcComm(), file.getAbsolute(), truncate, append);

            // Step 2: Return a wrapped StatusOutputStream that provides file metadata on completion
            return new StatusOutputStream<List<String>>(out) {
                @Override
                public List<String> getStatus() throws BackgroundException {
                    // Step 3: Extract parent directory and filename from the logical path
                    final List<String> status = new ArrayList<>();
                    GenQuery1QueryArgs input = new GenQuery1QueryArgs();

                    // select DATA_MODIFY_TIME, DATA_CREATE_TIME, DATA_SIZE, DATA_CHECKSUM ...
                    input.addColumnToSelectClause(GenQuery1Columns.COL_D_MODIFY_TIME);
                    input.addColumnToSelectClause(GenQuery1Columns.COL_D_CREATE_TIME);
                    input.addColumnToSelectClause(GenQuery1Columns.COL_DATA_SIZE);
                    input.addColumnToSelectClause(GenQuery1Columns.COL_D_DATA_CHECKSUM);

                    // where COLL_NAME = '<parent_path>' and DATA_NAME = '<filename>'
                    String logicalPath = file.getAbsolute();
                    String collNameCondStr = String.format("= '%s'", FilenameUtils.getFullPathNoEndSeparator(logicalPath));
                    String dataNameCondStr = String.format("= '%s'", FilenameUtils.getName(logicalPath));
                    input.addConditionToWhereClause(GenQuery1Columns.COL_COLL_NAME, collNameCondStr);
                    input.addConditionToWhereClause(GenQuery1Columns.COL_DATA_NAME, dataNameCondStr);

                    try {
                        IRODSQuery.executeGenQuery1(conn.getRcComm(), input, row -> {
                            status.addAll(row);
                            return false;
                        });
                    }
                    catch(IOException | IRODSException e) {
                        log.error("Could not retrieve status info using GenQuery1 for [{}]; {}",
                                file.getAbsolute(), e.getMessage());
                    }

                    return status;
                }
            };
        }
        catch(IRODSException | IOException e) {
            throw new IRODSExceptionMappingService().map("Uploading {0} failed", e, file);
        }
    }
}
