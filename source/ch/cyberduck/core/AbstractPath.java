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
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

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
        final StringBuilder reference = new StringBuilder(this.getAbsolute());
        final Attributes attributes = this.attributes();
        if(attributes.isDuplicate()) {
            if(StringUtils.isNotBlank(attributes.getVersionId())) {
                reference.append("-").append(attributes.getVersionId());
            }
            else if(StringUtils.isNotBlank(attributes.getChecksum())) {
                reference.append("-").append(attributes.getChecksum());
            }
        }
        return reference.toString();
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

    public abstract String toURL();

    /**
     * Fetch the directory listing
     *
     * @return Directory listing from server
     */
    public abstract AttributedList<? extends AbstractPath> list();

    /**
     * Get the cached directory listing if any or return #list instead.
     * No sorting and filtering applied.
     *
     * @return Cached directory listing as returned by the server
     * @see #list()
     */
    public AttributedList<? extends AbstractPath> children() {
        return this.children(null);
    }

    /**
     * Get the cached directory listing if any or return #list instead.
     * No sorting applied.
     *
     * @param filter Filter to apply to directory listing
     * @return Cached directory listing as returned by the server filtered
     * @see #list()
     */
    public AttributedList<? extends AbstractPath> children(PathFilter<? extends AbstractPath> filter) {
        return this.children(null, filter);
    }

    /**
     * Request a sorted and filtered file listing from the server. Has to be a directory.
     * A cached listing is returned if possible
     *
     * @param comparator The comparator to sort the listing with
     * @param filter     The filter to exlude certain files
     * @return The children of this path or an empty list if it is not accessible for some reason
     * @see #list()
     */
    public AttributedList<? extends AbstractPath> children(final Comparator<? extends AbstractPath> comparator,
                                                           final PathFilter<? extends AbstractPath> filter) {
        return this.list().filter(comparator, filter);
    }

    public abstract char getPathDelimiter();

    /**
     * @return true if this paths points to '/'
     * @see #getPathDelimiter()
     */
    public boolean isRoot() {
        return this.getAbsolute().equals(String.valueOf(this.getPathDelimiter()));
    }

    public static String getParent(final String absolute, final char delimiter) {
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

    public abstract AbstractPath getParent();

    public abstract boolean exists();


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
     * @param parent The parent directory
     * @param name   The relative filename
     */
    public void setPath(final String parent, final String name) {
        final String p;
        if(StringUtils.isBlank(parent)) {
            p = String.valueOf(this.getPathDelimiter());
        }
        else {
            p = parent;
        }
        // Determine if the parent path already ends with a delimiter
        if(p.endsWith(String.valueOf(this.getPathDelimiter()))) {
            this.setPath(p + name);
        }
        else {
            this.setPath(p + this.getPathDelimiter() + name);
        }
    }

    public abstract void setPath(String name);

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
        if(this.getParent().equals(directory.getParent())) {
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
    public abstract void touch();

    /**
     * #getAbsolute -> target
     *
     * @param target Where this file should point to
     */
    public abstract void symlink(String target);

    /**
     * Create a new folder.
     */
    public abstract void mkdir();

    /**
     * @param permission@see Session#isUnixPermissionsSupported()
     */
    public abstract void writeUnixPermission(Permission permission);

    /**
     * @param created  Creation timestamp of file
     * @param modified Modification timestamp of file
     * @param accessed @see ch.cyberduck.core.Session#isTimestampSupported()
     */
    public abstract void writeTimestamp(long created, long modified, long accessed);

    /**
     * Remove this file from the remote host. Does not affect any corresponding local file
     */
    public abstract void delete();

    /**
     * @param renamed Must be an absolute path
     */
    public abstract void rename(AbstractPath renamed);
}
