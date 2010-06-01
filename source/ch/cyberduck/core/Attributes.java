package ch.cyberduck.core;

/*
 *  Copyright (c) 2007 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

/**
 * @version $Id$
 */
public interface Attributes {

    public abstract int getType();

    /**
     * @param i
     * @see AbstractPath#FILE_TYPE
     * @see AbstractPath#DIRECTORY_TYPE
     * @see AbstractPath#SYMBOLIC_LINK_TYPE
     * @see #isDirectory()
     * @see #isFile()
     * @see #isSymbolicLink()
     */
    public abstract void setType(int i);

    /**
     * @return The length of the file
     */
    public abstract long getSize();

    /**
     * @return The time the file was last modified in millis UTC or -1 if unknown
     */
    public abstract long getModificationDate();

    public abstract void setModificationDate(long millis);

    /**
     * @return The time the file was created in millis UTC or -1 if unknown
     */
    public abstract long getCreationDate();

    public abstract void setCreationDate(long millis);

    /**
     * @return The time the file was last accessed in millis UTC or -1 if unknown
     */
    public abstract long getAccessedDate();

    public abstract void setAccessedDate(long millis);

    /**
     * @return The file permission mask or null if unknown
     */
    public abstract Permission getPermission();

    public abstract void setPermission(Permission permission);

    /**
     * @return True if this path denotes a directory or is a symbolic link pointing to a directory
     */
    public abstract boolean isDirectory();

    public abstract boolean isVolume();

    /**
     * @return True if this path denotes a regular file or is a symbolic link pointing to a regular file
     */
    public abstract boolean isFile();

    /**
     * @return True if this path denotes a symbolic link.
     *         Warning! Returns false for Mac OS Classic Alias
     */
    public abstract boolean isSymbolicLink();

    public abstract void setSize(long size);

    public abstract void setOwner(String owner);

    public abstract void setGroup(String group);

    public abstract String getOwner();

    public abstract String getGroup();

    public abstract String getChecksum();

    public abstract void setChecksum(String md5);

    public abstract void setStorageClass(String redundancy);

    public abstract String getStorageClass();

    public abstract void setVersionId(String versionId);

    public abstract String getVersionId();
}