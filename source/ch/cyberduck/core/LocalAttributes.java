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

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class LocalAttributes extends Attributes {
    private static final Logger log = Logger.getLogger(LocalAttributes.class);

    private String path;

    private String checksum;

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

    public void setModificationDate(final long timestamp) {
        if(timestamp < 0) {
            return;
        }
        if(!new File(path).setLastModified(timestamp)) {
            log.error(String.format("Write modification date failed for %s", path));
        }
    }

    @Override
    public long getSize() {
        if(this.isDirectory()) {
            return -1;
        }
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

    public void setPermission(final Permission permission) {
        this.permission = permission;
    }

    /**
     * @return True if package directory structure represented as a single file for the user.
     */
    public boolean isBundle() {
        return false;
    }

    @Override
    public boolean isVolume() {
        return null == new File(path).getParent();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    @Override
    public boolean isDirectory() {
        return new File(path).isDirectory();
    }

    /**
     * This is only returning the correct result if the file already exists.
     *
     * @see Local#exists()
     */
    @Override
    public boolean isFile() {
        return new File(path).isFile();
    }

    /**
     * Checks whether a given file is a symbolic link.
     * <p/>
     * <p>It doesn't really test for symbolic links but whether the
     * canonical and absolute paths of the file are identical - this
     * may lead to false positives on some platforms.</p>
     *
     * @return true if the file is a symbolic link.
     */
    @Override
    public boolean isSymbolicLink() {
        final File f = new File(path);
        if(!f.exists()) {
            return false;
        }
        // For a link that actually points to something (either a file or a directory),
        // the absolute path is the path through the link, whereas the canonical path
        // is the path the link references.
        try {
            return !f.getAbsolutePath().equals(f.getCanonicalPath());
        }
        catch(IOException e) {
            return false;
        }
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
    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(final String checksum) {
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
