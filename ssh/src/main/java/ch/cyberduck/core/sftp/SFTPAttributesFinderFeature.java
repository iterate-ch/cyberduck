package ch.cyberduck.core.sftp;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.ListProgressListener;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.AttributesAdapter;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

import net.schmizz.sshj.sftp.FileAttributes;

public class SFTPAttributesFinderFeature implements AttributesFinder, AttributesAdapter<FileAttributes> {

    private final SFTPSession session;
    private final boolean permissionsAvailable;

    public SFTPAttributesFinderFeature(final SFTPSession session) {
        this.session = session;
        permissionsAvailable = !isServerBlacklisted();
    }

    @Override
    public PathAttributes find(final Path file, final ListProgressListener listener) throws BackgroundException {
        if(file.isRoot()) {
            return PathAttributes.EMPTY;
        }
        try {
            final FileAttributes stat;
            if(file.isSymbolicLink()) {
                stat = session.sftp().lstat(file.getAbsolute());
            }
            else {
                stat = session.sftp().stat(file.getAbsolute());
            }
            switch(stat.getType()) {
                case BLOCK_SPECIAL:
                case CHAR_SPECIAL:
                case FIFO_SPECIAL:
                case SOCKET_SPECIAL:
                case REGULAR:
                case SYMLINK:
                    if(!file.getType().contains(Path.Type.file)) {
                        throw new NotfoundException(String.format("File %s is of type %s but expected %s",
                                file.getAbsolute(), stat.getType(), file.getType()));
                    }
                    break;
                case DIRECTORY:
                    if(!file.getType().contains(Path.Type.directory)) {
                        throw new NotfoundException(String.format("File %s is of type %s but expected %s",
                                file.getAbsolute(), stat.getType(), file.getType()));
                    }
                    break;
            }
            return this.toAttributes(stat);
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map("Failure to read attributes of {0}", e, file);
        }
    }

    @Override
    public PathAttributes toAttributes(final FileAttributes stat) {
        final PathAttributes attributes = new PathAttributes();
        switch(stat.getType()) {
            case REGULAR:
            case UNKNOWN:
                attributes.setSize(stat.getSize());
        }
        if(permissionsAvailable) {
            if(0 != stat.getMode().getPermissionsMask()) {
                attributes.setPermission(new Permission(Integer.toString(stat.getMode().getPermissionsMask(), 8)));
            }
            attributes.setOwner(String.valueOf(stat.getUID()));
            attributes.setGroup(String.valueOf(stat.getGID()));
        }

        if(0 != stat.getMtime()) {
            attributes.setModificationDate(stat.getMtime() * 1000L);
        }
        if(0 != stat.getAtime()) {
            attributes.setAccessedDate(stat.getAtime() * 1000L);
        }
        return attributes;
    }

    private boolean isServerBlacklisted() {
        final String serverVersion = session.getClient().getTransport().getServerVersion();
        for(String server : PreferencesFactory.get().getList("sftp.permissions.server.blacklist")) {
            if(StringUtils.contains(
                    /* seq */ serverVersion,
                    /* search seq */ server)) {
                // Known erroneous bitmask
                return true;
            }
        }
        return false;
    }
}
