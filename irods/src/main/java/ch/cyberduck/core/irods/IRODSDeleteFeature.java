package ch.cyberduck.core.irods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.irods.irods4j.high_level.vfs.IRODSFilesystem;
import org.irods.irods4j.high_level.vfs.IRODSFilesystem.RemoveOptions;
import org.irods.irods4j.high_level.vfs.ObjectStatus;
import org.irods.irods4j.high_level.vfs.ObjectStatus.ObjectType;
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

import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.transfer.TransferStatus;

public class IRODSDeleteFeature implements Delete {

    private final IRODSSession session;

    public IRODSDeleteFeature(IRODSSession session) {
        this.session = session;
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
            final String absolute = file.getAbsolute();
            try {
            	if (!IRODSFilesystem.exists(this.session.getClient().getRcComm(), absolute)) {
                    throw new NotfoundException(String.format("%s doesn't exist", absolute));
                }
            	ObjectStatus status = IRODSFilesystem.status(this.session.getClient().getRcComm(), absolute);
                 if (status.equals(ObjectType.DATA_OBJECT)) {
                     IRODSFilesystem.remove(this.session.getClient().getRcComm(), absolute, RemoveOptions.NO_TRASH);
                 } else if (status.equals(ObjectType.COLLECTION)) {
                     IRODSFilesystem.removeAll(this.session.getClient().getRcComm(), absolute, RemoveOptions.NO_TRASH);
                 }
            }
            catch(IOException | IRODSException e) {
                throw new IRODSExceptionMappingService().map("Cannot delete {0}", e, file);
            }
        }
    }

    @Override
    public EnumSet<Flags> features(final Path file) {
        return EnumSet.of(Flags.recursive);
    }
}
