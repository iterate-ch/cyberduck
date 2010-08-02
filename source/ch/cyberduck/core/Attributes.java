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

import ch.cyberduck.core.i18n.Locale;

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

    public String getOwner() {
        return Locale.localizedString("Unknown");
    }

    public String getGroup() {
        return Locale.localizedString("Unknown");
    }

    public abstract String getChecksum();

    /**
     * A version identifiying a particular revision of a file
     * with the same path.
     *
     * @return Version Identifier or null if not versioned.
     */
    public String getVersionId() {
        return null;
    }

    /**
     * If the path should not be displayed in a browser by default unless the user
     * explicitly chooses to show hidden files.
     *
     * @return True if hidden by default.
     */
    public boolean isDuplicate() {
        return false;
    }

    /**
     * @return The incrementing revision number of the file or null if not versioned.
     */
    public String getRevision() {
        return null;
    }

    /**
     * @param other
     * @return True if the checksum matches or the references are equal.
     * @see #getChecksum()
     */
    @Override
    public boolean equals(Object other) {
        if(other instanceof Attributes) {
            if(StringUtils.isNotBlank(this.getVersionId()) && StringUtils.isNotBlank(((PathAttributes) other).getVersionId())) {
                // Compare by version ID if e.g. supported by S3
                return this.getVersionId().equals(((PathAttributes) other).getVersionId());
            }
        }
        return super.equals(other);
    }
}