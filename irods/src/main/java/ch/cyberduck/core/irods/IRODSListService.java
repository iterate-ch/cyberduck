package ch.cyberduck.core.irods;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.irods.irods4j.common.Versioning;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.catalog.IRODSQuery.GenQuery1QueryArgs;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.CollectionEntry;
import org.irods.irods4j.high_level.vfs.IRODSCollectionIterator;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
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

import ch.cyberduck.core.AttributedList;
import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.ListService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.Checksum;

public class IRODSListService implements ListService {

    private final IRODSSession session;

    public IRODSListService(IRODSSession session) {
        this.session = session;
    }

    @Override
    public AttributedList<Path> list(final Path directory, final ListProgressListener listener) throws BackgroundException {
        try {
            final AttributedList<Path> children = new AttributedList<Path>();
            final IRODSConnection conn = session.getClient();
            String path = directory.getAbsolute();
            if (!IRODSFilesystem.exists(conn.getRcComm(), path)) {
                throw new NotfoundException(path);
            }
            final IRODSCollectionIterator iterator = new IRODSCollectionIterator(conn.getRcComm(), path);
            
            for (CollectionEntry entry : iterator) {
            	final String normalized = PathNormalizer.normalize(entry.path(), true);
            	if(StringUtils.equals(normalized, directory.getAbsolute())) {
                  continue;
            	}
            	final PathAttributes attributes = new PathAttributes();
            	String logicalPath = entry.path();
            	String parentPath = FilenameUtils.getFullPathNoEndSeparator(logicalPath);
            	String fileName = FilenameUtils.getName(logicalPath);
            	String query = "";
            	//check version
            	if(Versioning.compareVersions(conn.getRcComm().relVersion.substring(4), "4.3.4") > 0) {
            		query = String.format("select DATA_MODIFY_TIME, DATA_CREATE_TIME, DATA_SIZE, DATA_CHECKSUM, DATA_OWNER_NAME, DATA_OWNER_ZONE where COLL_NAME = '%s' and DATA_NAME = '%s'", parentPath, fileName);
            		List<List<String>> rows = IRODSQuery.executeGenQuery2(conn.getRcComm(), query);
                	List<String> row = rows.get(0);
                	attributes.setModificationDate(Long.parseLong(row.get(0)) * 1000); // seconds to ms
                	attributes.setCreationDate(Long.parseLong(row.get(1)) * 1000);
                	attributes.setSize(Long.parseLong(row.get(2)));
                	String checksum = row.get(3);
                	if (!StringUtils.isEmpty(checksum)) {
                	    attributes.setChecksum(Checksum.parse(checksum));
                	}

                	attributes.setOwner(row.get(4));
                	attributes.setGroup(row.get(5));
            	}else {
            		//if older version, use GenQuery1
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

        			IRODSQuery.executeGenQuery1(conn.getRcComm(), input, row -> {
                    	attributes.setModificationDate(Long.parseLong(row.get(0)) * 1000); // seconds to ms
                    	attributes.setCreationDate(Long.parseLong(row.get(1)) * 1000);
                    	attributes.setSize(Long.parseLong(row.get(2)));
                    	String checksum = row.get(3);
                    	if (!StringUtils.isEmpty(checksum)) {
                    	    attributes.setChecksum(Checksum.parse(checksum));
                    	}

                    	attributes.setOwner(row.get(4));
                    	attributes.setGroup(row.get(5));
                    	return false;
        			});
            	}
            	EnumSet<Path.Type> type = entry.isCollection()
            		    ? EnumSet.of(Path.Type.directory)
            		    : EnumSet.of(Path.Type.file);

            	children.add(new Path(directory, PathNormalizer.name(normalized), type, attributes));
            	listener.chunk(directory, children);
            }
            return children;
        }
        catch(IRODSException | IOException e) {
            throw new IRODSExceptionMappingService().map("Listing directory {0} failed", e, directory);
        }
    }
}
