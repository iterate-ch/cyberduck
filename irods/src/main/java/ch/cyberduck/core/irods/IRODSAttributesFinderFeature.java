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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.io.Checksum;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.irods.irods4j.high_level.catalog.IRODSQuery;
import org.irods.irods4j.high_level.catalog.IRODSQuery.GenQuery1QueryArgs;
import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.low_level.api.GenQuery1Columns;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.List;

public class IRODSAttributesFinderFeature implements AttributesFinder, AttributesAdapter<List<String>> {

    private final IRODSSession session;

    public IRODSAttributesFinderFeature(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        try {
            final IRODSConnection conn = session.getClient();
            final String logicalPath = file.getAbsolute();
            if(!IRODSFilesystem.exists(session.getClient().getRcComm(), logicalPath)) {
                throw new NotfoundException(file.getAbsolute());
            }

            GenQuery1QueryArgs input = new GenQuery1QueryArgs();

            // select DATA_MODIFY_TIME, DATA_CREATE_TIME, DATA_SIZE, DATA_CHECKSUM ...
            input.addColumnToSelectClause(GenQuery1Columns.COL_D_MODIFY_TIME);
            input.addColumnToSelectClause(GenQuery1Columns.COL_D_CREATE_TIME);
            input.addColumnToSelectClause(GenQuery1Columns.COL_DATA_SIZE);
            input.addColumnToSelectClause(GenQuery1Columns.COL_D_DATA_CHECKSUM);

            // where COLL_NAME = '<parent_path>' and DATA_NAME = '<filename>'
            String collNameCondStr = String.format("= '%s'", FilenameUtils.getFullPathNoEndSeparator(logicalPath));
            String dataNameCondStr = String.format("= '%s'", FilenameUtils.getName(logicalPath));
            input.addConditionToWhereClause(GenQuery1Columns.COL_COLL_NAME, collNameCondStr);
            input.addConditionToWhereClause(GenQuery1Columns.COL_DATA_NAME, dataNameCondStr);

            final PathAttributes attrs = new PathAttributes();

            IRODSQuery.executeGenQuery1(conn.getRcComm(), input, row -> {
                attrs.setModificationDate(Long.parseLong(row.get(0)) * 1000); // seconds to ms
                attrs.setCreationDate(Long.parseLong(row.get(1)) * 1000);
                attrs.setSize(Long.parseLong(row.get(2)));

                String checksum = row.get(3);
                if(!StringUtils.isEmpty(checksum)) {
                    attrs.setChecksum(Checksum.parse(checksum));
                }

                return false;
            });

            return attrs;
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
        if(!StringUtils.isEmpty(checksum)) {
            attributes.setChecksum(Checksum.parse(checksum));
        }

        return attributes;
    }
}
