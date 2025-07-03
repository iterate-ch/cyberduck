package ch.cyberduck.core.irods;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.irods.irods4j.common.Versioning;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.catalog.IRODSQuery.GenQuery1QueryArgs;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.io.IRODSDataObjectOutputStream;
import org.irods.irods4j.low_level.api.GenQuery1Columns;
import org.irods.irods4j.low_level.api.IRODSException;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Write;
import ch.cyberduck.core.io.StatusOutputStream;
import ch.cyberduck.core.transfer.TransferStatus;

public class IRODSWriteFeature implements Write<List<String>> {

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
                    	String logicalPath = file.getAbsolute();
                    	String parentPath = FilenameUtils.getFullPathNoEndSeparator(logicalPath);
                    	String fileName = FilenameUtils.getName(logicalPath);
                    	final List<String> status = new ArrayList<>();;
                    	 // Step 4: Check iRODS version to decide which query mechanism to use
                        if(Versioning.compareVersions(conn.getRcComm().relVersion.substring(4), "4.3.4") > 0) {
                    		String query = String.format("select DATA_MODIFY_TIME, DATA_CREATE_TIME, DATA_SIZE, DATA_CHECKSUM, DATA_OWNER_NAME, DATA_OWNER_ZONE where COLL_NAME = '%s' and DATA_NAME = '%s'", parentPath, fileName);
                    		List<List<String>> rows;
							try {
								 // Step 5: Execute the query and add the first result row to the status list
								rows = IRODSQuery.executeGenQuery2(conn.getRcComm(), query);
								status.addAll(rows.get(0));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IRODSException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}else {
                    		//if older version, use Query1
                    		GenQuery1QueryArgs input = new GenQuery1QueryArgs();

                			// select COLL_NAME, DATA_NAME, DATA_ACCESS_TIME
                			input.addColumnToSelectClause(GenQuery1Columns.COL_D_MODIFY_TIME);
                			input.addColumnToSelectClause(GenQuery1Columns.COL_D_CREATE_TIME);
                			input.addColumnToSelectClause(GenQuery1Columns.COL_DATA_SIZE);
                			input.addColumnToSelectClause(GenQuery1Columns.COL_D_DATA_CHECKSUM);
                			input.addColumnToSelectClause(GenQuery1Columns.COL_D_OWNER_NAME);
                			input.addColumnToSelectClause(GenQuery1Columns.COL_D_OWNER_ZONE);
                			

                			// where COLL_NAME like '/tempZone/home/rods and DATA_NAME = 'atime.txt'
                			String collNameCondStr = String.format("= '%s'", parentPath);
                			String dataNameCondStr = String.format("= '%s'", fileName);
                			input.addConditionToWhereClause(GenQuery1Columns.COL_COLL_NAME, collNameCondStr);
                			input.addConditionToWhereClause(GenQuery1Columns.COL_DATA_NAME, dataNameCondStr);

                			try {
								IRODSQuery.executeGenQuery1(conn.getRcComm(), input, row -> {
									status.addAll(row);
									return false;
								});
							} catch (IOException | IRODSException e) {
								e.printStackTrace();
							}
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
