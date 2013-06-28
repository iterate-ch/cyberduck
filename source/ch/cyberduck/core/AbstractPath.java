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
        if(ObjectUtils.equals(getParent(this.getAbsolute(), this.getPathDelimiter()), getParent(directory.getAbsolute(), this.getPathDelimiter()))) {
            // Cannot be a child if the same parent
            return false;
        }
        for(String parent = getParent(this.getAbsolute(), this.getPathDelimiter()); !parent.equals(String.valueOf(this.getPathDelimiter())); parent = getParent(parent, this.getPathDelimiter())) {
            if(parent.equals(directory.getAbsolute())) {
                return true;
            }
        }
        return false;
    }
}