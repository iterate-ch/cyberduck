package ch.cyberduck.core;

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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.io.Checksum;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;

public class LocalAttributes extends Attributes {
    private static final Logger log = Logger.getLogger(LocalAttributes.class);

    private final String path;
    private Checksum checksum = Checksum.NONE;
    private Permission permission = Permission.EMPTY;

    public LocalAttributes(final String path) {
        this.path = path;
        this.permission = new LocalPermission();
    }

    @Override
    public long getModificationDate() {
        if(Files.exists(Paths.get(path))) {
            try {
                return Files.getLastModifiedTime(Paths.get(path)).toMillis();
            }
            catch(IOException e) {
                log.warn(String.format("Failure getting timestamp of %s. %s", path, e.getMessage()));
            }
        }
        return -1;
    }

    /**
     * @return The modification date instead.
     */
    @Override
    public long getCreationDate() {
        return this.getModificationDate();
    }

    /**
     * @return The modification date instead.
     */
    @Override
    public long getAccessedDate() {
        return this.getModificationDate();
    }

    public void setModificationDate(final long timestamp) throws AccessDeniedException {
        if(timestamp < 0) {
            return;
        }
        try {
            Files.setLastModifiedTime(Paths.get(path), FileTime.fromMillis(timestamp));
        }
        catch(IOException e) {
            throw new LocalAccessDeniedException(String.format("Cannot change timestamp for %s", path), e);
        }
    }

    @Override
    public long getSize() {
        if(Files.exists(Paths.get(path))) {
            try {
                return Files.size(Paths.get(path));
            }
            catch(IOException e) {
                log.warn(String.format("Failure getting size of %s. %s", path, e.getMessage()));
            }
        }
        return -1;
    }

    @Override
    public Permission getPermission() {
        return permission;
    }

    public void setPermission(final Permission permission) throws AccessDeniedException {
        this.permission = permission;
    }

    /**
     * @return True if package directory structure represented as a single file for the user.
     */
    public boolean isBundle() {
        return false;
    }

    @Override
    public String getOwner() {
        return null;
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public Checksum getChecksum() {
        return checksum;
    }

    public void setChecksum(final Checksum checksum) {
        this.checksum = checksum;
    }

    protected class LocalPermission extends Permission {
        public LocalPermission() {
            //
        }

        public LocalPermission(final String mode) {
            super(mode);
        }

        public LocalPermission(final int mode) {
            super(mode);
        }

        @Override
        public boolean isReadable() {
            return Files.isReadable(Paths.get(path));
        }

        @Override
        public boolean isWritable() {
            return Files.isWritable(Paths.get(path));
        }

        @Override
        public boolean isExecutable() {
            return Files.isExecutable(Paths.get(path));
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LocalAttributes{");
        sb.append("checksum='").append(checksum).append('\'');
        sb.append(", timestamp=").append(this.getModificationDate());
        sb.append(", permission=").append(permission);
        sb.append('}');
        return sb.toString();
    }
}
