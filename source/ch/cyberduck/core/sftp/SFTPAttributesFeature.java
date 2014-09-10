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

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Attributes;

import java.io.IOException;

import net.schmizz.sshj.sftp.FileAttributes;
import net.schmizz.sshj.sftp.FileMode;

/**
 * @version $Id$
 */
public class SFTPAttributesFeature implements Attributes {

    private SFTPSession session;

    public SFTPAttributesFeature(final SFTPSession session) {
        this.session = session;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        try {
            return this.convert(session.sftp().stat(file.getAbsolute()));
        }
        catch(IOException e) {
            throw new SFTPExceptionMappingService().map(e);
        }
    }

    @Override
    public Attributes withCache(Cache<Path> cache) {
        return this;
    }

    public PathAttributes convert(final FileAttributes stat) {
        final PathAttributes attributes = new PathAttributes();
        if(stat.getType().equals(FileMode.Type.REGULAR)) {
            attributes.setSize(stat.getSize());
        }
        if(0 != stat.getMode().getPermissionsMask()) {
            attributes.setPermission(new Permission(Integer.toString(stat.getMode().getPermissionsMask(), 8)));
        }
        if(0 != stat.getUID()) {
            attributes.setOwner(String.valueOf(stat.getUID()));
        }
        if(0 != stat.getGID()) {
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
}
