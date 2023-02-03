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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;

public class LocalAttributes extends Attributes {
    private static final Logger log = LogManager.getLogger(LocalAttributes.class);

    private final String path;

    public LocalAttributes(final String path) {
        this.path = path;
    }

    @Override
    public long getModificationDate() {
        try {
            return Files.getLastModifiedTime(Paths.get(path)).toMillis();
        }
        catch(IOException e) {
            log.warn(String.format("Failure getting timestamp of %s. %s", path, e.getMessage()));
            return -1L;
        }
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
            throw new LocalAccessDeniedException(String.format("Cannot change timestamp of %s to %d", path, timestamp), e);
        }
    }

    @Override
    public long getSize() {
        try {
            return Files.size(Paths.get(path));
        }
        catch(IOException e) {
            log.warn(String.format("Failure getting size of %s. %s", path, e.getMessage()));
            return -1L;
        }
    }

    @Override
    public Permission getPermission() {
        if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            final BasicFileAttributes attributes;
            try {
                return new LocalPermission(PosixFilePermissions.toString(Files.readAttributes(Paths.get(path), PosixFileAttributes.class, LinkOption.NOFOLLOW_LINKS).permissions()));
            }
            catch(IOException e) {
                return Permission.EMPTY;
            }
        }
        return Permission.EMPTY;
    }

    public void setPermission(final Permission permission) throws AccessDeniedException {
        if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            try {
                Files.setPosixFilePermissions(Paths.get(path), PosixFilePermissions.fromString(permission.getSymbol()));
            }
            catch(IllegalArgumentException | IOException e) {
                throw new LocalAccessDeniedException(String.format("Cannot change permissions of %s to %s", path, permission.getSymbol()), e);
            }
        }
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
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final LocalAttributes that = (LocalAttributes) o;
        return Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LocalAttributes{");
        sb.append("path='").append(path).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
