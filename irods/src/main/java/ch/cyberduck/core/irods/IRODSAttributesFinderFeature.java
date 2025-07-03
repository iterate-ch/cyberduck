package ch.cyberduck.core.irods;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.irods.irods4j.common.Versioning;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.catalog.IRODSQuery.GenQuery1QueryArgs;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.GenQuery1Columns;
import org.irods.irods4j.low_level.api.IRODSException;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;


public class IRODSAttributesFinderFeature implements AttributesFinder, AttributesAdapter<List<String>> {

    private final IRODSSession session;

    public IRODSAttributesFinderFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
        	final PathAttributes[] attributes = new PathAttributes[1];
            final IRODSConnection conn = session.getClient();
            if(!IRODSFilesystem.exists(this.session.getClient().getRcComm(), file.getAbsolute())) {
                throw new NotfoundException(file.getAbsolute());
            }
        	String logicalPath = file.getAbsolute();
        	String parentPath = FilenameUtils.getFullPathNoEndSeparator(logicalPath);
        	String fileName = FilenameUtils.getName(logicalPath);
            if(Versioning.compareVersions(conn.getRcComm().relVersion.substring(4), "4.3.4") > 0) {
        		String query = String.format("select DATA_MODIFY_TIME, DATA_CREATE_TIME, DATA_SIZE, DATA_CHECKSUM, DATA_OWNER_NAME, DATA_OWNER_ZONE where COLL_NAME = '%s' and DATA_NAME = '%s'", parentPath, fileName);
        		List<List<String>> rows = IRODSQuery.executeGenQuery2(conn.getRcComm(), query);
            	List<String> row = rows.get(0);
            	attributes[0]=toAttributes(row);
        	}else {
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

    			StringBuilder output = new StringBuilder();

    			IRODSQuery.executeGenQuery1(conn.getRcComm(), input, row -> {
    				attributes[0]=toAttributes(row);
                	return false;
    			});
        	}
            return attributes[0];
        }
        catch(IOException | IRODSException e) {
            throw new IRODSExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final List<String> row) {
    	final IRODSConnection conn = session.getClient();
        final PathAttributes attributes = new PathAttributes();
        attributes.setModificationDate(Long.parseLong(row.get(0)) * 1000); // seconds to ms
    	attributes.setCreationDate(Long.parseLong(row.get(1)) * 1000);
    	attributes.setSize(Long.parseLong(row.get(2)));
    	String checksum = row.get(3);
    	if (!StringUtils.isEmpty(checksum)) {
    	    attributes.setChecksum(Checksum.parse(checksum));
    	}

    	attributes.setOwner(conn.getRcComm().relVersion);
    	attributes.setGroup(row.get(5));
        return attributes;
    }
}
