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

import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public abstract class Attributes {

    /**
     * @return
     * @see AbstractPath#FILE_TYPE
     * @see AbstractPath#DIRECTORY_TYPE
     * @see AbstractPath#SYMBOLIC_LINK_TYPE
     * @see #isDirectory()
     * @see #isFile()
     * @see #isSymbolicLink()
     */
    public abstract int getType();

    /**
     * @return The length of the file
     */
    public abstract long getSize();

    /**
     * @return The time the file was last modified in millis UTC or -1 if unknown
     */
    public abstract long getModificationDate();

    /**
     * @return The time the file was created in millis UTC or -1 if unknown
     */
    public abstract long getCreationDate();

    /**
     * @return The time the file was last accessed in millis UTC or -1 if unknown
     */
    public abstract long getAccessedDate();

    /**
     * @return The file permission mask or null if unknown
     */
    public abstract Permission getPermission();

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

    public abstract String getOwner();

    public abstract String getGroup();

    public abstract String getChecksum();

    /**
     * @param other
     * @return True if the checksum matches or the references are equal.
     * @see #getChecksum()
     */
    @Override
    public boolean equals(Object other) {
        if(other instanceof Attributes) {
            if(StringUtils.isNotBlank(this.getChecksum()) && StringUtils.isNotBlank(((Attributes) other).getChecksum())) {
                // Compare by checksum if e.g. supported by S3
                return this.getChecksum().equals(((Attributes) other).getChecksum());
            }
        }
        return super.equals(other);
    }
}