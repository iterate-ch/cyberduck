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

import ch.cyberduck.core.threading.BackgroundException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @version $Id$
 */
public abstract class AbstractPath {

    /**
     * Shortcut for the home directory
     */
    public static final String HOME = "~";

    public static final int FILE_TYPE = 1;
    public static final int DIRECTORY_TYPE = 2;
    public static final int SYMBOLIC_LINK_TYPE = 4;
    public static final int VOLUME_TYPE = 8;

    /**
     * @return Descriptive features for path
     */
    public abstract Attributes attributes();

    /**
     * Obtain a string representation of the path that is unique for versioned files.
     *
     * @return The absolute path with version ID and checksum if any.
     */
    public String unique() {
        if(this.attributes().isDuplicate()) {
            if(StringUtils.isNotBlank(this.attributes().getVersionId())) {
                return String.format("%s-%s", this.getAbsolute(), this.attributes().getVersionId());
            }
        }
        return this.getAbsolute();
    }

    /**
     * Default implementation returning a reference to self. You can override this
     * if you need a different strategy to compare hashcode and equality for caching
     * in a model.
     *
     * @return Reference to the path to be used in table models an file listing
     *         cache.
     * @see ch.cyberduck.core.Cache#lookup(PathReference)
     */
    public abstract PathReference getReference();

    /**
     * Fetch the directory listing
     *
     * @return Directory listing from server
     */
    public abstract AttributedList<? extends AbstractPath> list() throws BackgroundException;

    public abstract char getPathDelimiter();

    /**
     * @return true if this paths points to '/'
     * @see #getPathDelimiter()
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(String.valueOf(this.getPathDelimiter()));
    }

    public static String getParent(final String absolute, final char delimiter) {
        if(absolute.equals(String.valueOf(delimiter))) {
            return null;
        }
        int index = absolute.length() - 1;
        if(absolute.charAt(index) == delimiter) {
            if(index > 0) {
                index--;
            }
        }
        int cut = absolute.lastIndexOf(delimiter, index);
        if(cut > 0) {
            return absolute.substring(0, cut);
        }
        //if (index == 0) parent is root
        return String.valueOf(delimiter);
    }

    public static String getName(final String path) {
        //StringUtils.removeStart(absolute, this.getAbsolute() + Path.DELIMITER);
        if(String.valueOf(Path.DELIMITER).equals(path)) {
            return path;
        }
        return FilenameUtils.getName(path);
    }

    public abstract String getAbsolute();

    public abstract String getName();

    /**
     * Subclasses may override to return a user friendly representation of the name denoting this path.
     *
     * @return Name of the file
     * @see #getName()
     */
    public String getDisplayName() {
        return this.getName();
    }

    /**
     * @return The parent directory or self if this is the root of the hierarchy
     */
    public abstract AbstractPath getParent();

    /**
     * @return True if the path denoted exists
     */
    public abstract boolean exists() throws BackgroundException;

    /**
     * @return the extension if any or null otherwise
     */
    public String getExtension() {
        final String extension = FilenameUtils.getExtension(this.getName());
        if(StringUtils.isEmpty(extension)) {
            return null;
        }
        return extension;
    }

    /**
     * @param directory Parent directory
     * @return True if this is a child in the path hierarchy of the argument passed
     */
    public boolean isChild(final AbstractPath directory) {
        if(directory.attributes().isFile()) {
            // If a file we don't have any children at all
            return false;
        }
        if(this.isRoot()) {
            // Root cannot be a child of any other path
            return false;
        }
        if(directory.isRoot()) {
            // Any other path is a child
            return true;
        }
        if(ObjectUtils.equals(this.getParent(), directory.getParent())) {
            // Cannot be a child if the same parent
            return false;
        }
        for(AbstractPath parent = this.getParent(); !parent.isRoot(); parent = parent.getParent()) {
            if(parent.equals(directory)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The target of the symbolic link if this path denotes a symbolic link, null otherwise.
     * @see ch.cyberduck.core.PathAttributes#isSymbolicLink
     */
    public abstract AbstractPath getSymlinkTarget();


    /**
     * Create a new empty file.
     */
    public abstract boolean touch() throws BackgroundException;

    /**
     * #getAbsolute -> target
     *
     * @param target Where this file should point to
     */
    public abstract void symlink(String target) throws BackgroundException;

    /**
     * Create a new folder.
     */
    public abstract void mkdir() throws BackgroundException;

    /**
     * @param permission@see Session#isUnixPermissionsSupported()
     */
    public abstract void writeUnixPermission(Permission permission) throws BackgroundException;

    /**
     * @param created  Creation timestamp of file
     * @param modified Modification timestamp of file
     * @param accessed @see ch.cyberduck.core.Session#isTimestampSupported()
     */
    public abstract void writeTimestamp(long created, long modified, long accessed) throws BackgroundException;
}
