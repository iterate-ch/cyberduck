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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Objects;

public class LocalAttributes implements Attributes {
    private static final Logger log = LogManager.getLogger(LocalAttributes.class);

    private final String path;

    public LocalAttributes(final String path) {
        this.path = path;
    }

    private static BasicFileAttributes readAttributes(final String path) throws IOException {
        return readAttributes(path, BasicFileAttributes.class);
    }

    private static <T extends BasicFileAttributes> T readAttributes(final String path, final Class<T> type) throws IOException {
        try {
            return Files.readAttributes(Paths.get(path), type, LinkOption.NOFOLLOW_LINKS);
        }
        catch(UnsupportedOperationException e) {
            log.warn("Failure {} retrieving attributes of {}", e, path);
            throw new IOException(e);
        }
    }

    @Override
    public long getModificationDate() {
        try {
            return readAttributes(path).lastModifiedTime().toMillis();
        }
        catch(IOException e) {
            log.warn("Failure {} getting timestamp of {}", e, path);
            return -1L;
        }
    }

    /**
     * @return The modification date instead.
     */
    @Override
    public long getCreationDate() {
        try {
            return readAttributes(path).creationTime().toMillis();
        }
        catch(IOException e) {
            log.warn("Failure {} getting timestamp of {}", e, path);
            return -1L;
        }
    }

    /**
     * @return The modification date instead.
     */
    @Override
    public long getAccessedDate() {
        try {
            return readAttributes(path).lastAccessTime().toMillis();
        }
        catch(IOException e) {
            log.warn("Failure {} getting timestamp of {}", e, path);
            return -1L;
        }
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
            log.warn("Failure getting size of {}. {}", path, e.getMessage());
            return -1L;
        }
    }

    @Override
    public Permission getPermission() {
        if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            try {
                return new LocalPermission(PosixFilePermissions.toString(readAttributes(path, PosixFileAttributes.class).permissions()));
            }
            catch(IOException e) {
                return Permission.EMPTY;
            }
        }
        return new LocalPermission();
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
        if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            try {
                return readAttributes(path, PosixFileAttributes.class).owner().getName();
            }
            catch(IOException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public String getGroup() {
        if(FileSystems.getDefault().supportedFileAttributeViews().contains("posix")) {
            try {
                return readAttributes(path, PosixFileAttributes.class).group().getName();
            }
            catch(IOException e) {
                return null;
            }
        }
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
