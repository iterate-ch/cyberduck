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
import ch.cyberduck.core.io.Checksum;

import java.io.File;

public class LocalAttributes extends Attributes {

    private String path;

    private Checksum checksum;

    private Permission permission;

    public LocalAttributes(final String path) {
        this.path = path;
        this.permission = new LocalPermission();
    }

    @Override
    public long getModificationDate() {
        final File file = new File(path);
        if(file.exists()) {
            return file.lastModified();
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
        if(!new File(path).setLastModified(timestamp)) {
            throw new AccessDeniedException(String.format("Cannot change timestamp for %s", path));
        }
    }

    @Override
    public long getSize() {
        final File file = new File(path);
        if(file.exists()) {
            return new File(path).length();
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
            return new File(path).canRead();
        }

        @Override
        public boolean isWritable() {
            return new File(path).canWrite();
        }

        @Override
        public boolean isExecutable() {
            return new File(path).canExecute();
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
