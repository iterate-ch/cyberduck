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
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.preferences.HostPreferencesFactory;
import ch.cyberduck.core.preferences.PreferencesReader;
import ch.cyberduck.core.transfer.TransferStatus;

import org.irods.irods4j.high_level.connection.IRODSConnection;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem.RemoveOptions;
import org.irods.irods4j.high_level.vfs.ObjectStatus;
import org.irods.irods4j.high_level.vfs.ObjectStatus.ObjectType;
import org.irods.irods4j.low_level.api.IRODSException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public class IRODSDeleteFeature implements Delete {

    private final IRODSSession session;
    private final RemoveOptions removeOptions;

    public IRODSDeleteFeature(IRODSSession session) {
        this.session = session;

        PreferencesReader prefs = HostPreferencesFactory.get(session.getHost());
        if(prefs.getBoolean(IRODSProtocol.DELETE_OBJECTS_PERMANTENTLY)) {
            removeOptions = RemoveOptions.NO_TRASH;
        }
        else {
            removeOptions = RemoveOptions.NONE;
        }
    }

    @Override
    public void delete(final Map<Path, TransferStatus> files, final PasswordCallback prompt, final Callback callback) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Path file : files.keySet()) {
            boolean skip = false;
            for(Path d : deleted) {
                if(file.isChild(d)) {
                    skip = true;
                    break;
                }
            }

            if(skip) {
                continue;
            }

            deleted.add(file);
            callback.delete(file);

            try {
                final IRODSConnection conn = session.getClient();
                final String logicalPath = file.getAbsolute();
                final ObjectStatus status = IRODSFilesystem.status(conn.getRcComm(), logicalPath);

                if(!IRODSFilesystem.exists(status)) {
                    throw new NotfoundException(String.format("%s doesn't exist", logicalPath));
                }

                if(status.getType() == ObjectType.DATA_OBJECT) {
                    IRODSFilesystem.remove(conn.getRcComm(), logicalPath, removeOptions);
                }
                else if(status.getType() == ObjectType.COLLECTION) {
                    IRODSFilesystem.removeAll(conn.getRcComm(), logicalPath, removeOptions);
                }
            }
            catch(IRODSException e) {
                throw new IRODSExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.recursive);
    }
}
