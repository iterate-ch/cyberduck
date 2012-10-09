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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jets3t.service.utils.Mimetypes;

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
        if(this.attributes().isDuplicate()) {
            if(StringUtils.isNotBlank(this.attributes().getVersionId())) {
                reference.append("-").append(this.attributes().getVersionId());
            }
            else if(StringUtils.isNotBlank(this.attributes().getChecksum())) {
                reference.append("-").append(this.attributes().getChecksum());
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

    protected String getParent(String absolute) {
        int index = absolute.length() - 1;
        if(absolute.charAt(index) == this.getPathDelimiter()) {
            if(index > 0) {
                index--;
            }
        }
        int cut = absolute.lastIndexOf(this.getPathDelimiter(), index);
        if(cut > 0) {
            return absolute.substring(0, cut);
        }
        //if (index == 0) parent is root
        return String.valueOf(this.getPathDelimiter());
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

    public String getMimeType() {
        return getMimeType(this.getName().toLowerCase());
    }

    public static String getMimeType(String extension) {
        return Mimetypes.getInstance().getMimetype(extension);
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

    protected abstract void setPath(String name);

    /**
     * @param directory Parent directory
     * @return True if this is a child in the path hierarchy of the argument passed
     */
    public boolean isChild(AbstractPath directory) {
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

    public abstract String kind();

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
     * Create a new empty file.
     *
     * @param recursive Create intermediate directories as required.  If this option is
     *                  not specified, the full path prefix of each operand must already exist
     */
    public void touch(boolean recursive) {
        if(!this.exists()) {
            if(recursive) {
                if(!this.getParent().exists()) {
                    this.getParent().mkdir(recursive);
                }
            }
            this.touch();
        }
    }

    /**
     * Create a new folder.
     */
    public abstract void mkdir();

    /**
     * Create a new folder.
     *
     * @param recursive Create intermediate directories as required.  If this option is
     *                  not specified, the full path prefix of each operand must already exist
     */
    public void mkdir(boolean recursive) {
        if(recursive) {
            final AbstractPath parent = this.getParent();
            if(!parent.exists()) {
                parent.mkdir(recursive);
            }
        }
        this.mkdir();
    }

    /**
     * @param perm      The permissions to apply
     * @param recursive Include subdirectories and files
     * @see Session#isUnixPermissionsSupported()
     */
    public abstract void writeUnixPermission(Permission perm, boolean recursive);

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
     * @return True if the directory contains no children items
     */
    public boolean isEmpty() {
        return this.children().size() == 0;
    }

    /**
     * @param renamed Must be an absolute path
     */
    public abstract void rename(AbstractPath renamed);

    /**
     * Duplicate file
     *
     * @param copy Destination
     */
    public abstract void copy(AbstractPath copy);

    public static class DescriptiveUrl {
        private String url = StringUtils.EMPTY;

        private String help = StringUtils.EMPTY;

        public DescriptiveUrl(String url) {
            this(url, Locale.localizedString("Open in Web Browser"));
        }

        public DescriptiveUrl(String url, String help) {
            this.url = url;
            this.help = help;
        }

        public String getUrl() {
            return url;
        }

        public String getHelp() {
            return help;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof DescriptiveUrl) {
                return this.getUrl().equals(((DescriptiveUrl) obj).getUrl());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.getUrl().hashCode();
        }
    }
}
